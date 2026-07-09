package com.simple.ai.common.service.session;

/**
 * 智能体会话服务。
 *
 * @author qty
 */
public interface AgentSessionService {

    /**
     * 查询会话摘要。
     *
     * @param sessionId 会话ID
     * @return 会话摘要
     */
    String findSummary(String sessionId);

    /**
     * 保存会话摘要。
     *
     * @param sessionId 会话ID
     * @param summary 会话摘要
     */
    void saveSummary(String sessionId, String summary);

    /**
     * 追加会话消息。
     *
     * @param sessionId 会话ID
     * @param message 会话消息
     */
    void appendMessage(String sessionId, String message);

}
