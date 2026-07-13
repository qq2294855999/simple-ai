package com.simple.ai.service.aiModel;

import com.simple.ai.common.dto.aiModel.AiModelRuntimeConfig;
import com.simple.common.core.utils.AssertUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Component;

/**
 * AI 模型动态客户端工厂。
 *
 * @author qty
 */
@Component
public class AiModelChatClientFactory {

    /**
     * 为单次调用创建动态聊天客户端。
     *
     * @param config 已解析的运行时模型配置
     * @return 动态聊天客户端
     */
    public ChatClient create(AiModelRuntimeConfig config) {
        AssertUtils.notEmpty(config, "运行时模型配置不能为空");
        AssertUtils.notEmpty(config.getBaseUrl(), "模型服务地址不能为空");
        AssertUtils.notEmpty(config.getApiKey(), "模型API Key不能为空");
        AssertUtils.notEmpty(config.getModelCode(), "模型编码不能为空");

        // 为本次调用创建 OpenAI-compatible API
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .build();

        // 为本次调用绑定实际模型编码
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(config.getModelCode())
                .build();
        OpenAiChatModel chatModel = OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();
        return ChatClient.create(chatModel);
    }
}
