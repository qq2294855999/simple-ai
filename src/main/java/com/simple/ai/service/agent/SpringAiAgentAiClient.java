package com.simple.ai.service.agent;

import com.simple.ai.common.dto.agent.AgentAiRequest;
import com.simple.ai.common.dto.agent.AgentAiResponse;
import com.simple.ai.common.service.agent.AgentAiClient;
import com.simple.common.core.utils.AssertUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.function.Consumer;

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

        // 校验 AI 调用请求中的必填业务内容
        assertAiRequest(request);

        // 调用 Spring AI 获取模型响应内容
        String content = callSpringAi(request);

        // 构建成功响应对象
        return buildSuccessResponse(content);
    }

    @Override
    public AgentAiResponse chatStream(AgentAiRequest request, Consumer<String> tokenConsumer) {

        // 校验 AI 调用请求中的必填业务内容
        assertAiRequest(request);

        // 调用 Spring AI 流式接口并聚合完整响应内容
        String content = callSpringAiStream(request, tokenConsumer);

        // 构建成功响应对象
        return buildSuccessResponse(content);
    }

    /**
     * 校验 AI 调用请求。
     *
     * @param request AI 调用请求
     */
    private void assertAiRequest(AgentAiRequest request) {

        // 参数校验：提示词内容不能为空
        AssertUtils.notEmpty(request.getPromptContent(), "提示词内容不能为空");

        // 参数校验：用户命令内容不能为空
        AssertUtils.notEmpty(request.getCommandContent(), "用户命令内容不能为空");
    }

    /**
     * 调用 Spring AI 获取响应内容。
     *
     * @param request AI 调用请求
     * @return 响应内容
     */
    private String callSpringAi(AgentAiRequest request) {

        // 组装用户输入内容，确保模型同时获得上下文和用户命令
        String userContent = buildUserContent(request);

        // 通过 Spring AI ChatClient 执行同步调用
        return chatClient.prompt()
                .user(userContent)
                .call()
                .content();
    }

    /**
     * 调用 Spring AI 获取流式响应内容。
     *
     * @param request AI 调用请求
     * @param tokenConsumer token 内容消费者
     * @return 聚合后的完整响应内容
     */
    private String callSpringAiStream(AgentAiRequest request, Consumer<String> tokenConsumer) {
        String userContent = buildUserContent(request);
        StringBuilder contentBuilder = new StringBuilder();

        // 构建 Spring AI 流式请求，保留原生 token 输出能力
        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt();
        ChatClient.ChatClientRequestSpec userSpec = requestSpec.user(userContent);
        ChatClient.StreamResponseSpec streamSpec = userSpec.stream();
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

        // token 为空时不向外发布，也不参与完整内容聚合
        if (token == null || token.isBlank()) {
            return;
        }

        // 聚合完整响应内容，保证流式调用结束后仍能返回最终文本
        contentBuilder.append(token);

        // 未传入消费者时只保留最终聚合内容
        if (tokenConsumer == null) {
            return;
        }
        tokenConsumer.accept(token);
    }

    /**
     * 构建发送给模型的用户内容。
     *
     * @param request AI 调用请求
     * @return 用户内容
     */
    private String buildUserContent(AgentAiRequest request) {
        return request.getPromptContent() + "\n\n" + request.getCommandContent();
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
