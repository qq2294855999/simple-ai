package com.simple.ai.common.service.agent;

import com.simple.ai.common.dto.agent.AgentAiRequest;
import com.simple.ai.common.dto.agent.AgentAiResponse;

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

}
