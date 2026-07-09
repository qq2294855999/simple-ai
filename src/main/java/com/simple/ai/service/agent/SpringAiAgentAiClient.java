package com.simple.ai.service.agent;

import com.simple.ai.common.dto.agent.AgentAiRequest;
import com.simple.ai.common.dto.agent.AgentAiResponse;
import com.simple.ai.common.service.agent.AgentAiClient;
import com.simple.common.core.utils.AssertUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Spring AI 智能体 AI 调用客户端实现。
 *
 * @author qty
 */
@Slf4j
@Service
class SpringAiAgentAiClient implements AgentAiClient {

    /**
     * Spring AI 对话客户端
     */
    @Autowired
    private ChatClient chatClient;

    @Override
    public AgentAiResponse chat(AgentAiRequest request) {
        
        // 参数校验：提示词内容不能为空
        AssertUtils.notEmpty(request.getPromptContent(), "提示词内容不能为空");

        // 参数校验：用户命令内容不能为空
        AssertUtils.notEmpty(request.getCommandContent(), "用户命令内容不能为空");

        // 调用 Spring AI 获取模型响应内容
        String content = callSpringAi(request);

        // 构建成功响应对象
        return buildSuccessResponse(content);
    }

    /**
     * 调用 Spring AI 获取响应内容。
     *
     * @param request AI 调用请求
     * @return 响应内容
     */
    private String callSpringAi(AgentAiRequest request) {
        
        // 组装用户输入内容，确保模型同时获得上下文和用户命令
        String userContent = request.getPromptContent() + "\n\n" + request.getCommandContent();

        // 通过 Spring AI ChatClient 执行同步调用
        return chatClient.prompt()
                .user(userContent)
                .call()
                .content();
    }

    /**
     * 构建成功响应对象。
     *
     * @param content 响应内容
     * @return AI 调用响应
     */
    private AgentAiResponse buildSuccessResponse(String content) {
        AgentAiResponse response = new AgentAiResponse();
        response.setSuccess(Boolean.TRUE);
        response.setResponseContent(content);
        return response;
    }

}
