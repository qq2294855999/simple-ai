package com.simple.ai.service.agentChat;

import com.simple.ai.common.dto.agent.AgentContext;
import com.simple.ai.common.dto.agentChat.*;
import com.simple.ai.common.dto.atomicCommand.FindOneAtomicCommandRequest;
import com.simple.ai.common.dto.command.*;
import com.simple.ai.common.entity.agentChatMessage.AgentChatMessage;
import com.simple.ai.common.entity.agentChatSession.AgentChatSession;
import com.simple.ai.common.entity.agentDefinition.AgentDefinition;
import com.simple.ai.common.entity.atomicCommand.AtomicCommand;
import com.simple.ai.common.entity.executionEvent.ExecutionEvent;
import com.simple.ai.common.entity.task.Task;
import com.simple.ai.common.entity.taskDetail.TaskDetail;
import com.simple.ai.common.enums.AgentChatMessageFormatProcess;
import com.simple.ai.common.enums.AgentChatMessageRoleProcess;
import com.simple.ai.common.enums.AgentExecutionStatusProcess;
import com.simple.ai.common.service.agentChat.AgentChatService;
import com.simple.ai.common.service.chatTurn.ChatTurnService;
import com.simple.ai.common.service.command.CommandDispatchService;
import com.simple.ai.common.service.executionEvent.ExecutionEventBus;
import com.simple.ai.common.view.agentChatMessage.AgentChatMessageView;
import com.simple.ai.common.view.agentChatSession.AgentChatSessionView;
import com.simple.ai.common.view.agentDefinition.AgentDefinitionView;
import com.simple.ai.common.view.atomicCommand.AtomicCommandView;
import com.simple.ai.common.view.chatTurn.ChatTurnView;
import com.simple.ai.common.view.executionEvent.ExecutionEventView;
import com.simple.ai.common.view.task.TaskView;
import com.simple.ai.common.view.taskDetail.TaskDetailView;
import com.simple.ai.service.agent.AgentContextAssembler;
import com.simple.common.auth.client.util.LoginUserUtils;
import com.simple.common.core.common.service.lock.LockService;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.core.utils.JsonUtils;
import com.simple.common.mp.common.enums.Status;
import com.simple.common.websocket.utils.WebSocketUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 智能体聊天服务默认实现。
 *
 * @author qty
 */
@Slf4j
@Service
class DefaultAgentChatService implements AgentChatService {

    /**
     * HTML 标签匹配表达式
     */
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("(?is)<[^>]*>");

    /**
     * 智能体定义视图
     */
    @Autowired
    private AgentDefinitionView agentDefinitionView;

    /**
     * 聊天会话视图
     */
    @Autowired
    private AgentChatSessionView agentChatSessionView;

    /**
     * 聊天消息视图
     */
    @Autowired
    private AgentChatMessageView agentChatMessageView;

    /**
     * 任务视图
     */
    @Autowired
    private TaskView taskView;

    /**
     * 任务详情视图
     */
    @Autowired
    private TaskDetailView taskDetailView;

    /**
     * 命令调度服务
     */
    @Autowired
    private CommandDispatchService commandDispatchService;

    /**
     * 原子命令视图，用于 upsert 执行器返回的命令能力
     */
    @Autowired
    private AtomicCommandView atomicCommandView;

    /**
     * 智能体上下文组装器
     */
    @Autowired
    private AgentContextAssembler agentContextAssembler;

    /**
     * 智能体会话服务
     */
    /**
     * 事务模板
     */
    @Autowired
    private TransactionTemplate transactionTemplate;

    /**
     * 对话轮次服务，管理每轮对话的轮次记录
     */
    @Autowired
    private ChatTurnService chatTurnService;

    /**
     * 执行事件总线，将调度进度事件转换为 ExecutionEvent 并持久化
     */
    @Autowired
    private ExecutionEventBus executionEventBus;

    /**
     * 执行事件视图，用于查询消息关联的执行事件
     */
    @Autowired
    private ExecutionEventView executionEventView;

    /**
     * 对话轮次视图，用于查询轮次状态
     */
    @Autowired
    private ChatTurnView chatTurnView;

    /**
     * 分布式锁服务，用于会话级并发控制
     */
    @Autowired
    private LockService lockService;

    /**
     * Redis 字符串模板，用于幂等键去重
     */
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public AgentChatSessionResponse createSession(CreateAgentChatSessionRequest request) {
        AssertUtils.notEmpty(request.getAgentId(), "智能体主键不能为空");
        AssertUtils.notEmpty(request.getModelId(), "模型主键不能为空");
        AssertUtils.notEmpty(request.getClientId(), "客户端主键不能为空");

        // 校验会话绑定的智能体存在并已启用
        AgentDefinition agentDefinition = loadEnabledAgent(request.getAgentId());

        // 校验执行客户端在线状态，避免后续 WebSocket 调用无谓等待超时
        AssertUtils.isTrue(isClientOnline(request.getClientId()), "执行客户端[{}]不在线，请先启动客户端", request.getClientId());

        // 创建期装配快照：查询全部资产并序列化为 JSON
        String snapshotJson = agentContextAssembler.assembleForSnapshot(request.getAgentId(), request.getClientId());

        // 通过 WebSocket 同步查询执行器能力，获取支持的原子命令列表
        List<CapabilityResultDto.CommandItem> capabilities = fetchCapabilities(request.getClientId());

        // 将执行器能力命令 upsert 到本地 atomic_command 表
        upsertCapabilityCommands(capabilities);

        // 将能力命令列表合并到快照中
        snapshotJson = agentContextAssembler.enrichSnapshotWithCapabilities(snapshotJson, capabilities);

        // 创建持久化会话，保存完整上下文配置（含快照）
        AgentChatSession session = createSessionEntity(agentDefinition, request.getModelId(), request.getClientId(), snapshotJson);
        transactionTemplate.executeWithoutResult(status -> agentChatSessionView.save(session));
        return buildSessionResponse(session, agentDefinition);
    }

    @Override
    public List<AgentChatSessionResponse> findSessions(String agentId, String modelId, String clientId) {
        AssertUtils.notEmpty(agentId, "智能体主键不能为空");

        // 校验智能体存在后加载该智能体历史会话，按模型和客户端过滤
        AgentDefinition agentDefinition = loadEnabledAgent(agentId);
        List<AgentChatSession> sessions = agentChatSessionView.findAllByAgentId(agentId, modelId, clientId);
        return buildSessionResponses(sessions, agentDefinition);
    }

    @Override
    public List<AgentChatMessageResponse> findMessages(String sessionId) {
        AssertUtils.notEmpty(sessionId, "会话主键不能为空");

        // 读取会话消息并按序转换为前端响应
        List<AgentChatMessage> messages = agentChatMessageView.findAllBySessionId(sessionId);
        return buildMessageResponses(messages);
    }

    @Override
    public List<AgentChatMessageResponse> findMessages(String sessionId, int size, long beforeSequenceNo) {
        AssertUtils.notEmpty(sessionId, "会话主键不能为空");

        // 按序号倒序分页查询，再反转为升序后返回
        List<AgentChatMessage> messages = agentChatMessageView.findPageBySessionId(sessionId, beforeSequenceNo, size);
        java.util.Collections.reverse(messages);
        return buildMessageResponses(messages);
    }

    @Override
    public void sendStream(SendAgentChatMessageRequest request, Consumer<ChatSseEvent> eventConsumer) {
        AssertUtils.notEmpty(request.getSessionId(), "会话主键不能为空");
        AssertUtils.notEmpty(request.getContent(), "用户消息不能为空");

        // 幂等检查：通过 Redis SETNX 防止断线重连后产生重复消息
        checkIdempotent(request);

        // 会话级分布式锁：同一会话并发请求排队执行，避免消息序号错乱
        String lockKey = "chat:session:lock:" + request.getSessionId();
        lockService.lock(lockKey, () -> sendStreamInternal(request, eventConsumer));
    }

    /**
     * 流式发送聊天消息的内部实现，在分布式锁保护下执行。
     *
     * @param request       发送消息请求
     * @param eventConsumer SSE 事件消费者
     */
    private void sendStreamInternal(SendAgentChatMessageRequest request, Consumer<ChatSseEvent> eventConsumer) {

        // 在短事务内锁定会话并持久化用户消息，避免模型执行期间占用数据库锁
        AgentChatSession session = saveUserMessage(request);

        // 创建本轮对话轮次记录
        String turnId = startChatTurn(session, request);

        // 创建事件发送器，用于统一 SSE 事件输出
        ChatEventSender eventSender = new ChatEventSender(eventConsumer);

        // 装配智能体上下文（定义、规则、技能、记忆、客户端、执行器、协议）
        AgentContext agentContext = assembleAgentContext(session);

        // 创建运行时上下文，持有本轮聊天所需的全部运行时事实
        AgentChatRuntimeContext runtimeContext = new AgentChatRuntimeContext(session.getId(), turnId, session.getUserId(), session.getAgentId(), session.getModelId(), session.getClientId(),
                                                                             "", agentContext, eventSender);

        // 构建组合消费者：将调度事件同时写入执行轨迹并透传给SSE通道
        Consumer<CommandDispatchProgressEvent> compositeConsumer = buildCompositeConsumer(turnId, eventConsumer);

        // 通知客户端用户消息已被服务端接收
        publishChatEvent(eventConsumer, session.getId(), "MESSAGE_ACCEPTED", "用户消息已保存", "", "", false, "");

        // 基于运行时上下文执行 AI 与智能体流程
        CommandDispatchResponse response = dispatchAgentSafely(session, request, runtimeContext, compositeConsumer);

        // 更新运行时上下文中的任务ID
        if (response != null && response.getTaskId() != null) {
            runtimeContext = new AgentChatRuntimeContext(session.getId(), turnId, session.getUserId(), session.getAgentId(), session.getModelId(), session.getClientId(),
                                                         response.getTaskId(), agentContext, eventSender);
        }

        // 在独立短事务内持久化最终消息，保证流式收尾与数据库审计闭环
        String assistantMessageId = saveFinalMessage(session, response, eventConsumer);

        // 完成本轮对话，关联AI回复消息
        chatTurnService.completeTurn(turnId, assistantMessageId, "");

        // 记忆蒸馏不再自动触发，改为由用户或指定体明确要求蒸馏正确线路后再执行
    }

    /**
     * 装配智能体上下文。
     * <p>优先从会话 reserve 快照恢复，避免聊天期重复查询资产表；
     * 快照为空时降级为直接查询 DB 装配（存量会话兼容）。</p>
     *
     * @param session 会话实体
     * @return 智能体上下文
     */
    private AgentContext assembleAgentContext(AgentChatSession session) {
        String reserveJson = session.getReserve();

        // 优先从快照恢复上下文
        if (reserveJson != null && !reserveJson.isBlank()) {
            return agentContextAssembler.restoreFromSnapshot(reserveJson, session.getUserId(), session.getId());
        }

        // 降级：存量会话无快照时直接查询 DB 装配
        CommandDispatchRequest dispatchRequest = new CommandDispatchRequest();
        dispatchRequest.setAgentId(session.getAgentId());
        dispatchRequest.setUserId(session.getUserId());
        dispatchRequest.setSessionId(session.getId());
        dispatchRequest.setClientId(session.getClientId());
        return agentContextAssembler.assemble(dispatchRequest);
    }

    /**
     * 幂等检查：通过 Redis SETNX 判断当前请求是否已处理过。
     * <p>幂等键有效期为5分钟，覆盖正常对话超时窗口。</p>
     *
     * @param request 发送消息请求
     */
    private void checkIdempotent(SendAgentChatMessageRequest request) {
        String idempotencyKey = request.getIdempotencyKey();

        // 无幂等键时跳过检查，保持向后兼容
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return;
        }

        // 使用 Redis SETNX 原子操作判断键是否已存在
        String redisKey = "chat:idempotent:" + idempotencyKey;
        Boolean acquired = stringRedisTemplate.opsForValue().setIfAbsent(redisKey, "1", java.time.Duration.ofMinutes(5));

        // 键已存在说明是重复请求，直接跳过不产生重复消息
        AssertUtils.isTrue(Boolean.TRUE.equals(acquired), "消息已处理，请勿重复发送");
    }

    /**
     * 查询轮次状态，用于断线重连时判断轮次是否已完成。
     *
     * @param turnId 轮次主键
     * @return 轮次状态响应
     */
    @Override
    public AgentChatTurnStatusResponse findTurnStatus(String turnId) {
        AssertUtils.notEmpty(turnId, "轮次主键不能为空");

        // 查询轮次记录
        com.simple.ai.common.entity.chatTurn.ChatTurn turn = chatTurnView.findById(turnId);
        AssertUtils.notEmpty(turn, "轮次[{}]不存在", turnId);

        // 根据是否已关联AI回复消息判断轮次状态
        String turnStatus = (turn.getAssistantMessageId() != null && !turn.getAssistantMessageId().isBlank()) ? "COMPLETED" : "IN_PROGRESS";

        // 组装响应
        AgentChatTurnStatusResponse response = new AgentChatTurnStatusResponse();
        response.setTurnId(turn.getId());
        response.setSessionId(turn.getSessionId());
        response.setTurnNumber(turn.getTurnNumber());
        response.setTurnStatus(turnStatus);
        response.setAssistantMessageId(turn.getAssistantMessageId());
        response.setTaskId(turn.getTaskId());
        return response;
    }

    /**
     * 构建组合事件消费者，在透传事件的同时记录执行轨迹。
     * <p>MESSAGE_* 等聊天层事件由 ExecutionEventBus 内部白名单过滤，不会误录入执行轨迹。</p>
     * <p>内部事件统一映射为 PROGRESS 后输出 SSE。</p>
     *
     * @param turnId        对话轮次主键
     * @param eventConsumer 原始事件消费者（SSE 通道）
     * @return 组合消费者
     */
    private Consumer<CommandDispatchProgressEvent> buildCompositeConsumer(String turnId, Consumer<ChatSseEvent> eventConsumer) {

        // 轮次为空时不记录执行轨迹
        if (turnId == null || turnId.isBlank()) {
            return event -> {
                if (eventConsumer != null) {
                    eventConsumer.accept(mapToSseEvent(event));
                }
            };
        }

        // 组合消费：先记录到执行事件表，再映射为稳定事件透传给原始消费者
        return event -> {
            try {
                executionEventBus.recordEvent(turnId, "", event);
            } catch (RuntimeException e) {

                // 执行轨迹落库异常不影响主流程
                log.warn("执行事件记录失败，turnId={}, eventType={}", turnId, event.getEventType(), e);
            }
            if (eventConsumer != null) {
                eventConsumer.accept(mapToSseEvent(event));
            }
        };
    }

    @Override
    @Transactional
    public void deleteSession(String sessionId) {
        AssertUtils.notEmpty(sessionId, "会话主键不能为空");

        // 级联删除：收集消息中的任务ID，先删任务详情和任务，再删消息，最后删会话
        deleteSessionCascade(Collections.singletonList(sessionId));
    }

    @Override
    @Transactional
    public void deleteSessions(List<String> sessionIds) {
        AssertUtils.notEmpty(sessionIds, "会话主键列表不能为空");

        // 级联删除：收集消息中的任务ID，先删任务详情和任务，再删消息，最后删会话
        deleteSessionCascade(sessionIds);
    }

    @Override
    public List<TaskDetail> findTrajectory(String sessionId) {
        AssertUtils.notEmpty(sessionId, "会话主键不能为空");

        // 查询会话所有消息
        List<AgentChatMessage> messages = agentChatMessageView.findAllBySessionId(sessionId);

        // 收集所有非空任务ID
        Set<String> taskIds = new HashSet<>();
        for (AgentChatMessage message : messages) {
            if (message.getTaskId() != null && !message.getTaskId().isBlank()) {
                taskIds.add(message.getTaskId());
            }
        }

        // 无任务时返回空列表
        if (taskIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查询任务详情
        return taskDetailView.findAllByTaskIds(new ArrayList<>(taskIds));
    }

    /**
     * 创建本轮对话轮次记录。
     * <p>从最近一次保存的用户消息获取消息ID和任务ID，创建对话轮次。</p>
     *
     * @param session 会话实体
     * @param request 发送消息请求
     * @return 轮次主键
     */
    private String startChatTurn(AgentChatSession session, SendAgentChatMessageRequest request) {
        // 从已保存的用户消息中获取最后一条消息作为本轮用户消息
        List<AgentChatMessage> messages = agentChatMessageView.findAllBySessionId(session.getId());
        if (messages.isEmpty()) {
            return "";
        }
        AgentChatMessage userMessage = messages.get(messages.size() - 1);
        String taskId = userMessage.getTaskId() != null ? userMessage.getTaskId() : "";
        com.simple.ai.common.entity.chatTurn.ChatTurn turn = chatTurnService.startTurn(session.getId(), userMessage.getId(), taskId);
        return turn.getId();
    }

    /**
     * 级联删除会话数据。
     *
     * @param sessionIds 会话主键列表
     */
    private void deleteSessionCascade(List<String> sessionIds) {
        AssertUtils.notEmpty(sessionIds, "会话主键列表不能为空");

        // 查询所有消息并收集任务ID
        List<AgentChatMessage> allMessages = agentChatMessageView.findAllBySessionIds(sessionIds);
        Set<String> taskIds = new HashSet<>();
        for (AgentChatMessage message : allMessages) {
            if (message.getTaskId() != null && !message.getTaskId().isBlank()) {
                taskIds.add(message.getTaskId());
            }
        }

        // 删除任务详情
        if (!taskIds.isEmpty()) {
            taskDetailView.deleteByTaskIds(new ArrayList<>(taskIds));
        }

        // 删除任务
        if (!taskIds.isEmpty()) {
            List<Task> tasks = taskView.findAllByIds(new ArrayList<>(taskIds));
            List<String> taskIdList = tasks.stream().map(Task::getId).collect(Collectors.toList());
            if (!taskIdList.isEmpty()) {
                taskView.deleteByIds(taskIdList);
            }
        }

        // 删除消息
        List<String> messageIds = allMessages.stream().map(AgentChatMessage::getId).collect(Collectors.toList());
        if (!messageIds.isEmpty()) {
            agentChatMessageView.deleteByIds(messageIds);
        }

        // 删除会话
        agentChatSessionView.deleteByIds(sessionIds);

    }

    /**
     * 查询已启用智能体。
     *
     * @param agentId 智能体主键
     * @return 智能体实体
     */
    private AgentDefinition loadEnabledAgent(String agentId) {
        AgentDefinition agentDefinition = agentDefinitionView.findById(agentId);
        AssertUtils.notEmpty(agentDefinition, "智能体[{}]不存在", agentId);
        AssertUtils.isTrue(Status.ON.equals(agentDefinition.getStatus()), "智能体[{}]未启用", agentId);
        return agentDefinition;
    }

    /**
     * 通过 WebSocket 同步查询执行器能力，获取支持的原子命令列表。
     * <p>调用 system.capability 系统命令，通过框架级 sendSyncMsg 阻塞等待执行器返回 COMMAND_RESULT，
     * 解析其中的 data 数组（裸命令列表）并返回。超时或返回失败时抛出异常，
     * 阻断会话创建。</p>
     *
     * @param clientId 客户端ID
     * @return 执行器支持的原子命令元信息列表
     */
    private List<CapabilityResultDto.CommandItem> fetchCapabilities(String clientId) {
        // 构建 system.capability 批量命令请求
        String commandId = UUID.randomUUID().toString();
        ExecutorCommandItem item = new ExecutorCommandItem().setCommandId(commandId)
                                                            .setSequenceNo(1)
                                                            .setAtomicCommandCode("system.capability")
                                                            .setTimeoutMs((int) TimeUnit.SECONDS.toMillis(60));

        ExecutorCommandBatchRequest batchRequest = new ExecutorCommandBatchRequest().setDispatchId(UUID.randomUUID().toString())
                                                                                    .setTaskId("session-create-" + UUID.randomUUID().toString())
                                                                                    .setClientId(clientId)
                                                                                    .setStopOnFailure(Boolean.TRUE)
                                                                                    .setCommands(Collections.singletonList(item));

        // 使用 SEP 外层消息封装，通过框架级 sendSyncMsg 阻塞等待回执
        SepMessage<ExecutorCommandBatchRequest> message = new SepMessage<>();
        message.setMessageType("COMMAND_BATCH");
        message.setPayload(batchRequest);

        // 调用框架级同步发送，阻塞等待执行器返回
        Object result = WebSocketUtils.sendSyncMsg("agent-executor", clientId, message);

        // 从返回值提取 ExecutorCommandResultResponse
        ExecutorCommandResultResponse executorResult = extractExecutorResult(result);
        AssertUtils.isTrue(executorResult.getSuccess() != null && executorResult.getSuccess(), "执行器能力查询失败: %s",
                           executorResult.getError() != null ? executorResult.getError().getDetail() : "未知错误");

        // 解析 COMMAND_RESULT.data 中的命令列表（执行器返回裸数组，非 {commands: [...]} 结构）
        Object data = executorResult.getData();
        AssertUtils.notEmpty(data, "执行器能力查询返回数据为空");

        // 将原始数据序列化为 JSON 字符串后直接解析为命令列表
        String dataJson = JsonUtils.toJsonStr(data);
        List<CapabilityResultDto.CommandItem> capabilities = JsonUtils.toList(dataJson, CapabilityResultDto.CommandItem.class);
        AssertUtils.notEmpty(capabilities, "执行器能力查询结果解析失败");

        return capabilities;
    }

    /**
     * 从 sendSyncMsg 返回值中提取 ExecutorCommandResultResponse
     * <p>sendSyncMsg 返回的是回执中 data 字段的值，即 SepMessage JSON 对象。
     * 需要将其反序列化为 SepMessage 后提取 payload 再转为 ExecutorCommandResultResponse。</p>
     *
     * @param result sendSyncMsg 返回值（JSONObject/Map）
     * @return 执行器命令结果响应
     */
    private ExecutorCommandResultResponse extractExecutorResult(Object result) {
        AssertUtils.notEmpty(result, "执行器返回数据为空");
        try {
            String resultJson = JsonUtils.toJsonStr(result);
            SepMessage<?> replyMessage = JsonUtils.toJsonObj(resultJson, SepMessage.class);
            Object payload = replyMessage.getPayload();
            AssertUtils.notEmpty(payload, "执行器返回 payload 为空");
            String payloadJson = JsonUtils.toJsonStr(payload);
            return JsonUtils.toJsonObj(payloadJson, ExecutorCommandResultResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("解析执行器返回数据失败", e);
        }
    }

    /**
     * 将执行器能力命令 upsert 到本地 atomic_command 表。
     * <p>按命令编码（command）匹配已有记录：存在则更新名称、作用、状态；
     * 不存在则新增。使用批量操作避免逐条数据库交互。</p>
     *
     * @param capabilities 执行器能力命令列表
     */
    private void upsertCapabilityCommands(List<CapabilityResultDto.CommandItem> capabilities) {
        if (capabilities == null || capabilities.isEmpty()) {
            return;
        }

        transactionTemplate.executeWithoutResult(status -> {

            // 逐条 upsert：按命令编码匹配已有记录
            for (CapabilityResultDto.CommandItem item : capabilities) {
                FindOneAtomicCommandRequest queryReq = new FindOneAtomicCommandRequest().setCommand(item.getCode());

                AtomicCommand existing = atomicCommandView.findOne(queryReq, new FindOneAtomicCommandRequest());
                if (existing != null) {

                    // 更新已有命令的名称、作用、状态
                    existing.setName(item.getName());
                    existing.setRole(item.getDescription());
                    existing.setStatus(Status.ON);
                    atomicCommandView.updateById(existing);
                } else {

                    // 新增命令记录
                    AtomicCommand newCommand = new AtomicCommand();
                    newCommand.setName(item.getName());
                    newCommand.setCommand(item.getCode());
                    newCommand.setRole(item.getDescription());
                    newCommand.setStatus(Status.ON);
                    newCommand.setRemark("从执行器 system.capability 同步");
                    atomicCommandView.save(newCommand);
                }
            }
        });
    }

    /**
     * 创建会话实体。
     *
     * @param agentDefinition 智能体实体
     * @param modelId         模型主键
     * @param clientId        客户端主键
     * @param snapshotJson    上下文快照 JSON
     * @return 会话实体
     */
    private AgentChatSession createSessionEntity(AgentDefinition agentDefinition, String modelId, String clientId, String snapshotJson) {
        AgentChatSession session = new AgentChatSession();
        session.setAgentId(agentDefinition.getId());
        session.setSessionName("新对话");
        session.setLastMessageAt(new Date());
        session.setStatus(Status.ON);
        session.setReserve(snapshotJson != null ? snapshotJson : "");
        session.setRemark("智能体人机对话会话");

        // 从登录上下文获取当前用户ID并设置会话归属
        String currentUserId = LoginUserUtils.getUserTemporary().getUserId();
        AssertUtils.notEmpty(currentUserId, "当前登录用户身份为空");
        session.setCreateUserId(currentUserId);
        session.setUserId(currentUserId);

        // 保存会话级模型和客户端配置
        session.setModelId(modelId);
        session.setClientId(clientId);
        return session;
    }

    /**
     * 保存用户消息。
     *
     * @param request 发送消息请求
     * @return 锁定后的会话
     */
    private AgentChatSession saveUserMessage(SendAgentChatMessageRequest request) {
        return transactionTemplate.execute(status -> {

            // 锁定会话以保证同一会话内的消息序号连续递增
            AgentChatSession session = agentChatSessionView.findByIdForUpdate(request.getSessionId());
            AssertUtils.notEmpty(session, "会话[{}]不存在", request.getSessionId());
            AssertUtils.isTrue(Status.ON.equals(session.getStatus()), "会话[{}]不可用", request.getSessionId());

            // 保存用户原始文本消息
            Long sequenceNo = nextSequenceNo(session.getId());
            AgentChatMessage message = buildUserMessage(session.getId(), request.getContent(), sequenceNo);
            agentChatMessageView.save(message);

            // 首条用户消息作为会话名称，并更新时间用于历史会话排序
            updateSessionAfterMessage(session, request.getContent());
            return session;
        });
    }

    /**
     * 安全调用既有命令调度服务。
     *
     * @param session        会话实体
     * @param request        发送消息请求
     * @param runtimeContext 聊天运行时上下文
     * @param eventConsumer  SSE 事件消费者
     * @return 调度响应
     */
    private CommandDispatchResponse dispatchAgentSafely(AgentChatSession session, SendAgentChatMessageRequest request, AgentChatRuntimeContext runtimeContext,
                                                        Consumer<CommandDispatchProgressEvent> eventConsumer) {
        try {
            return dispatchAgent(session, request, runtimeContext, eventConsumer);
        } catch (RuntimeException e) {

            // 调度服务在创建任务前异常时仍生成失败回复，保证聊天消息链路完整
            return buildDispatchFailureResponse(e);
        }
    }

    /**
     * 调用既有命令调度服务。
     *
     * <p>显式传递运行时上下文，避免依赖 ThreadLocal 导致异步线程丢失上下文。</p>
     *
     * @param session        会话实体
     * @param request        发送消息请求
     * @param runtimeContext 聊天运行时上下文
     * @param eventConsumer  调度事件消费者（内部使用）
     * @return 调度响应
     */
    private CommandDispatchResponse dispatchAgent(AgentChatSession session, SendAgentChatMessageRequest request, AgentChatRuntimeContext runtimeContext,
                                                  Consumer<CommandDispatchProgressEvent> eventConsumer) {
        CommandDispatchRequest dispatchRequest = new CommandDispatchRequest();
        dispatchRequest.setAgentId(session.getAgentId());
        dispatchRequest.setCommandName("人机对话");
        dispatchRequest.setCommandContent(request.getContent());
        dispatchRequest.setSessionId(session.getId());

        // 从会话实体获取模型和客户端配置，避免前端重复传递
        dispatchRequest.setModelId(session.getModelId());
        dispatchRequest.setClientId(session.getClientId());

        // 从会话实体获取用户ID，避免 ThreadLocal 在异步线程中丢失
        dispatchRequest.setUserId(session.getUserId());
        return commandDispatchService.dispatchStream(dispatchRequest, runtimeContext, eventConsumer);
    }

    /**
     * 构建调度异常响应。
     *
     * @param exception 调度异常
     * @return 失败调度响应
     */
    private CommandDispatchResponse buildDispatchFailureResponse(RuntimeException exception) {
        CommandDispatchResponse response = new CommandDispatchResponse();
        response.setTaskId("");
        response.setExecStatus(AgentExecutionStatusProcess.FAILED);
        response.setResponseContent("");
        response.setFailureReason(resolveDispatchFailureReason(exception));
        return response;
    }

    /**
     * 解析调度异常原因。
     *
     * @param exception 调度异常
     * @return 用户可见失败原因
     */
    private String resolveDispatchFailureReason(RuntimeException exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank() ? "智能体对话调度失败" : message;
    }

    /**
     * 保存最终 AI 消息。
     *
     * @param session       会话实体
     * @param response      调度响应
     * @param eventConsumer SSE 事件消费者
     */
    private String saveFinalMessage(AgentChatSession session, CommandDispatchResponse response, Consumer<ChatSseEvent> eventConsumer) {
        return transactionTemplate.execute(status -> {

            // 重新锁定会话并分配最终消息序号
            AgentChatSession lockedSession = agentChatSessionView.findByIdForUpdate(session.getId());
            AssertUtils.notEmpty(lockedSession, "会话[{}]不存在", session.getId());
            Long sequenceNo = nextSequenceNo(lockedSession.getId());
            AgentChatMessage message = buildFinalMessage(lockedSession.getId(), response, sequenceNo);
            agentChatMessageView.save(message);

            // 更新会话最后消息时间
            updateSessionAfterMessage(lockedSession, message.getContent());
            publishFinalEvent(eventConsumer, lockedSession.getId(), message, response);
            return message.getId();
        });
    }

    /**
     * 获取下一条会话消息序号。
     *
     * @param sessionId 会话主键
     * @return 下一序号
     */
    private Long nextSequenceNo(String sessionId) {
        Long maxSequenceNo = agentChatMessageView.findMaxSequenceNo(sessionId);
        return maxSequenceNo + 1;
    }

    /**
     * 创建用户消息实体。
     *
     * @param sessionId  会话主键
     * @param content    用户文本
     * @param sequenceNo 消息序号
     * @return 用户消息
     */
    private AgentChatMessage buildUserMessage(String sessionId, String content, Long sequenceNo) {
        AgentChatMessage message = new AgentChatMessage();
        message.setSessionId(sessionId);
        message.setTaskId("");
        message.setRole(AgentChatMessageRoleProcess.USER);
        message.setContent(content);
        message.setContentFormat(AgentChatMessageFormatProcess.PLAIN_TEXT);
        message.setSequenceNo(sequenceNo);
        message.setStatus(Status.ON);
        message.setReserve("");
        message.setRemark("用户聊天消息");
        return message;
    }

    /**
     * 创建最终消息实体。
     *
     * @param sessionId  会话主键
     * @param response   调度响应
     * @param sequenceNo 消息序号
     * @return 最终消息
     */
    private AgentChatMessage buildFinalMessage(String sessionId, CommandDispatchResponse response, Long sequenceNo) {
        boolean success = AgentExecutionStatusProcess.SUCCESS.equals(response.getExecStatus());
        String content = success ? normalizeRestrictedMarkdown(response.getResponseContent()) : response.getFailureReason();

        // 思考内容：优先取 response 已汇总内容（为空则兜底空字符串，后续 SDK reasoning 功能接入后自动生效）
        String thinkingContent = response.getThinkingContent() != null ? response.getThinkingContent() : "";
        AgentChatMessageFormatProcess thinkingFormat = response.getThinkingContentFormat() != null ? response.getThinkingContentFormat() : AgentChatMessageFormatProcess.PLAIN_TEXT;

        AgentChatMessage message = new AgentChatMessage();
        message.setSessionId(sessionId);
        message.setTaskId(response.getTaskId());
        message.setRole(resolveFinalMessageRole(success));
        message.setContent(content);
        message.setContentFormat(resolveFinalMessageFormat(success));
        message.setThinkingContent(thinkingContent);
        message.setThinkingContentFormat(thinkingFormat);
        message.setSequenceNo(sequenceNo);
        message.setProviderId(response.getProviderId());
        message.setProviderName(response.getProviderName());
        message.setModelId(response.getModelId());
        message.setModelCode(response.getModelCode());
        message.setStatus(Status.ON);
        message.setReserve("");
        message.setRemark("智能体最终回复消息");
        return message;
    }

    /**
     * 归一化受限 Markdown。
     *
     * @param content 模型最终内容
     * @return 不含 HTML 的受限 Markdown
     */
    private String normalizeRestrictedMarkdown(String content) {

        // 模型未返回文本时降级为安全纯文本提示
        if (content == null || content.isBlank()) {
            return "未获取到有效回复。";
        }

        // HTML 标记出现时整体转义，避免剥离标签后保留脚本正文等不可信内容
        if (HTML_TAG_PATTERN.matcher(content).find()) {
            return escapeHtmlMarkup(content);
        }
        return content;
    }

    /**
     * 转义 Markdown 内容中的 HTML 标记。
     *
     * @param content 原始模型内容
     * @return 可安全按纯文本展示的内容
     */
    private String escapeHtmlMarkup(String content) {
        return content.replace("&", "\u0026amp;").replace("<", "\u0026lt;").replace(">", "\u0026gt;");
    }

    /**
     * 解析最终消息角色。
     *
     * @param success 调度是否成功
     * @return 消息角色
     */
    private AgentChatMessageRoleProcess resolveFinalMessageRole(boolean success) {
        return success ? AgentChatMessageRoleProcess.ASSISTANT : AgentChatMessageRoleProcess.SYSTEM_ERROR;
    }

    /**
     * 解析最终消息格式。
     *
     * @param success 调度是否成功
     * @return 内容格式
     */
    private AgentChatMessageFormatProcess resolveFinalMessageFormat(boolean success) {
        return success ? AgentChatMessageFormatProcess.RESTRICTED_MARKDOWN : AgentChatMessageFormatProcess.PLAIN_TEXT;
    }

    /**
     * 更新会话最后消息信息。
     *
     * @param session 会话实体
     * @param content 消息内容
     */
    private void updateSessionAfterMessage(AgentChatSession session, String content) {
        session.setLastMessageAt(new Date());

        // 新会话使用首条用户消息生成简短名称
        if ("新对话".equals(session.getSessionName())) {
            session.setSessionName(resolveSessionName(content));
        }
        agentChatSessionView.updateById(session);
    }

    /**
     * 解析会话名称。
     *
     * @param content 首条消息内容
     * @return 截断后的会话名称
     */
    private String resolveSessionName(String content) {
        String normalizedContent = content.replaceAll("\\s+", " ").trim();
        return normalizedContent.length() <= 30 ? normalizedContent : normalizedContent.substring(0, 30) + "\u2026";
    }

    /**
     * 发布最终消息事件。
     *
     * @param eventConsumer SSE 事件消费者
     * @param sessionId     会话主键
     * @param message       最终消息
     * @param response      调度响应
     */
    private void publishFinalEvent(Consumer<ChatSseEvent> eventConsumer, String sessionId, AgentChatMessage message, CommandDispatchResponse response) {
        boolean success = AgentExecutionStatusProcess.SUCCESS.equals(response.getExecStatus());

        // 成功时发送 FINAL 事件，失败时发送 ERROR 事件
        if (success) {
            sendSseEvent(eventConsumer, ChatSseEvent.builder()
                                                    .type(ChatSseEvent.Types.FINAL)
                                                    .messageId(message.getId())
                                                    .data(message.getContent())
                                                    .thinkingSummary(message.getThinkingContent())
                                                    .completed(true)
                                                    .build());
        } else {
            sendSseEvent(eventConsumer, ChatSseEvent.builder().type(ChatSseEvent.Types.ERROR).errorReason(response.getFailureReason()).completed(true).build());
        }
    }

    /**
     * 发布聊天事件。
     * <p>内部事件统一映射为 PROGRESS 后输出 SSE。</p>
     *
     * @param eventConsumer SSE 事件消费者
     * @param sessionId     会话主键
     * @param eventType     事件类型
     * @param message       事件说明
     * @param taskId        任务主键
     * @param payload       事件载荷
     * @param completed     是否完成
     * @param failureReason 失败原因
     */
    private void publishChatEvent(Consumer<ChatSseEvent> eventConsumer, String sessionId, String eventType, String message, String taskId, String payload, boolean completed,
                                  String failureReason) {

        // 客户端未订阅时无需构建事件
        if (eventConsumer == null) {
            return;
        }

        // 内部事件统一映射为稳定 SSE 事件
        ChatSseEvent sseEvent = mapInternalEventToSseEvent(eventType, message, taskId, payload, completed, failureReason);
        sendSseEvent(eventConsumer, sseEvent);
    }

    /**
     * 将内部事件映射为稳定 SSE 事件。
     *
     * @param eventType     内部事件类型
     * @param message       事件说明
     * @param taskId        任务主键
     * @param payload       事件载荷
     * @param completed     是否完成
     * @param failureReason 失败原因
     * @return 稳定 SSE 事件
     */
    private ChatSseEvent mapInternalEventToSseEvent(String eventType, String message, String taskId, String payload, boolean completed, String failureReason) {

        // MESSAGE_ACCEPTED 映射为 PROGRESS
        if ("MESSAGE_ACCEPTED".equals(eventType)) {
            return ChatSseEvent.builder().type(ChatSseEvent.Types.PROGRESS).taskId(taskId).data(message).completed(false).build();
        }

        // AI_TOKEN 映射为 REPLY（AI 流式回复内容）
        if ("AI_TOKEN".equals(eventType)) {
            return ChatSseEvent.builder().type(ChatSseEvent.Types.REPLY).taskId(taskId).data(payload).completed(false).build();
        }

        // AI_THINKING_TOKEN 映射为 THINKING（AI 流式思考内容）
        if ("AI_THINKING_TOKEN".equals(eventType)) {
            return ChatSseEvent.builder().type(ChatSseEvent.Types.THINKING).taskId(taskId).data(payload).completed(false).build();
        }

        // MESSAGE_COMPLETED 映射为 FINAL（由 publishFinalEvent 处理，此处兜底）
        if ("MESSAGE_COMPLETED".equals(eventType)) {
            return ChatSseEvent.builder().type(ChatSseEvent.Types.FINAL).taskId(taskId).data(payload).completed(true).build();
        }

        // CHAT_FAILED 映射为 ERROR
        if ("CHAT_FAILED".equals(eventType)) {
            return ChatSseEvent.builder().type(ChatSseEvent.Types.ERROR).taskId(taskId).errorReason(failureReason != null ? failureReason : message).completed(true).build();
        }

        // 其他内部事件（CONTEXT_LOADING、AI_STARTED、TOOL_CALLING 等）均映射为 PROGRESS
        return ChatSseEvent.builder().type(ChatSseEvent.Types.PROGRESS).taskId(taskId).data(message).completed(completed).build();
    }

    /**
     * 将 CommandDispatchProgressEvent 映射为 ChatSseEvent。
     * <p>用于 buildCompositeConsumer 中透传事件时的类型转换。</p>
     *
     * @param event 调度进度事件
     * @return 稳定 SSE 事件
     */
    private ChatSseEvent mapToSseEvent(CommandDispatchProgressEvent event) {
        return mapInternalEventToSseEvent(event.getEventType(), event.getMessage(), event.getTaskId(), event.getPayload(), Boolean.TRUE.equals(event.getCompleted()),
                                          event.getFailureReason());
    }

    /**
     * 发送 SSE 事件到浏览器。
     *
     * @param eventConsumer SSE 事件消费者
     * @param event         聊天 SSE 事件
     */
    private void sendSseEvent(Consumer<ChatSseEvent> eventConsumer, ChatSseEvent event) {
        if (eventConsumer == null) {
            return;
        }
        try {
            eventConsumer.accept(event);
        } catch (RuntimeException ignored) {

            // 客户端断开只终止事件投递，最终消息仍须完成持久化
        }
    }

    /**
     * 构建会话响应。
     *
     * @param session         会话实体
     * @param agentDefinition 智能体实体
     * @return 会话响应
     */
    private AgentChatSessionResponse buildSessionResponse(AgentChatSession session, AgentDefinition agentDefinition) {
        AgentChatSessionResponse response = new AgentChatSessionResponse();
        response.setId(session.getId());
        response.setAgentId(session.getAgentId());
        response.setAgentName(agentDefinition.getName());
        response.setSessionName(session.getSessionName());
        response.setLastMessageAt(session.getLastMessageAt());
        response.setModelId(session.getModelId());
        response.setClientId(session.getClientId());
        response.setCreateTime(session.getCreateTime());
        return response;
    }

    /**
     * 构建会话响应列表。
     *
     * @param sessions        会话列表
     * @param agentDefinition 智能体实体
     * @return 会话响应列表
     */
    private List<AgentChatSessionResponse> buildSessionResponses(List<AgentChatSession> sessions, AgentDefinition agentDefinition) {
        List<AgentChatSessionResponse> responses = new ArrayList<>();

        // 将每个持久化会话转换为前端展示数据
        for (AgentChatSession session : sessions) {
            responses.add(buildSessionResponse(session, agentDefinition));
        }
        return responses;
    }

    /**
     * 构建消息响应列表，包含执行事件。
     *
     * @param messages 消息列表
     * @return 消息响应列表
     */
    private List<AgentChatMessageResponse> buildMessageResponses(List<AgentChatMessage> messages) {
        List<AgentChatMessageResponse> responses = new ArrayList<>();

        // 收集所有非空任务ID用于批量查询执行事件
        List<String> taskIds = new ArrayList<>();
        for (AgentChatMessage message : messages) {
            if (message.getTaskId() != null && !message.getTaskId().isBlank()) {
                taskIds.add(message.getTaskId());
            }
        }

        // 批量加载执行事件并按任务ID分组
        Map<String, List<ExecutionEvent>> eventsByTaskId = loadExecutionEventsByTaskIds(taskIds);

        // 保持数据库已排序的会话消息顺序，组装响应
        for (AgentChatMessage message : messages) {
            AgentChatMessageResponse response = new AgentChatMessageResponse();
            response.setId(message.getId());
            response.setTaskId(message.getTaskId());
            response.setTurnId(message.getTurnId());
            response.setRole(message.getRole());
            response.setContent(message.getContent());
            response.setContentFormat(message.getContentFormat());
            response.setThinkingContent(message.getThinkingContent());
            response.setThinkingContentFormat(message.getThinkingContentFormat());
            response.setSequenceNo(message.getSequenceNo());
            response.setProviderName(message.getProviderName());
            response.setModelCode(message.getModelCode());
            response.setCreateTime(message.getCreateTime());

            // 填充该消息关联的执行事件
            String taskId = message.getTaskId();
            if (taskId != null && !taskId.isBlank()) {
                List<ExecutionEvent> events = eventsByTaskId.getOrDefault(taskId, Collections.emptyList());
                response.setExecutionEvents(mapToExecutionEventDtos(events));
            }
            responses.add(response);
        }
        return responses;
    }

    /**
     * 按任务主键批量加载执行事件，并按任务ID分组。
     *
     * @param taskIds 任务主键列表
     * @return 任务ID到执行事件列表的映射
     */
    private Map<String, List<ExecutionEvent>> loadExecutionEventsByTaskIds(List<String> taskIds) {

        // 无任务时返回空映射
        if (taskIds.isEmpty()) {
            return Collections.emptyMap();
        }

        // 批量查询执行事件
        List<ExecutionEvent> allEvents = executionEventView.findAllByTaskIds(taskIds);
        Map<String, List<ExecutionEvent>> eventsByTaskId = new HashMap<>();

        // 按任务ID分组
        for (ExecutionEvent event : allEvents) {
            String taskId = event.getTaskId();
            if (taskId != null && !taskId.isBlank()) {
                eventsByTaskId.computeIfAbsent(taskId, k -> new ArrayList<>()).add(event);
            }
        }
        return eventsByTaskId;
    }

    /**
     * 将 ExecutionEvent 实体列表映射为前端 DTO 列表。
     *
     * @param events 执行事件列表
     * @return 前端 DTO 列表
     */
    private List<AgentChatExecutionEventDto> mapToExecutionEventDtos(List<ExecutionEvent> events) {
        List<AgentChatExecutionEventDto> dtos = new ArrayList<>();

        // 遍历事件列表转换字段
        for (ExecutionEvent event : events) {
            AgentChatExecutionEventDto dto = new AgentChatExecutionEventDto();
            dto.setId(event.getId());
            dto.setEventType(event.getEventType());
            dto.setStepName(event.getStepName());
            dto.setCommandName(event.getCommandName());
            dto.setResponseContent(event.getResponseContent());
            dto.setFailureReason(event.getFailureReason());
            dto.setSequenceNo(event.getSequenceNo());
            dto.setStartedAt(event.getStartedAt());
            dto.setFinishedAt(event.getFinishedAt());
            dto.setProviderName(event.getProviderName());
            dto.setModelCode(event.getModelCode());
            dtos.add(dto);
        }
        return dtos;
    }

    @Override
    public Boolean isClientOnline(String clientId) {

        // 通过 WebSocket ChannelMap 判断客户端是否保持活跃连接
        return WebSocketUtils.isOnline("agent-executor", clientId);
    }
}