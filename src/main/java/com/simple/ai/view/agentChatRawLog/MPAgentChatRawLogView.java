package com.simple.ai.view.agentChatRawLog;

import com.simple.ai.common.entity.agentChatRawLog.AgentChatRawLog;
import com.simple.ai.common.view.agentChatRawLog.AgentChatRawLogView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * AI 原始消息日志数据访问视图实现。
 *
 * @author qty
 */
@Component
class MPAgentChatRawLogView implements AgentChatRawLogView {

    /**
     * 原始日志仓储
     */
    @Autowired
    private AgentChatRawLogRepository repository;

    @Override
    public void save(AgentChatRawLog rawLog) {
        repository.insert(rawLog);
    }
}