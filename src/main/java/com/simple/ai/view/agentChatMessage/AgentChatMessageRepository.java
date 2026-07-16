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

    /**
     * 批量查询多个会话的消息。
     *
     * @param sessionIds 会话主键列表
     * @return 消息列表
     */
    List<AgentChatMessage> selectAllBySessionIds(@Param("sessionIds") List<String> sessionIds);

    /**
     * 分页查询会话消息（按序号倒序，用于滚动向上加载更早的消息）。
     *
     * @param sessionId 会话主键
     * @param beforeSequenceNo 不包含此序号（首次传 Long.MAX_VALUE）
     * @param size 每页数量
     * @return 消息列表（按 sequenceNo 倒序）
     */
    List<AgentChatMessage> selectPageBySessionId(@Param("sessionId") String sessionId,
                                                  @Param("beforeSequenceNo") long beforeSequenceNo,
                                                  @Param("size") int size);
}
