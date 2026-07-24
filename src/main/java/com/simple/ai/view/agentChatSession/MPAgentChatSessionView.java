package com.simple.ai.view.agentChatSession;

import com.simple.ai.common.entity.agentChatSession.AgentChatSession;
import com.simple.ai.common.view.agentChatSession.AgentChatSessionView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 智能体聊天会话数据访问视图实现。
 *
 * @author qty
 */
@Component
class MPAgentChatSessionView implements AgentChatSessionView {

    /** 会话仓储 */
    @Autowired
    private AgentChatSessionRepository repository;

    @Override
    public void save(AgentChatSession session) {
        repository.insert(session);
    }

    @Override
    public AgentChatSession findByIdForUpdate(String id) {
        return repository.selectByIdForUpdate(id);
    }

    @Override
    public List<AgentChatSession> findAllByAgentId(String agentId, String modelId, String clientId) {
        return repository.selectAllByAgentId(agentId, modelId, clientId);
    }

    @Override
    public void updateById(AgentChatSession session) {
        repository.updateById(session);
    }

    @Override
    public void deleteByIds(List<String> ids) {
        repository.deleteByIds(ids);
    }
}