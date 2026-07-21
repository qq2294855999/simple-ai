package com.simple.ai.view.agentMemoryVersion;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.dto.agentMemoryVersion.PageAgentMemoryVersionRequest;
import com.simple.ai.common.dto.agentMemoryVersion.PageAgentMemoryVersionResponse;
import com.simple.ai.common.entity.agentMemoryVersion.AgentMemoryVersion;
import com.simple.ai.common.view.agentMemoryVersion.AgentMemoryVersionView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 记忆版本(agent_memory_version)数据库视图实现。
 *
 * @author qty
 */
@Component
class MPAgentMemoryVersionView implements AgentMemoryVersionView {

    @Autowired
    private AgentMemoryVersionRepository repository;

    @Override
    public List<PageAgentMemoryVersionResponse> findAll(PageAgentMemoryVersionRequest pageRequest, Page<PageAgentMemoryVersionResponse> page) {
        return repository.selectPage(pageRequest, page);
    }

    @Override
    public AgentMemoryVersion findById(String id) {
        return repository.selectById(id);
    }

    @Override
    public void save(AgentMemoryVersion entity) {
        repository.insert(entity);
    }

    @Override
    public void updateById(AgentMemoryVersion entity) {
        repository.updateById(entity);
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }
}
