package com.simple.ai.view.agentMemory;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.dto.agentMemory.DeleteAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemory.FindAllAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemory.FindOneAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemory.PageAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemory.PageAggregateAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemory.PageAggregateAgentMemoryResponse;
import com.simple.ai.common.entity.agentMemory.AgentMemory;
import com.simple.ai.common.view.agentMemory.AgentMemoryView;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.core.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Agent memory view implementation.
 *
 * @author qty
 */
@Component
class MPAgentMemoryView implements AgentMemoryView {

    @Autowired
    private AgentMemoryRepository repository;

    @Override
    public IPage<AgentMemory> findAll(PageAgentMemoryRequest pageRequest) {
        LambdaQueryWrapper<AgentMemory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(ObjUtil.isNotEmpty(pageRequest.getAgentId()), AgentMemory::getAgentId, pageRequest.getAgentId())
                .like(ObjUtil.isNotEmpty(pageRequest.getMemoryName()), AgentMemory::getMemoryName, pageRequest.getMemoryName())
                .like(ObjUtil.isNotEmpty(pageRequest.getStepName()), AgentMemory::getStepName, pageRequest.getStepName())
                .like(ObjUtil.isNotEmpty(pageRequest.getTriggerCondition()), AgentMemory::getTriggerCondition, pageRequest.getTriggerCondition())
                .like(ObjUtil.isNotEmpty(pageRequest.getTriggerAction()), AgentMemory::getTriggerAction, pageRequest.getTriggerAction())
                .eq(ObjUtil.isNotEmpty(pageRequest.getStatus()), AgentMemory::getStatus, pageRequest.getStatus())
                .like(ObjUtil.isNotEmpty(pageRequest.getReserver()), AgentMemory::getReserver, pageRequest.getReserver())
                .like(ObjUtil.isNotEmpty(pageRequest.getRemark()), AgentMemory::getRemark, pageRequest.getRemark());
        return repository.selectPage(pageRequest.getPage(AgentMemory.class), queryWrapper);
    }

    @Override
    public IPage<PageAggregateAgentMemoryResponse> findAggregateAll(PageAggregateAgentMemoryRequest pageRequest) {

        // Build page boundary
        Page<AgentMemory> page = pageRequest.getPage(AgentMemory.class);
        Long offset = (page.getCurrent() - 1) * page.getSize();

        // Query aggregate records and count
        List<PageAggregateAgentMemoryResponse> records = repository.selectAggregatePage(pageRequest, offset, page.getSize());
        Long total = repository.selectAggregateCount(pageRequest);

        Page<PageAggregateAgentMemoryResponse> result = new Page<>(page.getCurrent(), page.getSize(), total);
        result.setRecords(records);
        return result;
    }

    @Override
    public List<AgentMemory> findAll(FindAllAgentMemoryRequest findAllRequest, FindAllAgentMemoryRequest neRequest) {
        LambdaQueryWrapper<AgentMemory> queryWrapper = buildFindAllWrapper(findAllRequest, neRequest);
        return repository.selectList(queryWrapper);
    }

    @Override
    public AgentMemory findOne(FindOneAgentMemoryRequest findOneRequest, FindOneAgentMemoryRequest neRequest) {
        LambdaQueryWrapper<AgentMemory> queryWrapper = buildFindOneWrapper(findOneRequest, neRequest);
        List<AgentMemory> list = repository.selectList(queryWrapper);

        // Return null when no record exists
        if (list.isEmpty()) {
            return null;
        }

        // Validate unique result for single query
        if (list.size() > 1) {
            AssertUtils.error("data error", "single query returned multiple records: {}", JsonUtils.toJsonStr(findOneRequest));
        }
        return list.get(0);
    }

    @Override
    public Long findCount(FindOneAgentMemoryRequest findOneRequest, FindOneAgentMemoryRequest neRequest) {
        LambdaQueryWrapper<AgentMemory> queryWrapper = buildFindOneWrapper(findOneRequest, neRequest);
        return repository.selectCount(queryWrapper);
    }

    @Override
    public AgentMemory findById(String id) {
        return repository.selectById(id);
    }

    @Override
    public void save(AgentMemory agentMemory) {
        repository.insert(agentMemory);
    }

    @Override
    public void updateById(AgentMemory agentMemory) {
        repository.updateById(agentMemory);
    }

    @Override
    public void updateById(List<AgentMemory> list) {
        repository.updateById(list);
    }

    @Override
    public void saves(List<AgentMemory> list) {

        // Skip empty batch insert
        if (CollectionUtil.isNotEmpty(list)) {
            repository.insert(list);
        }
    }

    @Override
    public void deleteByIds(List<String> ids) {
        repository.deleteByIds(ids);
    }

    @Override
    public void delete(DeleteAgentMemoryRequest request) {
        LambdaQueryWrapper<AgentMemory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(request.getId()), AgentMemory::getId, request.getId())
                .eq(ObjUtil.isNotEmpty(request.getAgentId()), AgentMemory::getAgentId, request.getAgentId())
                .eq(ObjUtil.isNotEmpty(request.getMemoryName()), AgentMemory::getMemoryName, request.getMemoryName())
                .eq(ObjUtil.isNotEmpty(request.getStepName()), AgentMemory::getStepName, request.getStepName())
                .eq(ObjUtil.isNotEmpty(request.getTriggerCondition()), AgentMemory::getTriggerCondition, request.getTriggerCondition())
                .eq(ObjUtil.isNotEmpty(request.getTriggerAction()), AgentMemory::getTriggerAction, request.getTriggerAction())
                .eq(ObjUtil.isNotEmpty(request.getStatus()), AgentMemory::getStatus, request.getStatus())
                .eq(ObjUtil.isNotEmpty(request.getReserver()), AgentMemory::getReserver, request.getReserver())
                .eq(ObjUtil.isNotEmpty(request.getRemark()), AgentMemory::getRemark, request.getRemark());
        repository.delete(queryWrapper);
    }

    /**
     * Build list query wrapper.
     *
     * @param findAllRequest query condition
     * @param neRequest exclude condition
     * @return query wrapper
     */
    private LambdaQueryWrapper<AgentMemory> buildFindAllWrapper(FindAllAgentMemoryRequest findAllRequest, FindAllAgentMemoryRequest neRequest) {
        LambdaQueryWrapper<AgentMemory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findAllRequest.getId()), AgentMemory::getId, findAllRequest.getId())
                .eq(ObjUtil.isNotEmpty(findAllRequest.getAgentId()), AgentMemory::getAgentId, findAllRequest.getAgentId())
                .eq(ObjUtil.isNotEmpty(findAllRequest.getMemoryName()), AgentMemory::getMemoryName, findAllRequest.getMemoryName())
                .eq(ObjUtil.isNotEmpty(findAllRequest.getStepName()), AgentMemory::getStepName, findAllRequest.getStepName())
                .eq(ObjUtil.isNotEmpty(findAllRequest.getTriggerCondition()), AgentMemory::getTriggerCondition, findAllRequest.getTriggerCondition())
                .eq(ObjUtil.isNotEmpty(findAllRequest.getTriggerAction()), AgentMemory::getTriggerAction, findAllRequest.getTriggerAction())
                .eq(ObjUtil.isNotEmpty(findAllRequest.getStatus()), AgentMemory::getStatus, findAllRequest.getStatus())
                .eq(ObjUtil.isNotEmpty(findAllRequest.getReserver()), AgentMemory::getReserver, findAllRequest.getReserver())
                .eq(ObjUtil.isNotEmpty(findAllRequest.getRemark()), AgentMemory::getRemark, findAllRequest.getRemark())
                .ne(ObjUtil.isNotEmpty(neRequest.getId()), AgentMemory::getId, neRequest.getId())
                .ne(ObjUtil.isNotEmpty(neRequest.getAgentId()), AgentMemory::getAgentId, neRequest.getAgentId())
                .ne(ObjUtil.isNotEmpty(neRequest.getMemoryName()), AgentMemory::getMemoryName, neRequest.getMemoryName())
                .ne(ObjUtil.isNotEmpty(neRequest.getStepName()), AgentMemory::getStepName, neRequest.getStepName())
                .ne(ObjUtil.isNotEmpty(neRequest.getTriggerCondition()), AgentMemory::getTriggerCondition, neRequest.getTriggerCondition())
                .ne(ObjUtil.isNotEmpty(neRequest.getTriggerAction()), AgentMemory::getTriggerAction, neRequest.getTriggerAction())
                .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), AgentMemory::getStatus, neRequest.getStatus())
                .ne(ObjUtil.isNotEmpty(neRequest.getReserver()), AgentMemory::getReserver, neRequest.getReserver())
                .ne(ObjUtil.isNotEmpty(neRequest.getRemark()), AgentMemory::getRemark, neRequest.getRemark());
        return queryWrapper;
    }

    /**
     * Build single query wrapper.
     *
     * @param findOneRequest query condition
     * @param neRequest exclude condition
     * @return query wrapper
     */
    private LambdaQueryWrapper<AgentMemory> buildFindOneWrapper(FindOneAgentMemoryRequest findOneRequest, FindOneAgentMemoryRequest neRequest) {
        LambdaQueryWrapper<AgentMemory> queryWrapper = buildFindAllWrapper(toFindAllRequest(findOneRequest), toFindAllRequest(neRequest));
        return queryWrapper;
    }

    /**
     * Convert single query request to list query request.
     *
     * @param request single query request
     * @return list query request
     */
    private FindAllAgentMemoryRequest toFindAllRequest(FindOneAgentMemoryRequest request) {
        FindAllAgentMemoryRequest result = new FindAllAgentMemoryRequest();
        result.setId(request.getId());
        result.setAgentId(request.getAgentId());
        result.setMemoryName(request.getMemoryName());
        result.setStepName(request.getStepName());
        result.setTriggerCondition(request.getTriggerCondition());
        result.setTriggerAction(request.getTriggerAction());
        result.setStatus(request.getStatus());
        result.setReserver(request.getReserver());
        result.setRemark(request.getRemark());
        return result;
    }
}
