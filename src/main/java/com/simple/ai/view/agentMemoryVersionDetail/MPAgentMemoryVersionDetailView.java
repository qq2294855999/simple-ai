package com.simple.ai.view.agentMemoryVersionDetail;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.dto.agentMemoryVersionDetail.PageAgentMemoryVersionDetailRequest;
import com.simple.ai.common.dto.agentMemoryVersionDetail.PageAgentMemoryVersionDetailResponse;
import com.simple.ai.common.entity.agentMemoryVersionDetail.AgentMemoryVersionDetail;
import com.simple.ai.common.view.agentMemoryVersionDetail.AgentMemoryVersionDetailView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 记忆版本步骤(agent_memory_version_detail)数据库视图实现。
 *
 * @author qty
 */
@Component
class MPAgentMemoryVersionDetailView implements AgentMemoryVersionDetailView {

    @Autowired
    private AgentMemoryVersionDetailRepository repository;

    @Override
    public List<PageAgentMemoryVersionDetailResponse> findAll(PageAgentMemoryVersionDetailRequest pageRequest, Page<PageAgentMemoryVersionDetailResponse> page) {
        return repository.selectPage(pageRequest, page);
    }

    @Override
    public AgentMemoryVersionDetail findById(String id) {
        return repository.selectById(id);
    }

    @Override
    public void save(AgentMemoryVersionDetail entity) {
        repository.insert(entity);
    }

    @Override
    public void updateById(AgentMemoryVersionDetail entity) {
        repository.updateById(entity);
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }
}
