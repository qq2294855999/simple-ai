package com.simple.ai.service.agentChat;

import com.simple.ai.common.dto.agentChat.SendAgentChatMessageRequest;
import com.simple.ai.common.dto.command.CommandDispatchProgressEvent;
import com.simple.ai.common.dto.command.CommandDispatchResponse;
import com.simple.ai.common.entity.agentChatMessage.AgentChatMessage;
import com.simple.ai.common.entity.agentChatSession.AgentChatSession;
import com.simple.ai.common.service.command.CommandDispatchService;
import com.simple.ai.common.view.agentChatMessage.AgentChatMessageView;
import com.simple.ai.common.view.agentChatSession.AgentChatSessionView;
import com.simple.ai.common.enums.AgentChatMessageFormatProcess;
import com.simple.ai.common.enums.AgentChatMessageRoleProcess;
import com.simple.common.mp.common.enums.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 智能体聊天服务单元测试。
 *
 * @author qty
 */
@ExtendWith(MockitoExtension.class)
class DefaultAgentChatServiceTest {

    /** 被测聊天服务 */
    @InjectMocks
    private DefaultAgentChatService agentChatService;

    /** 会话数据访问视图 */
    @Mock
    private AgentChatSessionView agentChatSessionView;

    /** 消息数据访问视图 */
    @Mock
    private AgentChatMessageView agentChatMessageView;

    /** 命令调度服务 */
    @Mock
    private CommandDispatchService commandDispatchService;

    /** 事务模板 */
    @Mock
    private TransactionTemplate transactionTemplate;

    /** 保存消息参数捕获器 */
    @Captor
    private ArgumentCaptor<AgentChatMessage> messageCaptor;

    /**
     * 初始化不访问外部服务的事务和会话替身。
     */
    @BeforeEach
    void setUp() {
        AgentChatSession session = buildSession();

        // 在本地直接执行短事务回调，不创建数据库事务
        when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });
        doAnswer(invocation -> {
            Consumer<?> callback = invocation.getArgument(0);
            callback.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());

        // 锁定查询始终返回同一模拟会话
        when(agentChatSessionView.findByIdForUpdate(anyString())).thenReturn(session);
        when(agentChatMessageView.findMaxSequenceNo(anyString())).thenReturn(0L, 1L);
    }

    /**
     * 验证成功回复会保存受限 Markdown，HTML 会整体降级且聊天事件不会吞没调度轨迹。
     */
    @Test
    void sendStreamShouldPersistEscapedAssistantMessageAndKeepDispatchEvent() {
        CommandDispatchResponse response = buildSuccessResponse("task-local", "<script>unsafe()</script>");
        List<CommandDispatchProgressEvent> events = new ArrayList<>();
        SendAgentChatMessageRequest request = buildSendRequest();

        // 模拟调度链路向聊天通道投递一条非消息轨迹
        when(commandDispatchService.dispatchStream(any(), any())).thenAnswer(invocation -> {
            Consumer<CommandDispatchProgressEvent> consumer = invocation.getArgument(1);
            consumer.accept(buildProgressEvent("TASK_CREATED", "调度任务已创建", ""));
            return response;
        });

        agentChatService.sendStream(request, events::add);

        // 验证用户消息和最终 AI 消息均被持久化
        verify(agentChatMessageView, times(2)).save(messageCaptor.capture());
        List<AgentChatMessage> messages = messageCaptor.getAllValues();
        AgentChatMessage finalMessage = messages.get(1);
        assertEquals(AgentChatMessageRoleProcess.ASSISTANT.name(), finalMessage.getRole());
        assertEquals(AgentChatMessageFormatProcess.RESTRICTED_MARKDOWN.name(), finalMessage.getContentFormat());
        assertEquals("&lt;script&gt;unsafe()&lt;/script&gt;", finalMessage.getContent());
        assertEquals("task-local", finalMessage.getTaskId());

        // 验证聊天确认、调度轨迹和最终消息事件均按职责投递
        assertEquals("MESSAGE_ACCEPTED", events.get(0).getEventType());
        assertEquals("TASK_CREATED", events.get(1).getEventType());
        assertEquals("MESSAGE_COMPLETED", events.get(2).getEventType());
        assertEquals(finalMessage.getContent(), events.get(2).getPayload());
    }

    /**
     * 验证调度失败会生成可读取的系统失败消息和 CHAT_FAILED 事件。
     */
    @Test
    void sendStreamShouldPersistSystemErrorWhenDispatchFails() {
        CommandDispatchResponse response = new CommandDispatchResponse();
        response.setTaskId("task-local");
        response.setExecStatus("FAILED");
        response.setResponseContent("");
        response.setFailureReason("本地模拟调度失败");
        List<CommandDispatchProgressEvent> events = new ArrayList<>();

        when(commandDispatchService.dispatchStream(any(), any())).thenReturn(response);

        agentChatService.sendStream(buildSendRequest(), events::add);

        // 验证失败不会伪装为 AI 成功回复
        verify(agentChatMessageView, times(2)).save(messageCaptor.capture());
        AgentChatMessage finalMessage = messageCaptor.getAllValues().get(1);
        assertEquals(AgentChatMessageRoleProcess.SYSTEM_ERROR.name(), finalMessage.getRole());
        assertEquals(AgentChatMessageFormatProcess.PLAIN_TEXT.name(), finalMessage.getContentFormat());
        assertEquals("本地模拟调度失败", finalMessage.getContent());
        assertEquals("CHAT_FAILED", events.get(1).getEventType());
        assertEquals("本地模拟调度失败", events.get(1).getFailureReason());
    }

    /**
     * 构建模拟会话。
     *
     * @return 已启用会话
     */
    private AgentChatSession buildSession() {
        AgentChatSession session = new AgentChatSession();
        session.setId("session-local");
        session.setAgentId("agent-local");
        session.setSessionName("新对话");
        ReflectionTestUtils.setField(session, "status", Status.ON);
        return session;
    }

    /**
     * 构建发送请求。
     *
     * @return 本地测试请求
     */
    private SendAgentChatMessageRequest buildSendRequest() {
        SendAgentChatMessageRequest request = new SendAgentChatMessageRequest();
        request.setSessionId("session-local");
        request.setContent("请生成本地测试回复");
        return request;
    }

    /**
     * 构建成功调度响应。
     *
     * @param taskId 任务主键
     * @param content 回复内容
     * @return 成功响应
     */
    private CommandDispatchResponse buildSuccessResponse(String taskId, String content) {
        CommandDispatchResponse response = new CommandDispatchResponse();
        response.setTaskId(taskId);
        response.setExecStatus("SUCCESS");
        response.setResponseContent(content);
        response.setFailureReason("");
        return response;
    }

    /**
     * 构建模拟轨迹事件。
     *
     * @param eventType 事件类型
     * @param message 事件说明
     * @param payload 事件载荷
     * @return 模拟事件
     */
    private CommandDispatchProgressEvent buildProgressEvent(String eventType, String message, String payload) {
        CommandDispatchProgressEvent event = new CommandDispatchProgressEvent();
        event.setEventType(eventType);
        event.setMessage(message);
        event.setPayload(payload);
        return event;
    }
}
