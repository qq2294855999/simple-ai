package com.simple.ai.common.service.agent;

import com.simple.ai.common.dto.agent.AgentAiRequest;
import com.simple.ai.common.dto.agent.AgentAiResponse;
import com.simple.ai.service.agentChat.AgentChatRuntimeContext;

import java.util.function.Consumer;

/**
 * 智能体 AI 调用客户端。
 *
 * @author qty
 */
public interface AgentAiClient {

    /**
     * 发送智能体 AI 调用请求。
     *
     * @param request AI 调用请求
     * @return AI 调用响应
     */
    AgentAiResponse chat(AgentAiRequest request);

    /**
     * 发送智能体 AI 流式调用请求（仅 content 流式）。
     *
     * @param request AI 调用请求
     * @param tokenConsumer content token 消费者
     * @return AI 调用响应
     */
    default AgentAiResponse chatStream(AgentAiRequest request, Consumer<String> tokenConsumer) {
        return chatStream(request, tokenConsumer, null);
    }

    /**
     * 发送智能体 AI 流式调用请求（支持 reasoning 思考内容分开发送）。
     *
     * @param request               AI 调用请求
     * @param tokenConsumer         content token 消费者（正常回复文本）
     * @param thinkingTokenConsumer reasoning/thinking token 消费者（思考过程文本），不需要可传 null
     * @return AI 调用响应
     */
    default AgentAiResponse chatStream(AgentAiRequest request, Consumer<String> tokenConsumer, Consumer<String> thinkingTokenConsumer) {
        AgentAiResponse response = chat(request);

        // 默认实现回退同步响应，兼容未支持 token 流式的 AI 客户端
        if (tokenConsumer != null && response.getResponseContent() != null) {
            tokenConsumer.accept(response.getResponseContent());
        }
        if (thinkingTokenConsumer != null && response.getThinkingContent() != null) {
            thinkingTokenConsumer.accept(response.getThinkingContent());
        }
        return response;
    }

    /**
     * 发送智能体 AI 流式调用请求（显式传递运行时上下文）。
     *
     * @param request               AI 调用请求
     * @param runtimeContext        聊天运行时上下文
     * @param tokenConsumer         content token 消费者（正常回复文本）
     * @param thinkingTokenConsumer reasoning/thinking token 消费者（思考过程文本），不需要可传 null
     * @return AI 调用响应
     */
    default AgentAiResponse chatStream(AgentAiRequest request, AgentChatRuntimeContext runtimeContext, Consumer<String> tokenConsumer, Consumer<String> thinkingTokenConsumer) {
        return chatStream(request, tokenConsumer, thinkingTokenConsumer);
    }
}