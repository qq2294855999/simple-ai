package com.simple.ai.common.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Simple AI 全局配置属性。
 * <p>读取 application.yaml 中 simple.ai 前缀下的所有配置项，
 * 包括聊天、记忆蒸馏等核心参数，统一管理，禁止在代码中硬编码。</p>
 *
 * @author qty
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "simple.ai")
public class SimpleAiProperties {

    /**
     * 聊天相关配置。
     */
    private Chat chat = new Chat();

    /**
     * 聊天配置子组。
     */
    @Getter
    @Setter
    public static class Chat {

        /**
         * SSE 流式聊天最大等待时长（毫秒），默认 300000（5分钟）。
         */
        private long streamTimeoutMillis = 300000L;

        /**
         * 注入 AI 上下文的对话历史最大轮数。
         * <p>每轮包含一条 USER 消息和一条 ASSISTANT 消息。</p>
         */
        private int maxHistoryTurns = 20;
    }
}