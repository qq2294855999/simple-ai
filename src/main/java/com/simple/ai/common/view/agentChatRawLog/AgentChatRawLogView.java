package com.simple.ai.common.view.agentChatRawLog;

import com.simple.ai.common.entity.agentChatRawLog.AgentChatRawLog;

/**
 * AI 原始消息日志数据访问视图。
 *
 * @author qty
 */
public interface AgentChatRawLogView {

    /**
     * 保存原始日志。
     *
     * @param rawLog 原始日志实体
     */
    void save(AgentChatRawLog rawLog);
}