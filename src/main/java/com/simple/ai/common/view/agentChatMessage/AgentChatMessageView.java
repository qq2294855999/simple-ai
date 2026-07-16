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

    /**
     * 分页查询会话消息（按序号倒序取最近 N 条，用于滚动加载更早的消息）。
     *
     * @param sessionId 会话主键
     * @param beforeSequenceNo 不包含此序号之前的消息（首次传 Long.MAX_VALUE）
     * @param size 每页数量
     * @return 消息列表（按 sequenceNo 倒序）
     */
    List<AgentChatMessage> findPageBySessionId(String sessionId, long beforeSequenceNo, int size);

    /**
     * 批量查询多个会话的消息。
     *
     * @param sessionIds 会话主键列表
     * @return 消息列表
     */
    List<AgentChatMessage> findAllBySessionIds(List<String> sessionIds);

    /**
     * 批量删除消息。
     *
     * @param ids 消息主键列表
     */
    void deleteByIds(List<String> ids);
}
