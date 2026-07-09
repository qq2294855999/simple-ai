package com.simple.ai.service.agentMemory;

import java.util.Date;

import com.simple.common.core.utils.BeanUtils;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.eventbus.common.service.EventBusService;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.simple.ai.common.service.agentMemory.AgentMemoryService;
import com.simple.ai.common.entity.agentMemory.AgentMemory;
import com.simple.ai.common.view.agentMemory.AgentMemoryView;
import com.simple.ai.common.dto.agentMemory.PageAgentMemoryResponse;
import com.simple.ai.common.dto.agentMemory.InfoAgentMemoryResponse;
import com.simple.ai.common.dto.agentMemory.CreateAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemory.UpdateAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemory.PageAgentMemoryRequest;
import com.simple.ai.common.copy.agentMemory.AgentMemoryCopyMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * 智能体记忆(agent_memory)默认接口实现
 *
 * @author qty
 */
@Service
@Transactional
class DefaultAgentMemoryService implements AgentMemoryService {

    @Autowired
    private AgentMemoryView agentMemoryView;

    @Autowired
    private AgentMemoryCopyMapper copy;

    @Autowired
    private EventBusService eventBusService;

    @Override
    public IPage<PageAgentMemoryResponse> findAll(PageAgentMemoryRequest pageRequest) {
        var pageInfo = agentMemoryView.findAll(pageRequest);
        return pageInfo.convert(entity -> copy.toPageResponse(entity));
    }

    @Override
    public InfoAgentMemoryResponse findById(String id) {
        var agentMemory = agentMemoryView.findById(id);
        AssertUtils.notEmpty(agentMemory, "主键为[{}]的数据为空", id);
        return copy.toInfoResponse(agentMemory);
    }

    @Override
    public String save(CreateAgentMemoryRequest createRequest) {
        var entity = copy.toEntity(createRequest);
        agentMemoryView.save(entity);
        return entity.getId();
    }

    @Override
    public String updateById(UpdateAgentMemoryRequest updateRequest) {
        var agentMemory = agentMemoryView.findById(updateRequest.getId());
        AssertUtils.notEmpty(agentMemory, "主键[{}]的数据不存在", updateRequest.getId());

        var entity = copy.toEntity(updateRequest);
        agentMemoryView.updateById(entity);
        return entity.getId();
    }

    @Override
    public void deleteByIds(List<String> ids) {
        agentMemoryView.deleteByIds(ids);
    }
}

