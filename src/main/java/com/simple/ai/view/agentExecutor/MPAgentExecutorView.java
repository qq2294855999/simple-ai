package com.simple.ai.view.agentExecutor;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.dto.agentExecutor.PageAgentExecutorRequest;
import com.simple.ai.common.dto.agentExecutor.PageAgentExecutorResponse;
import com.simple.ai.common.entity.agentExecutor.AgentExecutor;
import com.simple.ai.common.view.agentExecutor.AgentExecutorView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 执行器类型(agent_executor)数据库视图实现。
 *
 * @author qty
 */
@Component
class MPAgentExecutorView implements AgentExecutorView {

    @Autowired
    private AgentExecutorRepository repository;

    @Override
    public List<PageAgentExecutorResponse> findAll(PageAgentExecutorRequest pageRequest, Page<PageAgentExecutorResponse> page) {
        return repository.selectPage(pageRequest, page);
    }

    @Override
    public AgentExecutor findById(String id) {
        return repository.selectById(id);
    }

    @Override
    public void save(AgentExecutor entity) {
        repository.insert(entity);
    }

    @Override
    public void updateById(AgentExecutor entity) {
        repository.updateById(entity);
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }
}
