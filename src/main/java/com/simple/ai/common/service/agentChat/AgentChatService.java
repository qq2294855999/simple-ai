package com.simple.ai.common.service.agentChat;

import com.simple.ai.common.dto.agentChat.AgentChatMessageResponse;
import com.simple.ai.common.dto.agentChat.AgentChatSessionResponse;
import com.simple.ai.common.dto.agentChat.CreateAgentChatSessionRequest;
import com.simple.ai.common.dto.agentChat.SendAgentChatMessageRequest;
import com.simple.ai.common.dto.command.CommandDispatchProgressEvent;

import java.util.List;
import java.util.function.Consumer;

/**
 * 智能体聊天服务。
 *
 * @author qty
 */
public interface AgentChatService {

    /**
     * 创建聊天会话。
     *
     * @param request 创建会话请求
     * @return 会话响应
     */
    AgentChatSessionResponse createSession(CreateAgentChatSessionRequest request);

    /**
     * 查询智能体会话。
     *
     * @param agentId 智能体主键
     * @return 会话列表
     */
    List<AgentChatSessionResponse> findSessions(String agentId);

    /**
     * 查询会话历史消息。
     *
     * @param sessionId 会话主键
     * @return 历史消息
     */
    List<AgentChatMessageResponse> findMessages(String sessionId);

    /**
     * 流式发送聊天消息。
     *
     * @param request 发送消息请求
     * @param eventConsumer 事件消费者
     */
    void sendStream(SendAgentChatMessageRequest request, Consumer<CommandDispatchProgressEvent> eventConsumer);
}
