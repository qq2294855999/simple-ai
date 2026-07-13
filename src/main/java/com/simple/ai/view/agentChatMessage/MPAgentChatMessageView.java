package com.simple.ai.view.agentChatMessage;

import com.simple.ai.common.entity.agentChatMessage.AgentChatMessage;
import com.simple.ai.common.view.agentChatMessage.AgentChatMessageView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 智能体聊天消息数据访问视图实现。
 *
 * @author qty
 */
@Component
class MPAgentChatMessageView implements AgentChatMessageView {

    /** 消息仓储 */
    @Autowired
    private AgentChatMessageRepository repository;

    @Override
    public Long findMaxSequenceNo(String sessionId) {
        return repository.selectMaxSequenceNo(sessionId);
    }

    @Override
    public void save(AgentChatMessage message) {
        repository.insert(message);
    }

    @Override
    public List<AgentChatMessage> findAllBySessionId(String sessionId) {
        return repository.selectAllBySessionId(sessionId);
    }
}
