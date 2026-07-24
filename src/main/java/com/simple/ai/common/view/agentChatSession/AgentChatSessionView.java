package com.simple.ai.common.view.agentChatSession;

import com.simple.ai.common.entity.agentChatSession.AgentChatSession;

import java.util.List;

/**
 * 智能体聊天会话数据访问视图。
 *
 * @author qty
 */
public interface AgentChatSessionView {

    /**
     * 保存会话。
     *
     * @param session 会话实体
     */
    void save(AgentChatSession session);

    /**
     * 锁定并查询会话。
     *
     * @param id 会话主键
     * @return 会话实体
     */
    AgentChatSession findByIdForUpdate(String id);

    /**
     * 查询智能体下的会话，支持按模型和客户端过滤。
     *
     * @param agentId  智能体主键
     * @param modelId  模型主键（可选，不传则不过滤）
     * @param clientId 客户端主键（可选，不传则不过滤）
     * @return 会话列表
     */
    List<AgentChatSession> findAllByAgentId(String agentId, String modelId, String clientId);

    /**
     * 更新会话。
     *
     * @param session 会话实体
     */
    void updateById(AgentChatSession session);

    /**
     * 批量删除会话。
     *
     * @param ids 会话主键列表
     */
    void deleteByIds(List<String> ids);
}