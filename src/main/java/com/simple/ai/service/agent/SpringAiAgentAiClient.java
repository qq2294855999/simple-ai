package com.simple.ai.service.agent;

import com.simple.ai.common.dto.agent.AgentAiRequest;
import com.simple.ai.common.dto.agent.AgentAiResponse;
import com.simple.ai.common.dto.aiModel.AiModelRuntimeConfig;
import com.simple.ai.common.service.agent.AgentAiClient;
import com.simple.ai.service.aiModel.AiModelChatClientFactory;
import com.simple.ai.service.aiModel.AiModelRoutingService;
import com.simple.common.core.utils.AssertUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.function.Consumer;

/**
 * Spring AI 智能体 AI 调用客户端实现。
 *
 * @author qty
 */
@Service
class SpringAiAgentAiClient implements AgentAiClient {

    @Autowired
    private AiModelRoutingService aiModelRoutingService;

    @Autowired
    private AiModelChatClientFactory aiModelChatClientFactory;

    @Autowired
    private List<ToolCallback> toolCallbacks;

    @Override
    public AgentAiResponse chat(AgentAiRequest request) {

        // 校验 AI 调用请求中的必填业务内容
        assertAiRequest(request);

        // 按运行时路由解析模型并创建本次调用客户端
        AiModelRuntimeConfig config = resolveRuntimeConfig(request);
        ChatClient chatClient = aiModelChatClientFactory.create(config);

        // 调用 Spring AI 获取模型响应内容
        String content = callSpringAi(chatClient, request);

        // 构建成功响应对象
        return buildSuccessResponse(content, config);
    }

    @Override
    public AgentAiResponse chatStream(AgentAiRequest request, Consumer<String> tokenConsumer) {

        // 校验 AI 调用请求中的必填业务内容
        assertAiRequest(request);

        // 按运行时路由解析模型并创建本次调用客户端
        AiModelRuntimeConfig config = resolveRuntimeConfig(request);
        ChatClient chatClient = aiModelChatClientFactory.create(config);

        // 调用 Spring AI 流式接口并聚合完整响应内容
        String content = callSpringAiStream(chatClient, request, tokenConsumer);

        // 构建成功响应对象
        return buildSuccessResponse(content, config);
    }

    /**
     * 校验 AI 调用请求。
     *
     * @param request AI 调用请求
     */
    private void assertAiRequest(AgentAiRequest request) {
        AssertUtils.notEmpty(request, "AI调用请求不能为空");
        AssertUtils.notEmpty(request.getAgentId(), "智能体主键不能为空");
        AssertUtils.notEmpty(request.getPromptContent(), "提示词内容不能为空");
        AssertUtils.notEmpty(request.getCommandContent(), "用户命令内容不能为空");
    }

    /**
     * 解析当前调用的运行时模型配置。
     *
     * @param request AI 调用请求
     * @return 运行时模型配置
     */
    private AiModelRuntimeConfig resolveRuntimeConfig(AgentAiRequest request) {
        return aiModelRoutingService.resolve(request.getModelId(), request.getAgentId());
    }

    /**
     * 调用 Spring AI 获取响应内容。
     *
     * @param chatClient 动态聊天客户端
     * @param request AI 调用请求
     * @return 响应内容
     */
    private String callSpringAi(ChatClient chatClient, AgentAiRequest request) {
        String userContent = buildUserContent(request);
        // 注册工具回调，让 AI 在对话中自主调用工具完成数据操作
        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt();
        ChatClient.ChatClientRequestSpec userSpec = requestSpec.user(userContent);
        ChatClient.ChatClientRequestSpec toolSpec = userSpec.toolCallbacks(toolCallbacks);
        return toolSpec.call()
                .content();
    }

    /**
     * 调用 Spring AI 获取流式响应内容。
     *
     * @param chatClient 动态聊天客户端
     * @param request AI 调用请求
     * @param tokenConsumer token 内容消费者
     * @return 聚合后的完整响应内容
     */
    private String callSpringAiStream(ChatClient chatClient, AgentAiRequest request, Consumer<String> tokenConsumer) {
        String userContent = buildUserContent(request);
        StringBuilder contentBuilder = new StringBuilder();

        // 构建 Spring AI 流式请求，保留原生 token 输出能力
        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt();
        ChatClient.ChatClientRequestSpec userSpec = requestSpec.user(userContent);
        // 注册工具回调，让 AI 在流式对话中自主调用工具完成数据操作
        ChatClient.ChatClientRequestSpec toolSpec = userSpec.toolCallbacks(toolCallbacks);
        ChatClient.StreamResponseSpec streamSpec = toolSpec.stream();
        Flux<String> contentFlux = streamSpec.content();

        // 消费模型输出片段，同时聚合完整响应用于任务最终落库
        Flux<String> consumedFlux = contentFlux.doOnNext(token -> acceptStreamToken(tokenConsumer, contentBuilder, token));
        consumedFlux.blockLast();
        return contentBuilder.toString();
    }

    /**
     * 处理流式 token 内容。
     *
     * @param tokenConsumer token 内容消费者
     * @param contentBuilder 完整响应内容构建器
     * @param token 当前 token 内容
     */
    private void acceptStreamToken(Consumer<String> tokenConsumer, StringBuilder contentBuilder, String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        contentBuilder.append(token);
        if (tokenConsumer == null) {
            return;
        }
        tokenConsumer.accept(token);
    }

    /**
     * 构建发送给模型的用户内容。
     *
     * <p>将系统提示词、会话摘要和用户命令拼接为结构化上下文。</p>
     *
     * @param request AI 调用请求
     * @return 用户内容
     */
    private String buildUserContent(AgentAiRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append(request.getPromptContent());

        // 注入会话摘要，帮助AI理解历史对话脉络
        String sessionSummary = request.getSessionSummary();
        if (sessionSummary != null && !sessionSummary.isBlank()) {
            builder.append("\n\n## 会话历史\n");
            builder.append(sessionSummary);
        }
        builder.append("\n\n## 当前命令\n");
        builder.append(request.getCommandContent());
        return builder.toString();
    }

    /**
     * 构建成功响应对象。
     *
     * @param content 响应内容
     * @param config 实际运行模型配置
     * @return AI 调用响应
     */
    private AgentAiResponse buildSuccessResponse(String content, AiModelRuntimeConfig config) {
        AgentAiResponse response = new AgentAiResponse();
        response.setSuccess(Boolean.TRUE);
        response.setResponseContent(content);
        response.setFailureReason("");
        response.setProviderId(config.getProviderId());
        response.setProviderName(config.getProviderName());
        response.setModelId(config.getModelId());
        response.setModelCode(config.getModelCode());
        return response;
    }
}
