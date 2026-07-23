package com.simple.ai.service.agentChat;

import com.simple.ai.common.dto.agentChat.*;
import com.simple.ai.common.dto.command.CommandDispatchProgressEvent;
import com.simple.ai.common.dto.command.CommandDispatchRequest;
import com.simple.ai.common.dto.command.CommandDispatchResponse;
import com.simple.ai.common.entity.agentChatMessage.AgentChatMessage;
import com.simple.ai.common.entity.agentChatSession.AgentChatSession;
import com.simple.ai.common.entity.agentDefinition.AgentDefinition;
import com.simple.ai.common.entity.executionEvent.ExecutionEvent;
import com.simple.ai.common.entity.task.Task;
import com.simple.ai.common.entity.taskDetail.TaskDetail;
import com.simple.ai.common.enums.AgentChatMessageFormatProcess;
import com.simple.ai.common.enums.AgentChatMessageRoleProcess;
import com.simple.ai.common.enums.AgentExecutionStatusProcess;
import com.simple.ai.common.service.agentChat.AgentChatService;
import com.simple.ai.common.service.chatTurn.ChatTurnService;
import com.simple.ai.common.service.command.CommandDispatchService;
import com.simple.ai.common.service.executionEvent.AgentMemoryDistiller;
import com.simple.ai.common.service.executionEvent.ExecutionEventBus;
import com.simple.ai.common.service.session.AgentSessionService;
import com.simple.ai.common.view.agentChatMessage.AgentChatMessageView;
import com.simple.ai.common.view.agentChatSession.AgentChatSessionView;
import com.simple.ai.common.view.agentDefinition.AgentDefinitionView;
import com.simple.ai.common.view.chatTurn.ChatTurnView;
import com.simple.ai.common.view.executionEvent.ExecutionEventView;
import com.simple.ai.common.view.task.TaskView;
import com.simple.ai.common.view.taskDetail.TaskDetailView;
import com.simple.common.auth.client.util.LoginUserUtils;
import com.simple.common.core.common.service.lock.LockService;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.mp.common.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.*;
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
     * 智能体会话服务
     */
    @Autowired
    private AgentSessionService agentSessionService;

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
     * 智能体记忆蒸馏器，在每轮对话完成后提炼执行轨迹为记忆证据
     */
    @Autowired
    private AgentMemoryDistiller agentMemoryDistiller;

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

        // 创建持久化会话，保存完整上下文配置
        AgentChatSession session = createSessionEntity(agentDefinition, request.getModelId(), request.getClientId());
        transactionTemplate.executeWithoutResult(status -> agentChatSessionView.save(session));
        return buildSessionResponse(session, agentDefinition);
    }

    @Override
    public List<AgentChatSessionResponse> findSessions(String agentId) {
        AssertUtils.notEmpty(agentId, "智能体主键不能为空");

        // 校验智能体存在后加载该智能体历史会话
        AgentDefinition agentDefinition = loadEnabledAgent(agentId);
        List<AgentChatSession> sessions = agentChatSessionView.findAllByAgentId(agentId);
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
    public void sendStream(SendAgentChatMessageRequest request, Consumer<CommandDispatchProgressEvent> eventConsumer) {
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
     * @param eventConsumer 进度事件消费者
     */
    private void sendStreamInternal(SendAgentChatMessageRequest request, Consumer<CommandDispatchProgressEvent> eventConsumer) {

        // 在短事务内锁定会话并持久化用户消息，避免模型执行期间占用数据库锁
        AgentChatSession session = saveUserMessage(request);

        // 创建本轮对话轮次记录
        String turnId = startChatTurn(session, request);

        // 构建组合消费者：将调度事件同时写入执行轨迹并透传给SSE通道
        Consumer<CommandDispatchProgressEvent> compositeConsumer = buildCompositeConsumer(turnId, eventConsumer);

        // 通知客户端用户消息已被服务端接收
        publishChatEvent(eventConsumer, session.getId(), "MESSAGE_ACCEPTED", "用户消息已保存", "", "", false, "");

        // 基于原有命令调度核心执行 AI 与智能体流程
        CommandDispatchResponse response = dispatchAgentSafely(session, request, compositeConsumer);

        // 在独立短事务内持久化最终消息，保证流式收尾与数据库审计闭环
        String assistantMessageId = saveFinalMessage(session, response, eventConsumer);

        // 完成本轮对话，关联AI回复消息
        chatTurnService.completeTurn(turnId, assistantMessageId, "");

        // 异步触发记忆蒸馏：收集执行事件并创建记忆证据
        triggerDistillation(turnId, response.getTaskId(), session.getAgentId(), request.getContent());
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
     * 触发记忆蒸馏，将本轮执行轨迹提炼为记忆证据。
     * <p>蒸馏过程在独立事务中执行，失败不影响主流程。</p>
     *
     * @param turnId         对话轮次主键
     * @param taskId         调度任务主键
     * @param agentId        智能体主键
     * @param commandContent 用户命令内容
     */
    private void triggerDistillation(String turnId, String taskId, String agentId, String commandContent) {

        // 轮次为空时跳过蒸馏
        if (turnId == null || turnId.isBlank()) {
            return;
        }
        try {
            agentMemoryDistiller.distill(turnId, taskId, agentId, commandContent);
        } catch (RuntimeException e) {

            // 蒸馏失败不影响聊天主流程
            log.warn("记忆蒸馏失败，turnId={}", turnId, e);
        }
    }

    /**
     * 构建组合事件消费者，在透传事件的同时记录执行轨迹。
     * <p>MESSAGE_* 等聊天层事件由 ExecutionEventBus 内部白名单过滤，不会误录入执行轨迹。</p>
     *
     * @param turnId        对话轮次主键
     * @param eventConsumer 原始事件消费者（SSE 通道）
     * @return 组合消费者
     */
    private Consumer<CommandDispatchProgressEvent> buildCompositeConsumer(String turnId, Consumer<CommandDispatchProgressEvent> eventConsumer) {

        // 轮次为空时不记录执行轨迹
        if (turnId == null || turnId.isBlank()) {
            return eventConsumer;
        }

        // 组合消费：先记录到执行事件表，再透传给原始消费者
        return event -> {
            try {
                executionEventBus.recordEvent(turnId, "", event);
            } catch (RuntimeException e) {

                // 执行轨迹落库异常不影响主流程
                log.warn("执行事件记录失败，turnId={}, eventType={}", turnId, event.getEventType(), e);
            }
            if (eventConsumer != null) {
                eventConsumer.accept(event);
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

        // 清理 Redis 中的会话缓存数据（摘要和消息历史）
        for (String sessionId : sessionIds) {
            agentSessionService.deleteBySessionId(sessionId);
        }
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
     * 创建会话实体。
     *
     * @param agentDefinition 智能体实体
     * @param modelId         模型主键
     * @param clientId        客户端主键
     * @return 会话实体
     */
    private AgentChatSession createSessionEntity(AgentDefinition agentDefinition, String modelId, String clientId) {
        AgentChatSession session = new AgentChatSession();
        session.setAgentId(agentDefinition.getId());
        session.setSessionName("新对话");
        session.setLastMessageAt(new Date());
        session.setStatus(Status.ON);
        session.setReserve("");
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
     * @param session       会话实体
     * @param request       发送消息请求
     * @param eventConsumer 事件消费者
     * @return 调度响应
     */
    private CommandDispatchResponse dispatchAgentSafely(AgentChatSession session, SendAgentChatMessageRequest request, Consumer<CommandDispatchProgressEvent> eventConsumer) {
        try {
            return dispatchAgent(session, request, eventConsumer);
        } catch (RuntimeException e) {

            // 调度服务在创建任务前异常时仍生成失败回复，保证聊天消息链路完整
            return buildDispatchFailureResponse(e);
        }
    }

    /**
     * 调用既有命令调度服务。
     *
     * <p>从会话实体获取 userId、modelId、clientId，避免依赖 ThreadLocal 导致异步线程丢失上下文。</p>
     *
     * @param session       会话实体
     * @param request       发送消息请求
     * @param eventConsumer 事件消费者
     * @return 调度响应
     */
    private CommandDispatchResponse dispatchAgent(AgentChatSession session, SendAgentChatMessageRequest request, Consumer<CommandDispatchProgressEvent> eventConsumer) {
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
        return commandDispatchService.dispatchStream(dispatchRequest, eventConsumer);
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
     * @param eventConsumer 事件消费者
     */
    private String saveFinalMessage(AgentChatSession session, CommandDispatchResponse response, Consumer<CommandDispatchProgressEvent> eventConsumer) {
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
        AgentChatMessage message = new AgentChatMessage();
        message.setSessionId(sessionId);
        message.setTaskId(response.getTaskId());
        message.setRole(resolveFinalMessageRole(success));
        message.setContent(content);
        message.setContentFormat(resolveFinalMessageFormat(success));
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
     * @param eventConsumer 事件消费者
     * @param sessionId     会话主键
     * @param message       最终消息
     * @param response      调度响应
     */
    private void publishFinalEvent(Consumer<CommandDispatchProgressEvent> eventConsumer, String sessionId, AgentChatMessage message, CommandDispatchResponse response) {
        boolean success = AgentExecutionStatusProcess.SUCCESS.equals(response.getExecStatus());
        String eventType = success ? "MESSAGE_COMPLETED" : "CHAT_FAILED";
        String eventMessage = success ? "AI 最终回复已保存" : "AI 对话执行失败";
        publishChatEvent(eventConsumer, sessionId, eventType, eventMessage, message.getTaskId(), message.getContent(), true, response.getFailureReason());
    }

    /**
     * 发布聊天事件。
     *
     * @param eventConsumer 事件消费者
     * @param sessionId     会话主键
     * @param eventType     事件类型
     * @param message       事件说明
     * @param taskId        任务主键
     * @param payload       事件载荷
     * @param completed     是否完成
     * @param failureReason 失败原因
     */
    private void publishChatEvent(Consumer<CommandDispatchProgressEvent> eventConsumer, String sessionId, String eventType, String message, String taskId, String payload, boolean completed,
                                  String failureReason) {

        // 客户端未订阅时无需构建事件
        if (eventConsumer == null) {
            return;
        }
        CommandDispatchProgressEvent event = new CommandDispatchProgressEvent();
        event.setSessionId(sessionId);
        event.setTaskId(taskId);
        event.setEventType(eventType);
        event.setStepId("");
        event.setStepName("");
        event.setExecStatus(null);
        event.setMessage(message);
        event.setPayload(payload);
        event.setCompleted(completed);
        event.setFailureReason(failureReason);
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
}