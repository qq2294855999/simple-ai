package com.simple.ai.service.agentDefinition;

import java.util.Date;

import com.simple.common.core.utils.BeanUtils;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.eventbus.common.service.EventBusService;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.simple.ai.common.service.agentDefinition.AgentDefinitionService;
import com.simple.ai.common.entity.agentDefinition.AgentDefinition;
import com.simple.ai.common.view.agentDefinition.AgentDefinitionView;
import com.simple.ai.common.dto.agentDefinition.PageAgentDefinitionResponse;
import com.simple.ai.common.dto.agentDefinition.InfoAgentDefinitionResponse;
import com.simple.ai.common.dto.agentDefinition.CreateAgentDefinitionRequest;
import com.simple.ai.common.dto.agentDefinition.UpdateAgentDefinitionRequest;
import com.simple.ai.common.dto.agentDefinition.PageAgentDefinitionRequest;
import com.simple.ai.common.copy.agentDefinition.AgentDefinitionCopyMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * 智能体定义(agent_definition)默认接口实现
 *
 * @author qty
 */
@Service
@Transactional
class DefaultAgentDefinitionService implements AgentDefinitionService {

    @Autowired
    private AgentDefinitionView agentDefinitionView;

    @Autowired
    private AgentDefinitionCopyMapper copy;

    @Autowired
    private EventBusService eventBusService;

    @Override
    public IPage<PageAgentDefinitionResponse> findAll(PageAgentDefinitionRequest pageRequest) {
        var pageInfo = agentDefinitionView.findAll(pageRequest);
        return pageInfo.convert(entity -> copy.toPageResponse(entity));
    }

    @Override
    public InfoAgentDefinitionResponse findById(String id) {
        var agentDefinition = agentDefinitionView.findById(id);
        AssertUtils.notEmpty(agentDefinition, "主键为[{}]的数据为空", id);
        return copy.toInfoResponse(agentDefinition);
    }

    @Override
    public String save(CreateAgentDefinitionRequest createRequest) {
        var entity = copy.toEntity(createRequest);
        agentDefinitionView.save(entity);
        return entity.getId();
    }

    @Override
    public String updateById(UpdateAgentDefinitionRequest updateRequest) {
        var agentDefinition = agentDefinitionView.findById(updateRequest.getId());
        AssertUtils.notEmpty(agentDefinition, "主键[{}]的数据不存在", updateRequest.getId());

        var entity = copy.toEntity(updateRequest);
        agentDefinitionView.updateById(entity);
        return entity.getId();
    }

    @Override
    public void deleteByIds(List<String> ids) {
        agentDefinitionView.deleteByIds(ids);
    }
}

