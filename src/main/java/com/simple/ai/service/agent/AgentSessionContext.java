package com.simple.ai.service.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 智能体会话上下文持有者。
 * <p>通过 ThreadLocal 存储当前会话完整上下文（sessionId、userId、agentId），
 * 供 ToolCallback 在异步线程（boundedElastic）中获取会话信息，
 * 替代原有的 Redis + ThreadLocal 双层传递方案，简化架构。</p>
 *
 * @author qty
 */
public final class AgentSessionContext {

    /**
     * 完整会话上下文内部类，包含工具回调所需的会话级参数。
     *
     * @author qty
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionContext {

        /**
         * 会话ID
         */
        private String sessionId;

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
     * 会话上下文线程本地变量
     */
    private static final ThreadLocal<SessionContext> CURRENT_SESSION = new ThreadLocal<>();

    private AgentSessionContext() {
    }

    /**
     * 设置当前会话完整上下文。
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     * @param agentId   智能体ID
     */
    public static void set(String sessionId, String userId, String agentId) {
        CURRENT_SESSION.set(new SessionContext(sessionId, userId, agentId));
    }

    /**
     * 获取当前会话完整上下文。
     *
     * @return 会话上下文，未设置时返回 null
     */
    public static SessionContext get() {
        return CURRENT_SESSION.get();
    }

    /**
     * 获取当前会话ID。
     *
     * @return 会话ID
     */
    public static String getCurrentSessionId() {
        SessionContext ctx = CURRENT_SESSION.get();
        return ctx != null ? ctx.getSessionId() : null;
    }

    /**
     * 获取当前用户ID。
     *
     * @return 用户ID
     */
    public static String getCurrentUserId() {
        SessionContext ctx = CURRENT_SESSION.get();
        return ctx != null ? ctx.getUserId() : null;
    }

    /**
     * 获取当前智能体ID。
     *
     * @return 智能体ID
     */
    public static String getCurrentAgentId() {
        SessionContext ctx = CURRENT_SESSION.get();
        return ctx != null ? ctx.getAgentId() : null;
    }

    /**
     * 设置当前会话ID（保持向后兼容）。
     *
     * @param sessionId 会话ID
     * @deprecated 使用 {@link #set(String, String, String)} 替代
     */
    @Deprecated
    public static void setCurrentSessionId(String sessionId) {
        SessionContext ctx = CURRENT_SESSION.get();
        if (ctx == null) {
            ctx = new SessionContext();
            CURRENT_SESSION.set(ctx);
        }
        ctx.setSessionId(sessionId);
    }

    /**
     * 清除当前会话上下文。
     */
    public static void clear() {
        CURRENT_SESSION.remove();
    }
}