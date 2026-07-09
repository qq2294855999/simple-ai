package com.simple.ai.common.service.agent;

import com.simple.ai.common.dto.agent.AgentAiRequest;
import com.simple.ai.common.dto.agent.AgentAiResponse;

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
     * 发送智能体 AI 流式调用请求。
     *
     * @param request AI 调用请求
     * @param tokenConsumer token 内容消费者
     * @return AI 调用响应
     */
    default AgentAiResponse chatStream(AgentAiRequest request, Consumer<String> tokenConsumer) {
        AgentAiResponse response = chat(request);

        // 默认实现回退同步响应，兼容未支持 token 流式的 AI 客户端
        if (tokenConsumer != null && response.getResponseContent() != null) {
            tokenConsumer.accept(response.getResponseContent());
        }
        return response;
    }

}
