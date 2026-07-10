package com.simple.ai.view.agentMemoryDetail;

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
import com.simple.ai.common.view.agentMemoryDetail.AgentMemoryDetailView;
import com.simple.ai.common.entity.agentMemoryDetail.AgentMemoryDetail;
import com.simple.ai.common.dto.agentMemoryDetail.PageAgentMemoryDetailRequest;
import com.simple.ai.common.dto.agentMemoryDetail.PageAggregateAgentMemoryDetailRequest;
import com.simple.ai.common.dto.agentMemoryDetail.PageAggregateAgentMemoryDetailResponse;
import com.simple.ai.common.dto.agentMemoryDetail.FindOneAgentMemoryDetailRequest;
import com.simple.ai.common.dto.agentMemoryDetail.FindAllAgentMemoryDetailRequest;
import com.simple.ai.common.dto.agentMemoryDetail.DeleteAgentMemoryDetailRequest;
import com.simple.common.mp.common.enums.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.simple.common.core.utils.JsonUtils;

/**
 * 智能体记忆详情(agent_memory_detail)数据库视图实现
 *
 * @author qty
 */
@Component
class MPAgentMemoryDetailView implements AgentMemoryDetailView {

    @Autowired
    private AgentMemoryDetailRepository repository;

    @Override
    public IPage<AgentMemoryDetail> findAll(PageAgentMemoryDetailRequest pageRequest) {
        LambdaQueryWrapper<AgentMemoryDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(ObjUtil.isNotEmpty(pageRequest.getAgentMemoryId()), AgentMemoryDetail::getAgentMemoryId, pageRequest.getAgentMemoryId())
                    .like(ObjUtil.isNotEmpty(pageRequest.getStepName()), AgentMemoryDetail::getStepName, pageRequest.getStepName())
                    .like(ObjUtil.isNotEmpty(pageRequest.getStepType()), AgentMemoryDetail::getStepType, pageRequest.getStepType())
                    .like(ObjUtil.isNotEmpty(pageRequest.getExecContent()), AgentMemoryDetail::getExecContent, pageRequest.getExecContent())
                    .like(ObjUtil.isNotEmpty(pageRequest.getReturnDataFormat()), AgentMemoryDetail::getReturnDataFormat, pageRequest.getReturnDataFormat())
                    .like(ObjUtil.isNotEmpty(pageRequest.getParentStepId()), AgentMemoryDetail::getParentStepId, pageRequest.getParentStepId())
                    .like(ObjUtil.isNotEmpty(pageRequest.getNextStepId()), AgentMemoryDetail::getNextStepId, pageRequest.getNextStepId())
                    .like(ObjUtil.isNotEmpty(pageRequest.getBranchCondition()), AgentMemoryDetail::getBranchCondition, pageRequest.getBranchCondition())
                    .like(ObjUtil.isNotEmpty(pageRequest.getBranchRoute()), AgentMemoryDetail::getBranchRoute, pageRequest.getBranchRoute())
                    .like(ObjUtil.isNotEmpty(pageRequest.getModel()), AgentMemoryDetail::getModel, pageRequest.getModel())
                    .eq(ObjUtil.isNotEmpty(pageRequest.getStatus()), AgentMemoryDetail::getStatus, pageRequest.getStatus())
                    .like(ObjUtil.isNotEmpty(pageRequest.getReserver()), AgentMemoryDetail::getReserver, pageRequest.getReserver())
                    .like(ObjUtil.isNotEmpty(pageRequest.getRemark()), AgentMemoryDetail::getRemark, pageRequest.getRemark());
        return repository.selectPage(pageRequest.getPage(AgentMemoryDetail.class), queryWrapper);
    }

    @Override
    public IPage<PageAggregateAgentMemoryDetailResponse> findAggregateAll(PageAggregateAgentMemoryDetailRequest pageRequest) {

        // 构建分页边界
        Page<AgentMemoryDetail> page = pageRequest.getPage(AgentMemoryDetail.class);
        Long offset = (page.getCurrent() - 1) * page.getSize();

        // 查询聚合记录与总数
        List<PageAggregateAgentMemoryDetailResponse> records = repository.selectAggregatePage(pageRequest, offset, page.getSize());
        Long total = repository.selectAggregateCount(pageRequest);

        Page<PageAggregateAgentMemoryDetailResponse> result = new Page<>(page.getCurrent(), page.getSize(), total);
        result.setRecords(records);
        return result;
    }

    @Override
    public List<AgentMemoryDetail> findAll(FindAllAgentMemoryDetailRequest findAllRequest, FindAllAgentMemoryDetailRequest neRequest) {
        LambdaQueryWrapper<AgentMemoryDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findAllRequest.getId()), AgentMemoryDetail::getId, findAllRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getAgentMemoryId()), AgentMemoryDetail::getAgentMemoryId, findAllRequest.getAgentMemoryId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getStepName()), AgentMemoryDetail::getStepName, findAllRequest.getStepName())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getStepType()), AgentMemoryDetail::getStepType, findAllRequest.getStepType())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getExecContent()), AgentMemoryDetail::getExecContent, findAllRequest.getExecContent())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getReturnDataFormat()), AgentMemoryDetail::getReturnDataFormat, findAllRequest.getReturnDataFormat())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getParentStepId()), AgentMemoryDetail::getParentStepId, findAllRequest.getParentStepId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getNextStepId()), AgentMemoryDetail::getNextStepId, findAllRequest.getNextStepId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getBranchCondition()), AgentMemoryDetail::getBranchCondition, findAllRequest.getBranchCondition())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getBranchRoute()), AgentMemoryDetail::getBranchRoute, findAllRequest.getBranchRoute())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getModel()), AgentMemoryDetail::getModel, findAllRequest.getModel())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getStatus()), AgentMemoryDetail::getStatus, findAllRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getReserver()), AgentMemoryDetail::getReserver, findAllRequest.getReserver())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getRemark()), AgentMemoryDetail::getRemark, findAllRequest.getRemark())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), AgentMemoryDetail::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getAgentMemoryId()), AgentMemoryDetail::getAgentMemoryId, neRequest.getAgentMemoryId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStepName()), AgentMemoryDetail::getStepName, neRequest.getStepName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStepType()), AgentMemoryDetail::getStepType, neRequest.getStepType())
                    .ne(ObjUtil.isNotEmpty(neRequest.getExecContent()), AgentMemoryDetail::getExecContent, neRequest.getExecContent())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReturnDataFormat()), AgentMemoryDetail::getReturnDataFormat, neRequest.getReturnDataFormat())
                    .ne(ObjUtil.isNotEmpty(neRequest.getParentStepId()), AgentMemoryDetail::getParentStepId, neRequest.getParentStepId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getNextStepId()), AgentMemoryDetail::getNextStepId, neRequest.getNextStepId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getBranchCondition()), AgentMemoryDetail::getBranchCondition, neRequest.getBranchCondition())
                    .ne(ObjUtil.isNotEmpty(neRequest.getBranchRoute()), AgentMemoryDetail::getBranchRoute, neRequest.getBranchRoute())
                    .ne(ObjUtil.isNotEmpty(neRequest.getModel()), AgentMemoryDetail::getModel, neRequest.getModel())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), AgentMemoryDetail::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReserver()), AgentMemoryDetail::getReserver, neRequest.getReserver())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRemark()), AgentMemoryDetail::getRemark, neRequest.getRemark());

        return repository.selectList(queryWrapper);
    }

    @Override
    public List<AgentMemoryDetail> findAllByAgentMemoryIds(List<String> agentMemoryIds) {
        LambdaQueryWrapper<AgentMemoryDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CollectionUtil.isNotEmpty(agentMemoryIds), AgentMemoryDetail::getAgentMemoryId, agentMemoryIds)
                    .eq(AgentMemoryDetail::getStatus, Status.ON);
        return repository.selectList(queryWrapper);
    }

    @Override
    public AgentMemoryDetail findOne(FindOneAgentMemoryDetailRequest findOneRequest, FindOneAgentMemoryDetailRequest neRequest) {
        LambdaQueryWrapper<AgentMemoryDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), AgentMemoryDetail::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getAgentMemoryId()), AgentMemoryDetail::getAgentMemoryId, findOneRequest.getAgentMemoryId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStepName()), AgentMemoryDetail::getStepName, findOneRequest.getStepName())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStepType()), AgentMemoryDetail::getStepType, findOneRequest.getStepType())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getExecContent()), AgentMemoryDetail::getExecContent, findOneRequest.getExecContent())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getReturnDataFormat()), AgentMemoryDetail::getReturnDataFormat, findOneRequest.getReturnDataFormat())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getParentStepId()), AgentMemoryDetail::getParentStepId, findOneRequest.getParentStepId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getNextStepId()), AgentMemoryDetail::getNextStepId, findOneRequest.getNextStepId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getBranchCondition()), AgentMemoryDetail::getBranchCondition, findOneRequest.getBranchCondition())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getBranchRoute()), AgentMemoryDetail::getBranchRoute, findOneRequest.getBranchRoute())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getModel()), AgentMemoryDetail::getModel, findOneRequest.getModel())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStatus()), AgentMemoryDetail::getStatus, findOneRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getReserver()), AgentMemoryDetail::getReserver, findOneRequest.getReserver())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getRemark()), AgentMemoryDetail::getRemark, findOneRequest.getRemark())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), AgentMemoryDetail::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getAgentMemoryId()), AgentMemoryDetail::getAgentMemoryId, neRequest.getAgentMemoryId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStepName()), AgentMemoryDetail::getStepName, neRequest.getStepName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStepType()), AgentMemoryDetail::getStepType, neRequest.getStepType())
                    .ne(ObjUtil.isNotEmpty(neRequest.getExecContent()), AgentMemoryDetail::getExecContent, neRequest.getExecContent())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReturnDataFormat()), AgentMemoryDetail::getReturnDataFormat, neRequest.getReturnDataFormat())
                    .ne(ObjUtil.isNotEmpty(neRequest.getParentStepId()), AgentMemoryDetail::getParentStepId, neRequest.getParentStepId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getNextStepId()), AgentMemoryDetail::getNextStepId, neRequest.getNextStepId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getBranchCondition()), AgentMemoryDetail::getBranchCondition, neRequest.getBranchCondition())
                    .ne(ObjUtil.isNotEmpty(neRequest.getBranchRoute()), AgentMemoryDetail::getBranchRoute, neRequest.getBranchRoute())
                    .ne(ObjUtil.isNotEmpty(neRequest.getModel()), AgentMemoryDetail::getModel, neRequest.getModel())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), AgentMemoryDetail::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReserver()), AgentMemoryDetail::getReserver, neRequest.getReserver())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRemark()), AgentMemoryDetail::getRemark, neRequest.getRemark());

        List<AgentMemoryDetail> list = repository.selectList(queryWrapper);
        if (list.isEmpty()) {
            return null;
        } else if (list.size() > 1) {
            AssertUtils.error("数据异常", "参数为[{}]查询只需要一条数据，但是查询出来多条", JsonUtils.toJsonStr(findOneRequest));
        }
        return list.get(0);
    }

    @Override
    public Long findCount(FindOneAgentMemoryDetailRequest findOneRequest, FindOneAgentMemoryDetailRequest neRequest) {
        LambdaQueryWrapper<AgentMemoryDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), AgentMemoryDetail::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getAgentMemoryId()), AgentMemoryDetail::getAgentMemoryId, findOneRequest.getAgentMemoryId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStepName()), AgentMemoryDetail::getStepName, findOneRequest.getStepName())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStepType()), AgentMemoryDetail::getStepType, findOneRequest.getStepType())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getExecContent()), AgentMemoryDetail::getExecContent, findOneRequest.getExecContent())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getReturnDataFormat()), AgentMemoryDetail::getReturnDataFormat, findOneRequest.getReturnDataFormat())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getParentStepId()), AgentMemoryDetail::getParentStepId, findOneRequest.getParentStepId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getNextStepId()), AgentMemoryDetail::getNextStepId, findOneRequest.getNextStepId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getBranchCondition()), AgentMemoryDetail::getBranchCondition, findOneRequest.getBranchCondition())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getBranchRoute()), AgentMemoryDetail::getBranchRoute, findOneRequest.getBranchRoute())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getModel()), AgentMemoryDetail::getModel, findOneRequest.getModel())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStatus()), AgentMemoryDetail::getStatus, findOneRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getReserver()), AgentMemoryDetail::getReserver, findOneRequest.getReserver())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getRemark()), AgentMemoryDetail::getRemark, findOneRequest.getRemark())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), AgentMemoryDetail::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getAgentMemoryId()), AgentMemoryDetail::getAgentMemoryId, neRequest.getAgentMemoryId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStepName()), AgentMemoryDetail::getStepName, neRequest.getStepName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStepType()), AgentMemoryDetail::getStepType, neRequest.getStepType())
                    .ne(ObjUtil.isNotEmpty(neRequest.getExecContent()), AgentMemoryDetail::getExecContent, neRequest.getExecContent())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReturnDataFormat()), AgentMemoryDetail::getReturnDataFormat, neRequest.getReturnDataFormat())
                    .ne(ObjUtil.isNotEmpty(neRequest.getParentStepId()), AgentMemoryDetail::getParentStepId, neRequest.getParentStepId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getNextStepId()), AgentMemoryDetail::getNextStepId, neRequest.getNextStepId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getBranchCondition()), AgentMemoryDetail::getBranchCondition, neRequest.getBranchCondition())
                    .ne(ObjUtil.isNotEmpty(neRequest.getBranchRoute()), AgentMemoryDetail::getBranchRoute, neRequest.getBranchRoute())
                    .ne(ObjUtil.isNotEmpty(neRequest.getModel()), AgentMemoryDetail::getModel, neRequest.getModel())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), AgentMemoryDetail::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReserver()), AgentMemoryDetail::getReserver, neRequest.getReserver())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRemark()), AgentMemoryDetail::getRemark, neRequest.getRemark());
        return repository.selectCount(queryWrapper);
    }

    @Override
    public AgentMemoryDetail findById(String id) {
        return repository.selectById(id);
    }

    @Override
    public void save(AgentMemoryDetail agentMemoryDetail) {
        repository.insert(agentMemoryDetail);
    }

    @Override
    public void updateById(AgentMemoryDetail agentMemoryDetail) {
        repository.updateById(agentMemoryDetail);
    }

    @Override
    public void updateById(List<AgentMemoryDetail> list) {
        repository.updateById(list);
    }

    @Override
    public void saves(List<AgentMemoryDetail> list) {
        if (CollectionUtil.isNotEmpty(list)) {
            repository.insert(list);
        }
    }

    @Override
    public void deleteByIds(List<String> ids) {
        repository.deleteByIds(ids);
    }

    @Override
    public void delete(DeleteAgentMemoryDetailRequest request) {
        LambdaQueryWrapper<AgentMemoryDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(request.getId()), AgentMemoryDetail::getId, request.getId())
                    .eq(ObjUtil.isNotEmpty(request.getAgentMemoryId()), AgentMemoryDetail::getAgentMemoryId, request.getAgentMemoryId())
                    .eq(ObjUtil.isNotEmpty(request.getStepName()), AgentMemoryDetail::getStepName, request.getStepName())
                    .eq(ObjUtil.isNotEmpty(request.getStepType()), AgentMemoryDetail::getStepType, request.getStepType())
                    .eq(ObjUtil.isNotEmpty(request.getExecContent()), AgentMemoryDetail::getExecContent, request.getExecContent())
                    .eq(ObjUtil.isNotEmpty(request.getReturnDataFormat()), AgentMemoryDetail::getReturnDataFormat, request.getReturnDataFormat())
                    .eq(ObjUtil.isNotEmpty(request.getParentStepId()), AgentMemoryDetail::getParentStepId, request.getParentStepId())
                    .eq(ObjUtil.isNotEmpty(request.getNextStepId()), AgentMemoryDetail::getNextStepId, request.getNextStepId())
                    .eq(ObjUtil.isNotEmpty(request.getBranchCondition()), AgentMemoryDetail::getBranchCondition, request.getBranchCondition())
                    .eq(ObjUtil.isNotEmpty(request.getBranchRoute()), AgentMemoryDetail::getBranchRoute, request.getBranchRoute())
                    .eq(ObjUtil.isNotEmpty(request.getModel()), AgentMemoryDetail::getModel, request.getModel())
                    .eq(ObjUtil.isNotEmpty(request.getStatus()), AgentMemoryDetail::getStatus, request.getStatus())
                    .eq(ObjUtil.isNotEmpty(request.getReserver()), AgentMemoryDetail::getReserver, request.getReserver())
                    .eq(ObjUtil.isNotEmpty(request.getRemark()), AgentMemoryDetail::getRemark, request.getRemark());
        repository.delete(queryWrapper);
    }
}

