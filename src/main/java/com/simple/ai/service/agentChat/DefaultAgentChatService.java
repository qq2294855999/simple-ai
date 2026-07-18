package com.simple.ai.service.agentChat;

import com.simple.ai.common.dto.agentChat.AgentChatMessageResponse;
import com.simple.ai.common.dto.agentChat.AgentChatSessionResponse;
import com.simple.ai.common.dto.agentChat.CreateAgentChatSessionRequest;
import com.simple.ai.common.dto.agentChat.SendAgentChatMessageRequest;
import com.simple.ai.common.dto.command.CommandDispatchProgressEvent;
import com.simple.ai.common.dto.command.CommandDispatchRequest;
import com.simple.ai.common.dto.command.CommandDispatchResponse;
import com.simple.ai.common.dto.taskDetail.FindAllTaskDetailRequest;
import com.simple.ai.common.entity.agentChatMessage.AgentChatMessage;
import com.simple.ai.common.entity.agentChatSession.AgentChatSession;
import com.simple.ai.common.entity.agentDefinition.AgentDefinition;
import com.simple.ai.common.entity.task.Task;
import com.simple.ai.common.entity.taskDetail.TaskDetail;
import com.simple.ai.common.enums.AgentChatMessageFormatProcess;
import com.simple.ai.common.enums.AgentChatMessageRoleProcess;
import com.simple.ai.common.service.agentChat.AgentChatService;
import com.simple.ai.common.service.command.CommandDispatchService;
import com.simple.ai.common.service.session.AgentSessionService;
import com.simple.ai.common.view.agentChatMessage.AgentChatMessageView;
import com.simple.ai.common.view.agentChatSession.AgentChatSessionView;
import com.simple.ai.common.view.agentDefinition.AgentDefinitionView;
import com.simple.ai.common.view.task.TaskView;
import com.simple.ai.common.view.taskDetail.TaskDetailView;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.mp.common.enums.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 智能体聊天服务默认实现。
 *
 * @author qty
 */
@Service
class DefaultAgentChatService implements AgentChatService {

    /** HTML 标签匹配表达式 */
    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("(?is)<[^>]*>");

    /** 智能体定义视图 */
    @Autowired
    private AgentDefinitionView agentDefinitionView;

    /** 聊天会话视图 */
    @Autowired
    private AgentChatSessionView agentChatSessionView;

    /** 聊天消息视图 */
    @Autowired
    private AgentChatMessageView agentChatMessageView;

    /** 任务视图 */
    @Autowired
    private TaskView taskView;

    /** 任务详情视图 */
    @Autowired
    private TaskDetailView taskDetailView;

    /** 命令调度服务 */
    @Autowired
    private CommandDispatchService commandDispatchService;

    /** 智能体会话服务 */
    @Autowired
    private AgentSessionService agentSessionService;

    /** 事务模板 */
    @Autowired
    private TransactionTemplate transactionTemplate;

    @Override
    public AgentChatSessionResponse createSession(CreateAgentChatSessionRequest request) {
        AssertUtils.notEmpty(request.getAgentId(), "智能体主键不能为空");

        // 校验会话绑定的智能体存在并已启用
        AgentDefinition agentDefinition = loadEnabledAgent(request.getAgentId());

        // 创建持久化会话
        AgentChatSession session = createSessionEntity(agentDefinition);
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

        // 在短事务内锁定会话并持久化用户消息，避免模型执行期间占用数据库锁
        AgentChatSession session = saveUserMessage(request);

        // 通知客户端用户消息已被服务端接收
        publishChatEvent(eventConsumer, session.getId(), "MESSAGE_ACCEPTED", "用户消息已保存", "", "", false, "");

        // 基于原有命令调度核心执行 AI 与智能体流程
        CommandDispatchResponse response = dispatchAgentSafely(session, request, eventConsumer);

        // 在独立短事务内持久化最终消息，保证流式收尾与数据库审计闭环
        saveFinalMessage(session, response, eventConsumer);
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
     * @return 会话实体
     */
    private AgentChatSession createSessionEntity(AgentDefinition agentDefinition) {
        AgentChatSession session = new AgentChatSession();
        session.setAgentId(agentDefinition.getId());
        session.setSessionName("新对话");
        session.setLastMessageAt(new Date());
        session.setStatus(Status.ON);
        session.setReserver("");
        session.setRemark("智能体人机对话会话");
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
     * @param session 会话实体
     * @param request 发送消息请求
     * @param eventConsumer 事件消费者
     * @return 调度响应
     */
    private CommandDispatchResponse dispatchAgentSafely(AgentChatSession session, SendAgentChatMessageRequest request,
                                                         Consumer<CommandDispatchProgressEvent> eventConsumer) {
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
     * @param session 会话实体
     * @param request 发送消息请求
     * @param eventConsumer 事件消费者
     * @return 调度响应
     */
    private CommandDispatchResponse dispatchAgent(AgentChatSession session, SendAgentChatMessageRequest request,
                                                   Consumer<CommandDispatchProgressEvent> eventConsumer) {
        CommandDispatchRequest dispatchRequest = new CommandDispatchRequest();
        dispatchRequest.setAgentId(session.getAgentId());
        dispatchRequest.setCommandName("人机对话");
        dispatchRequest.setCommandContent(request.getContent());
        dispatchRequest.setSessionId(session.getId());
        dispatchRequest.setModelId(request.getModelId());
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
        response.setExecStatus("FAILED");
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
     * @param session 会话实体
     * @param response 调度响应
     * @param eventConsumer 事件消费者
     */
    private void saveFinalMessage(AgentChatSession session, CommandDispatchResponse response,
                                   Consumer<CommandDispatchProgressEvent> eventConsumer) {
        transactionTemplate.executeWithoutResult(status -> {

            // 重新锁定会话并分配最终消息序号
            AgentChatSession lockedSession = agentChatSessionView.findByIdForUpdate(session.getId());
            AssertUtils.notEmpty(lockedSession, "会话[{}]不存在", session.getId());
            Long sequenceNo = nextSequenceNo(lockedSession.getId());
            AgentChatMessage message = buildFinalMessage(lockedSession.getId(), response, sequenceNo);
            agentChatMessageView.save(message);

            // 更新会话最后消息时间
            updateSessionAfterMessage(lockedSession, message.getContent());
            publishFinalEvent(eventConsumer, lockedSession.getId(), message, response);
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
     * @param sessionId 会话主键
     * @param content 用户文本
     * @param sequenceNo 消息序号
     * @return 用户消息
     */
    private AgentChatMessage buildUserMessage(String sessionId, String content, Long sequenceNo) {
        AgentChatMessage message = new AgentChatMessage();
        message.setSessionId(sessionId);
        message.setTaskId("");
        message.setRole(AgentChatMessageRoleProcess.USER.name());
        message.setContent(content);
        message.setContentFormat(AgentChatMessageFormatProcess.PLAIN_TEXT.name());
        message.setSequenceNo(sequenceNo);
        message.setStatus(Status.ON);
        message.setReserver("");
        message.setRemark("用户聊天消息");
        return message;
    }

    /**
     * 创建最终消息实体。
     *
     * @param sessionId 会话主键
     * @param response 调度响应
     * @param sequenceNo 消息序号
     * @return 最终消息
     */
    private AgentChatMessage buildFinalMessage(String sessionId, CommandDispatchResponse response, Long sequenceNo) {
        boolean success = "SUCCESS".equals(response.getExecStatus());
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
        message.setReserver("");
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
        return content.replace("&", "\u0026amp;")
                .replace("<", "\u0026lt;")
                .replace(">", "\u0026gt;");
    }

    /**
     * 解析最终消息角色。
     *
     * @param success 调度是否成功
     * @return 消息角色
     */
    private String resolveFinalMessageRole(boolean success) {
        return success ? AgentChatMessageRoleProcess.ASSISTANT.name() : AgentChatMessageRoleProcess.SYSTEM_ERROR.name();
    }

    /**
     * 解析最终消息格式。
     *
     * @param success 调度是否成功
     * @return 内容格式
     */
    private String resolveFinalMessageFormat(boolean success) {
        return success ? AgentChatMessageFormatProcess.RESTRICTED_MARKDOWN.name()
                : AgentChatMessageFormatProcess.PLAIN_TEXT.name();
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
     * @param sessionId 会话主键
     * @param message 最终消息
     * @param response 调度响应
     */
    private void publishFinalEvent(Consumer<CommandDispatchProgressEvent> eventConsumer, String sessionId,
                                    AgentChatMessage message, CommandDispatchResponse response) {
        boolean success = "SUCCESS".equals(response.getExecStatus());
        String eventType = success ? "MESSAGE_COMPLETED" : "CHAT_FAILED";
        String eventMessage = success ? "AI 最终回复已保存" : "AI 对话执行失败";
        publishChatEvent(eventConsumer, sessionId, eventType, eventMessage, message.getTaskId(), message.getContent(), true,
                response.getFailureReason());
    }

    /**
     * 发布聊天事件。
     *
     * @param eventConsumer 事件消费者
     * @param sessionId 会话主键
     * @param eventType 事件类型
     * @param message 事件说明
     * @param taskId 任务主键
     * @param payload 事件载荷
     * @param completed 是否完成
     * @param failureReason 失败原因
     */
    private void publishChatEvent(Consumer<CommandDispatchProgressEvent> eventConsumer, String sessionId, String eventType,
                                   String message, String taskId, String payload, boolean completed, String failureReason) {

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
        event.setExecStatus("");
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
     * @param session 会话实体
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
        return response;
    }

    /**
     * 构建会话响应列表。
     *
     * @param sessions 会话列表
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
     * 构建消息响应列表。
     *
     * @param messages 消息列表
     * @return 消息响应列表
     */
    private List<AgentChatMessageResponse> buildMessageResponses(List<AgentChatMessage> messages) {
        List<AgentChatMessageResponse> responses = new ArrayList<>();

        // 保持数据库已排序的会话消息顺序
        for (AgentChatMessage message : messages) {
            AgentChatMessageResponse response = new AgentChatMessageResponse();
            response.setId(message.getId());
            response.setTaskId(message.getTaskId());
            response.setRole(message.getRole());
            response.setContent(message.getContent());
            response.setContentFormat(message.getContentFormat());
            response.setSequenceNo(message.getSequenceNo());
            response.setProviderName(message.getProviderName());
            response.setModelCode(message.getModelCode());
            response.setCreateTime(message.getCreateTime());
            responses.add(response);
        }
        return responses;
    }
}
