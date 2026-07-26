package com.simple.ai.view.agentChatRawLog;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simple.ai.common.entity.agentChatRawLog.AgentChatRawLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 原始消息日志数据库访问层。
 *
 * @author qty
 */
@Mapper
public interface AgentChatRawLogRepository extends BaseMapper<AgentChatRawLog> {
}