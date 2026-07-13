package com.simple.ai.view.agentChatMessage;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simple.ai.common.entity.agentChatMessage.AgentChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 智能体聊天消息数据库访问层。
 *
 * @author qty
 */
@Mapper
public interface AgentChatMessageRepository extends BaseMapper<AgentChatMessage> {

    /**
     * 查询会话消息。
     *
     * @param sessionId 会话主键
     * @return 消息列表
     */
    List<AgentChatMessage> selectAllBySessionId(@Param("sessionId") String sessionId);

    /**
     * 查询会话消息最大序号。
     *
     * @param sessionId 会话主键
     * @return 最大序号
     */
    Long selectMaxSequenceNo(@Param("sessionId") String sessionId);
}
