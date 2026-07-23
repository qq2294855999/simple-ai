package com.simple.ai.service.agent;

/**
 * 智能体会话上下文持有者。
 * <p>通过 ThreadLocal 存储当前会话的 sessionId，供 ToolCallback 在异步线程中获取会话信息。
 * 由调度服务在调用 AI 前设置，工具回调通过 getCurrentSessionId() 获取。</p>
 *
 * @author qty
 */
public final class AgentSessionContext {

    /**
     * 当前会话ID线程本地变量
     */
    private static final ThreadLocal<String> CURRENT_SESSION_ID = new ThreadLocal<>();

    private AgentSessionContext() {
    }

    /**
     * 获取当前会话ID。
     *
     * @return 会话ID
     */
    public static String getCurrentSessionId() {
        return CURRENT_SESSION_ID.get();
    }

    /**
     * 设置当前会话ID。
     *
     * @param sessionId 会话ID
     */
    public static void setCurrentSessionId(String sessionId) {
        CURRENT_SESSION_ID.set(sessionId);
    }

    /**
     * 清除当前会话ID。
     */
    public static void clear() {
        CURRENT_SESSION_ID.remove();
    }
}