package com.simple.ai.common.view.agentChatMessage;

import com.simple.ai.common.entity.agentChatMessage.AgentChatMessage;

import java.util.List;

/**
 * 智能体聊天消息数据访问视图。
 *
 * @author qty
 */
public interface AgentChatMessageView {

    /**
     * 查询会话消息最大序号。
     *
     * @param sessionId 会话主键
     * @return 最大序号
     */
    Long findMaxSequenceNo(String sessionId);

    /**
     * 保存消息。
     *
     * @param message 消息实体
     */
    void save(AgentChatMessage message);

    /**
     * 查询会话消息。
     *
     * @param sessionId 会话主键
     * @return 消息列表
     */
    List<AgentChatMessage> findAllBySessionId(String sessionId);
}
