package com.simple.ai.view.agentChatSession;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simple.ai.common.entity.agentChatSession.AgentChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 智能体聊天会话数据库访问层。
 *
 * @author qty
 */
@Mapper
public interface AgentChatSessionRepository extends BaseMapper<AgentChatSession> {

    /**
     * 锁定并查询会话。
     *
     * @param id 会话主键
     * @return 会话实体
     */
    AgentChatSession selectByIdForUpdate(@Param("id") String id);

    /**
     * 查询智能体会话，支持按模型和客户端过滤。
     *
     * @param agentId  智能体主键
     * @param modelId  模型主键（可选，不传则不过滤）
     * @param clientId 客户端主键（可选，不传则不过滤）
     * @return 会话列表
     */
    List<AgentChatSession> selectAllByAgentId(@Param("agentId") String agentId, @Param("modelId") String modelId, @Param("clientId") String clientId);
}