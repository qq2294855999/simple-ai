package com.simple.ai.controller.agentChat;

import com.simple.ai.common.dto.agentChat.AgentChatMessageResponse;
import com.simple.ai.common.dto.agentChat.AgentChatSessionResponse;
import com.simple.ai.common.dto.agentChat.CreateAgentChatSessionRequest;
import com.simple.ai.common.dto.agentChat.SendAgentChatMessageRequest;
import com.simple.ai.common.service.agentChat.AgentChatService;
import com.simple.common.core.response.R;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 智能体聊天控制器接口契约测试。
 *
 * @author qty
 */
@ExtendWith(MockitoExtension.class)
class AgentChatControllerContractTest {

    /** 被测控制器 */
    private AgentChatController controller;

    /** 聊天服务替身 */
    @Mock
    private AgentChatService agentChatService;

    /** 同步执行器替身 */
    @Mock
    private TaskExecutor taskExecutor;

    /** 流式请求捕获器 */
    @Captor
    private ArgumentCaptor<SendAgentChatMessageRequest> requestCaptor;

    /**
     * 初始化控制器并将异步任务改为同步执行。
     */
    @BeforeEach
    void setUp() {
        controller = new AgentChatController();
        ReflectionTestUtils.setField(controller, "agentChatService", agentChatService);
        ReflectionTestUtils.setField(controller, "taskExecutor", taskExecutor);

    }

    /**
     * 验证创建会话会原样委托请求并按统一响应返回服务结果。
     */
    @Test
    void createSessionShouldDelegateRequestAndWrapServiceResponse() {
        CreateAgentChatSessionRequest request = new CreateAgentChatSessionRequest();
        request.setAgentId("agent-local");
        AgentChatSessionResponse response = new AgentChatSessionResponse();
        response.setId("session-local");
        when(agentChatService.createSession(request)).thenReturn(response);

        R<AgentChatSessionResponse> actual = controller.createSession(request);

        assertSame(response, actual.getData());
        verify(agentChatService).createSession(request);
    }

    /**
     * 验证会话列表和历史消息接口分别按真实路径参数委托服务。
     */
    @Test
    void findSessionsAndMessagesShouldDelegatePathParameters() {
        AgentChatSessionResponse session = new AgentChatSessionResponse();
        session.setId("session-local");
        AgentChatMessageResponse message = new AgentChatMessageResponse();
        message.setId("message-local");
        List<AgentChatSessionResponse> sessions = List.of(session);
        List<AgentChatMessageResponse> messages = List.of(message);
        when(agentChatService.findSessions("agent-local")).thenReturn(sessions);
        when(agentChatService.findMessages("session-local")).thenReturn(messages);

        R<List<AgentChatSessionResponse>> sessionResult = controller.findSessions("agent-local");
        R<List<AgentChatMessageResponse>> messageResult = controller.findMessages("session-local");

        assertSame(sessions, sessionResult.getData());
        assertSame(messages, messageResult.getData());
        verify(agentChatService).findSessions("agent-local");
        verify(agentChatService).findMessages("session-local");
    }

    /**
     * 验证 SSE 入口立即返回有限超时输出器，并只将请求委托给一次服务调用。
     */
    @Test
    void sendStreamShouldReturnTimedEmitterAndDelegateToService() {
        SendAgentChatMessageRequest request = new SendAgentChatMessageRequest();
        request.setSessionId("session-local");
        request.setContent("本地聊天测试消息");

        // 在测试线程直接运行异步任务，避免创建实际线程或连接外部服务
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(taskExecutor).execute(any(Runnable.class));

        SseEmitter emitter = assertTimeoutPreemptively(java.time.Duration.ofSeconds(1), () -> controller.sendStream(request));

        assertEquals(300000L, emitter.getTimeout());
        verify(taskExecutor).execute(any(Runnable.class));
        verify(agentChatService, times(1)).sendStream(requestCaptor.capture(), any());
        assertSame(request, requestCaptor.getValue());
    }
}
