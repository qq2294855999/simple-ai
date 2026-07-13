package com.simple.ai.controller.agentChat;

import com.simple.ai.common.dto.agentChat.AgentChatMessageResponse;
import com.simple.ai.common.dto.agentChat.AgentChatSessionResponse;
import com.simple.ai.common.dto.agentChat.CreateAgentChatSessionRequest;
import com.simple.ai.common.dto.agentChat.SendAgentChatMessageRequest;
import com.simple.ai.common.dto.command.CommandDispatchProgressEvent;
import com.simple.ai.common.service.agentChat.AgentChatService;
import com.simple.common.auth.client.common.annotation.HasAuthority;
import com.simple.common.core.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * 智能体人机对话控制层。
 *
 * @author qty
 */
@Tag(name = "智能体人机对话")
@RequestMapping("sys/agent-chat")
@RestController
public class AgentChatController {

    /** 聊天 SSE 最大等待时长 */
    private static final long CHAT_STREAM_TIMEOUT_MILLIS = 300000L;

    /** 聊天服务 */
    @Autowired
    private AgentChatService agentChatService;

    /** 异步任务执行器 */
    @Autowired
    private TaskExecutor taskExecutor;

    /**
     * 创建聊天会话。
     *
     * @param request 创建会话请求
     * @return 会话响应
     */
    @PostMapping("session")
    @Operation(summary = "创建智能体聊天会话")
    @HasAuthority("sys:agent-chat:session")
    public R<AgentChatSessionResponse> createSession(@RequestBody @Valid CreateAgentChatSessionRequest request) {
        return R.ok(agentChatService.createSession(request));
    }

    /**
     * 查询智能体聊天会话。
     *
     * @param agentId 智能体主键
     * @return 会话列表
     */
    @GetMapping("session-list")
    @Operation(summary = "查询智能体聊天会话")
    @HasAuthority("sys:agent-chat:session-list")
    public R<List<AgentChatSessionResponse>> findSessions(@RequestParam String agentId) {
        return R.ok(agentChatService.findSessions(agentId));
    }

    /**
     * 查询会话历史消息。
     *
     * @param sessionId 会话主键
     * @return 消息列表
     */
    @GetMapping("message-list/{sessionId}")
    @Operation(summary = "查询智能体聊天历史消息")
    @HasAuthority("sys:agent-chat:message-list")
    public R<List<AgentChatMessageResponse>> findMessages(@PathVariable String sessionId) {
        return R.ok(agentChatService.findMessages(sessionId));
    }

    /**
     * 流式发送聊天消息。
     *
     * @param request 发送消息请求
     * @return SSE 事件输出器
     */
    @PostMapping("send-stream")
    @Operation(summary = "流式发送智能体聊天消息")
    @HasAuthority("sys:agent-chat:send-stream")
    public SseEmitter sendStream(@RequestBody @Valid SendAgentChatMessageRequest request) {
        SseEmitter emitter = new SseEmitter(CHAT_STREAM_TIMEOUT_MILLIS);

        // SSE 超时必须以错误终态关闭，避免浏览器无限等待 loading
        emitter.onTimeout(() -> emitter.completeWithError(new TimeoutException("智能体聊天响应超时")));

        try {
            // 在异步线程执行模型调用，保持请求线程可立即返回 SSE 通道
            taskExecutor.execute(() -> runChatStream(request, emitter));
        } catch (RuntimeException e) {

            // 执行器拒绝任务时立即关闭 SSE，前端将显示失败消息并结束 loading
            emitter.completeWithError(e);
        }
        return emitter;
    }

    /**
     * 执行聊天流并写出事件。
     *
     * @param request 发送消息请求
     * @param emitter SSE 输出器
     */
    private void runChatStream(SendAgentChatMessageRequest request, SseEmitter emitter) {
        try {
            agentChatService.sendStream(request, event -> sendEvent(emitter, event));
            emitter.complete();
        } catch (Exception e) {

            // 数据库或 AI 调用异常必须关闭 SSE，使客户端进入失败处理而非保持 loading
            emitter.completeWithError(e);
        }
    }

    /**
     * 写出单条 SSE 事件。
     *
     * @param emitter SSE 输出器
     * @param event 调度事件
     */
    private void sendEvent(SseEmitter emitter, CommandDispatchProgressEvent event) {
        try {
            emitter.send(SseEmitter.event().name(event.getEventType()).data(event));
        } catch (IOException e) {
            throw new IllegalStateException("发送智能体聊天事件失败", e);
        }
    }
}
