package com.simple.ai.view.agentMemoryStep;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.dto.agentMemoryStep.PageAgentMemoryStepRequest;
import com.simple.ai.common.entity.agentMemoryStep.AgentMemoryStep;
import com.simple.ai.common.view.agentMemoryStep.AgentMemoryStepView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 智能体记忆步骤(agent_memory_step)数据库视图实现
 *
 * @author qty
 */
@Component
class MPAgentMemoryStepView implements AgentMemoryStepView {

    @Autowired
    private AgentMemoryStepRepository repository;

    @Override
    public IPage<AgentMemoryStep> findAll(PageAgentMemoryStepRequest pageRequest) {
        Page<AgentMemoryStep> page = new Page<>(pageRequest.getCurrent(), pageRequest.getSize());
        LambdaQueryWrapper<AgentMemoryStep> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ObjUtil.isNotEmpty(pageRequest.getMemoryId()), AgentMemoryStep::getMemoryId, pageRequest.getMemoryId())
               .like(ObjUtil.isNotEmpty(pageRequest.getStepName()), AgentMemoryStep::getStepName, pageRequest.getStepName())
               .eq(ObjUtil.isNotEmpty(pageRequest.getAtomicCommandCode()), AgentMemoryStep::getAtomicCommandCode, pageRequest.getAtomicCommandCode())
               .orderByAsc(AgentMemoryStep::getSequenceNo);
        return repository.selectPage(page, wrapper);
    }

    @Override
    public List<AgentMemoryStep> findAllByMemoryId(String memoryId) {
        LambdaQueryWrapper<AgentMemoryStep> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentMemoryStep::getMemoryId, memoryId).orderByAsc(AgentMemoryStep::getSequenceNo);
        return repository.selectList(wrapper);
    }

    @Override
    public AgentMemoryStep findById(String id) {
        return repository.selectById(id);
    }

    @Override
    public void save(AgentMemoryStep step) {
        repository.insert(step);
    }

    @Override
    public void saves(List<AgentMemoryStep> list) {

        // 空集合不执行批量新增
        if (CollectionUtil.isNotEmpty(list)) {
            repository.insert(list);
        }
    }

    @Override
    public void updateById(AgentMemoryStep step) {
        repository.updateById(step);
    }

    @Override
    public void deleteByMemoryId(String memoryId) {
        LambdaQueryWrapper<AgentMemoryStep> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AgentMemoryStep::getMemoryId, memoryId);
        repository.delete(wrapper);
    }

    @Override
    public void deleteByIds(List<String> ids) {
        repository.deleteByIds(ids);
    }
}