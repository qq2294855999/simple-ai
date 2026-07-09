package com.simple.ai.view.agentMemory;

import java.util.Date;

import cn.hutool.core.collection.CollectionUtil;
import com.simple.common.core.utils.AssertUtils;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.lang.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Component;
import com.simple.ai.common.view.agentMemory.AgentMemoryView;
import com.simple.ai.common.entity.agentMemory.AgentMemory;
import com.simple.ai.common.dto.agentMemory.PageAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemory.FindOneAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemory.FindAllAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemory.DeleteAgentMemoryRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.simple.common.core.utils.JsonUtils;

/**
 * 智能体记忆(agent_memory)数据库视图实现
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
                    .like(ObjUtil.isNotEmpty(pageRequest.getTriggerCondition()), AgentMemory::getTriggerCondition, pageRequest.getTriggerCondition())
                    .eq(ObjUtil.isNotEmpty(pageRequest.getStatus()), AgentMemory::getStatus, pageRequest.getStatus());
        return repository.selectPage(pageRequest.getPage(AgentMemory.class), queryWrapper);
    }

    @Override
    public List<AgentMemory> findAll(FindAllAgentMemoryRequest findAllRequest, FindAllAgentMemoryRequest neRequest) {
        LambdaQueryWrapper<AgentMemory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findAllRequest.getId()), AgentMemory::getId, findAllRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getAgentId()), AgentMemory::getAgentId, findAllRequest.getAgentId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getMemoryName()), AgentMemory::getMemoryName, findAllRequest.getMemoryName())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getTriggerCondition()), AgentMemory::getTriggerCondition, findAllRequest.getTriggerCondition())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getStatus()), AgentMemory::getStatus, findAllRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), AgentMemory::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getAgentId()), AgentMemory::getAgentId, neRequest.getAgentId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getMemoryName()), AgentMemory::getMemoryName, neRequest.getMemoryName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTriggerCondition()), AgentMemory::getTriggerCondition, neRequest.getTriggerCondition())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), AgentMemory::getStatus, neRequest.getStatus());

        return repository.selectList(queryWrapper);
    }

    @Override
    public AgentMemory findOne(FindOneAgentMemoryRequest findOneRequest, FindOneAgentMemoryRequest neRequest) {
        LambdaQueryWrapper<AgentMemory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), AgentMemory::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getAgentId()), AgentMemory::getAgentId, findOneRequest.getAgentId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getMemoryName()), AgentMemory::getMemoryName, findOneRequest.getMemoryName())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getTriggerCondition()), AgentMemory::getTriggerCondition, findOneRequest.getTriggerCondition())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStatus()), AgentMemory::getStatus, findOneRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), AgentMemory::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getAgentId()), AgentMemory::getAgentId, neRequest.getAgentId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getMemoryName()), AgentMemory::getMemoryName, neRequest.getMemoryName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTriggerCondition()), AgentMemory::getTriggerCondition, neRequest.getTriggerCondition())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), AgentMemory::getStatus, neRequest.getStatus());

        List<AgentMemory> list = repository.selectList(queryWrapper);
        if (list.isEmpty()) {
            return null;
        } else if (list.size() > 1) {
            AssertUtils.error("数据异常", "参数为[{}]查询只需要一条数据，但是查询出来多条", JsonUtils.toJsonStr(findOneRequest));
        }
        return list.get(0);
    }

    @Override
    public Long findCount(FindOneAgentMemoryRequest findOneRequest, FindOneAgentMemoryRequest neRequest) {
        LambdaQueryWrapper<AgentMemory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), AgentMemory::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getAgentId()), AgentMemory::getAgentId, findOneRequest.getAgentId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getMemoryName()), AgentMemory::getMemoryName, findOneRequest.getMemoryName())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getTriggerCondition()), AgentMemory::getTriggerCondition, findOneRequest.getTriggerCondition())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStatus()), AgentMemory::getStatus, findOneRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), AgentMemory::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getAgentId()), AgentMemory::getAgentId, neRequest.getAgentId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getMemoryName()), AgentMemory::getMemoryName, neRequest.getMemoryName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTriggerCondition()), AgentMemory::getTriggerCondition, neRequest.getTriggerCondition())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), AgentMemory::getStatus, neRequest.getStatus());
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
                    .eq(ObjUtil.isNotEmpty(request.getTriggerCondition()), AgentMemory::getTriggerCondition, request.getTriggerCondition())
                    .eq(ObjUtil.isNotEmpty(request.getStatus()), AgentMemory::getStatus, request.getStatus());
        repository.delete(queryWrapper);
    }
}

