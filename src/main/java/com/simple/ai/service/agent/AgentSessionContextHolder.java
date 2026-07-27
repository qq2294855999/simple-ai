package com.simple.ai.service.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 智能体会话上下文持有者。
 * <p>通过 ThreadLocal 存储 sessionId → 会话上下文（userId、agentId）映射，
 * 供 ToolCallback 在异步线程（boundedElastic）中获取会话上下文。</p>
 * <p>不再使用 Redis，改用 AgentSessionContext ThreadLocal 直接传递，
 * 配合 SessionAwareToolCallback 在工具执行线程上设置上下文。</p>
 *
 * @author qty
 */
@Component
public class AgentSessionContextHolder {

    /**
     * 会话上下文内部类，包含工具回调所需的会话级参数。
     *
     * @author qty
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionContext {

        /**
         * 用户ID
         */
        private String userId;

        /**
         * 智能体定义ID
         */
        private String agentId;
    }

    /**
     * 存储会话上下文（完整上下文，包含 userId 和 agentId）。
     *
     * <p>通过 AgentSessionContext ThreadLocal 存储，供同一线程的工具回调直接获取。</p>
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     * @param agentId   智能体定义ID
     */
    public void putContext(String sessionId, String userId, String agentId) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }

        // 将完整会话上下文存入 ThreadLocal
        AgentSessionContext.set(sessionId, userId, agentId);
    }

    /**
     * 获取完整会话上下文。
     *
     * @param sessionId 会话ID（已忽略，直接从 ThreadLocal 获取）
     * @return 会话上下文，未命中时返回 null
     */
    public SessionContext getContext(String sessionId) {
        AgentSessionContext.SessionContext ctx = AgentSessionContext.get();
        if (ctx == null) {
            return null;
        }
        return new SessionContext(ctx.getUserId(), ctx.getAgentId());
    }

    /**
     * 存储会话上下文（仅 userId，保持向后兼容）。
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     * @deprecated 使用 {@link #putContext(String, String, String)} 替代
     */
    @Deprecated
    public void put(String sessionId, String userId) {
        if (sessionId == null || sessionId.isBlank() || userId == null || userId.isBlank()) {
            return;
        }
        putContext(sessionId, userId, "");
    }

    /**
     * 获取会话上下文中的用户ID。
     *
     * @param sessionId 会话ID（已忽略，直接从 ThreadLocal 获取）
     * @return 用户ID
     * @deprecated 使用 {@link #getContext(String)} 替代
     */
    @Deprecated
    public String getUserId(String sessionId) {
        SessionContext context = getContext(sessionId);
        return context != null ? context.getUserId() : null;
    }

    /**
     * 清除会话上下文。
     *
     * @param sessionId 会话ID（已忽略，直接清除 ThreadLocal）
     */
    public void remove(String sessionId) {
        AgentSessionContext.clear();
    }
}