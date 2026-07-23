package com.simple.ai.service.agent;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

/**
 * 会话感知工具回调包装器。
 * <p>在 Spring AI 调用 ToolCallback 之前，将 sessionId 注入到
 * {@link AgentSessionContext} ThreadLocal 中，使运行在 boundedElastic
 * 异步线程上的工具回调能通过 ThreadLocal 获取会话上下文。</p>
 * <p>Spring AI 的 toolContext 参数在流式调用中可能无法可靠传递，
 * 因此采用在工具执行线程上主动设置 ThreadLocal 的策略，
 * 配合 Redis 中已存储的会话上下文，实现跨线程的上下文传递。</p>
 *
 * @author qty
 */
class SessionAwareToolCallback implements ToolCallback {

    /**
     * 被包装的原始工具回调
     */
    private final ToolCallback delegate;

    /**
     * 当前会话ID
     */
    private final String sessionId;

    /**
     * 创建会话感知工具回调包装器。
     *
     * @param delegate  原始工具回调
     * @param sessionId 会话ID
     */
    SessionAwareToolCallback(ToolCallback delegate, String sessionId) {
        this.delegate = delegate;
        this.sessionId = sessionId;
    }

    @Override
    public String call(String toolInput) {

        // 在 boundedElastic 执行线程上设置会话ID
        AgentSessionContext.setCurrentSessionId(sessionId);
        try {
            return delegate.call(toolInput);
        } finally {
            AgentSessionContext.clear();
        }
    }

    @Override
    public String call(String toolInput, ToolContext toolContext) {

        // 在 boundedElastic 执行线程上设置会话ID
        AgentSessionContext.setCurrentSessionId(sessionId);
        try {
            return delegate.call(toolInput, toolContext);
        } finally {
            AgentSessionContext.clear();
        }
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
    }

    @Override
    public ToolMetadata getToolMetadata() {
        return delegate.getToolMetadata();
    }
}
