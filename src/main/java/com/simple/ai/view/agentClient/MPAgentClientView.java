package com.simple.ai.view.agentClient;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.dto.agentClient.PageAgentClientRequest;
import com.simple.ai.common.dto.agentClient.PageAgentClientResponse;
import com.simple.ai.common.entity.agentClient.AgentClient;
import com.simple.ai.common.view.agentClient.AgentClientView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 客户端实例(agent_client)数据库视图实现。
 *
 * @author qty
 */
@Component
class MPAgentClientView implements AgentClientView {

    @Autowired
    private AgentClientRepository repository;

    @Override
    public List<PageAgentClientResponse> findAll(PageAgentClientRequest pageRequest, Page<PageAgentClientResponse> page) {
        return repository.selectPage(pageRequest, page);
    }

    @Override
    public AgentClient findById(String id) {
        return repository.selectById(id);
    }

    @Override
    public AgentClient findByIdWithLock(String id) {
        return repository.selectByIdWithLock(id);
    }

    @Override
    public void save(AgentClient entity) {
        repository.insert(entity);
    }

    @Override
    public void updateById(AgentClient entity) {
        repository.updateById(entity);
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }
}
