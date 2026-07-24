package com.simple.ai.service.agentMemory;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.copy.agentMemory.AgentMemoryCopyMapper;
import com.simple.ai.common.dto.agentMemory.*;
import com.simple.ai.common.entity.agentMemory.AgentMemory;
import com.simple.ai.common.entity.agentMemoryStep.AgentMemoryStep;
import com.simple.ai.common.service.agentMemory.AgentMemoryService;
import com.simple.ai.common.view.agentMemory.AgentMemoryView;
import com.simple.ai.common.view.agentMemoryStep.AgentMemoryStepView;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.mp.common.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 智能体记忆(agent_memory)默认接口实现
 *
 * @author qty
 */
@Slf4j
@Service
@Transactional
class DefaultAgentMemoryService implements AgentMemoryService {

    /**
     * 记忆视图
     */
    @Autowired
    private AgentMemoryView agentMemoryView;

    /**
     * 记忆步骤视图
     */
    @Autowired
    private AgentMemoryStepView agentMemoryStepView;

    /**
     * 对象属性复制
     */
    @Autowired
    private AgentMemoryCopyMapper copy;

    @Override
    public IPage<PageAgentMemoryResponse> findAll(PageAgentMemoryRequest pageRequest) {
        var pageInfo = agentMemoryView.findAll(pageRequest);
        return pageInfo.convert(entity -> copy.toPageResponse(entity));
    }

    @Override
    public InfoAgentMemoryResponse findById(String id) {
        AgentMemory memory = agentMemoryView.findById(id);
        AssertUtils.notEmpty(memory, "主键为[{}]的数据为空", id);

        InfoAgentMemoryResponse response = copy.toInfoResponse(memory);

        // 加载记忆步骤列表
        List<AgentMemoryStep> steps = agentMemoryStepView.findAllByMemoryId(id);
        response.setSteps(steps);

        return response;
    }

    @Override
    public String save(CreateAgentMemoryRequest createRequest) {
        AgentMemory entity = copy.toEntity(createRequest);
        entity.setVersionNo(1);
        entity.setVersionStatus(1);
        entity.setCreateReason("MANUAL");
        entity.setStatus(Status.ON);
        entity.setReserve("");
        agentMemoryView.save(entity);
        return entity.getId();
    }

    @Override
    public String updateById(UpdateAgentMemoryRequest updateRequest) {
        AgentMemory existing = agentMemoryView.findById(updateRequest.getId());
        AssertUtils.notEmpty(existing, "主键[{}]的数据不存在", updateRequest.getId());

        AgentMemory entity = copy.toEntity(updateRequest);
        agentMemoryView.updateById(entity);
        return entity.getId();
    }

    @Override
    public void deleteByIds(List<String> ids) {

        // 删除记忆步骤
        for (String id : ids) {
            agentMemoryStepView.deleteByMemoryId(id);
        }

        // 删除记忆主表
        agentMemoryView.deleteByIds(ids);
    }

    @Override
    public void publish(String id) {
        AgentMemory memory = agentMemoryView.findById(id);
        AssertUtils.notEmpty(memory, "主键为[{}]的数据为空", id);

        // 仅 DRAFT 状态可发布
        AssertUtils.isTrue(Integer.valueOf(1).equals(memory.getVersionStatus()), "记忆[{}]不是草稿状态，无法发布", id);

        memory.setVersionStatus(2);
        agentMemoryView.updateById(memory);

        log.info("记忆发布成功：memoryId={}, memoryName={}", id, memory.getMemoryName());
    }
}