package com.simple.ai.service.agentMemoryStep;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.copy.agentMemoryStep.AgentMemoryStepCopyMapper;
import com.simple.ai.common.dto.agentMemoryStep.CreateAgentMemoryStepRequest;
import com.simple.ai.common.dto.agentMemoryStep.PageAgentMemoryStepRequest;
import com.simple.ai.common.dto.agentMemoryStep.PageAgentMemoryStepResponse;
import com.simple.ai.common.dto.agentMemoryStep.UpdateAgentMemoryStepRequest;
import com.simple.ai.common.entity.agentMemoryStep.AgentMemoryStep;
import com.simple.ai.common.service.agentMemoryStep.AgentMemoryStepService;
import com.simple.ai.common.view.agentMemoryStep.AgentMemoryStepView;
import com.simple.common.core.utils.AssertUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 智能体记忆步骤(agent_memory_step)默认接口实现。
 *
 * @author qty
 */
@Slf4j
@Service
@Transactional
class DefaultAgentMemoryStepService implements AgentMemoryStepService {

    /**
     * 记忆步骤视图
     */
    @Autowired
    private AgentMemoryStepView agentMemoryStepView;

    /**
     * 对象属性复制
     */
    @Autowired
    private AgentMemoryStepCopyMapper copy;

    @Override
    public IPage<PageAgentMemoryStepResponse> findAll(PageAgentMemoryStepRequest pageRequest) {
        var pageInfo = agentMemoryStepView.findAll(pageRequest);
        return pageInfo.convert(entity -> copy.toPageResponse(entity));
    }

    @Override
    public String save(CreateAgentMemoryStepRequest createRequest) {
        AgentMemoryStep entity = copy.toEntity(createRequest);
        entity.setStatus("ON");
        agentMemoryStepView.save(entity);
        return entity.getId();
    }

    @Override
    public String updateById(UpdateAgentMemoryStepRequest updateRequest) {
        AgentMemoryStep existing = agentMemoryStepView.findById(updateRequest.getId());
        AssertUtils.notEmpty(existing, "主键[{}]的步骤数据不存在", updateRequest.getId());

        AgentMemoryStep entity = copy.toEntity(updateRequest);
        agentMemoryStepView.updateById(entity);
        return entity.getId();
    }

    @Override
    public void deleteByIds(List<String> ids) {
        agentMemoryStepView.deleteByIds(ids);
    }
}