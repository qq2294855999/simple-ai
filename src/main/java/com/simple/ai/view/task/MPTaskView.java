package com.simple.ai.view.task;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.dto.task.*;
import com.simple.ai.common.entity.task.Task;
import com.simple.ai.common.view.task.TaskView;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.core.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 任务(task)数据库视图实现
 *
 * @author qty
 */
@Component
class MPTaskView implements TaskView {

    @Autowired
    private TaskRepository repository;

    @Override
    public IPage<Task> findAll(PageTaskRequest pageRequest) {
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(ObjUtil.isNotEmpty(pageRequest.getAgentMemoryId()), Task::getAgentMemoryId, pageRequest.getAgentMemoryId())
                    .like(ObjUtil.isNotEmpty(pageRequest.getTaskName()), Task::getTaskName, pageRequest.getTaskName())
                    .like(ObjUtil.isNotEmpty(pageRequest.getParentTaskId()), Task::getParentTaskId, pageRequest.getParentTaskId())
                    .like(ObjUtil.isNotEmpty(pageRequest.getNextTaskId()), Task::getNextTaskId, pageRequest.getNextTaskId())
                    .like(ObjUtil.isNotEmpty(pageRequest.getStepType()), Task::getStepType, pageRequest.getStepType())
                    .like(ObjUtil.isNotEmpty(pageRequest.getBranchCondition()), Task::getBranchCondition, pageRequest.getBranchCondition())
                    .like(ObjUtil.isNotEmpty(pageRequest.getBranchRoute()), Task::getBranchRoute, pageRequest.getBranchRoute())
                    .like(ObjUtil.isNotEmpty(pageRequest.getRequestParams()), Task::getRequestParams, pageRequest.getRequestParams())
                    .like(ObjUtil.isNotEmpty(pageRequest.getReturnParams()), Task::getReturnParams, pageRequest.getReturnParams())
                    .like(ObjUtil.isNotEmpty(pageRequest.getExecStatus()), Task::getExecStatus, pageRequest.getExecStatus())
                    .like(ObjUtil.isNotEmpty(pageRequest.getFailureReason()), Task::getFailureReason, pageRequest.getFailureReason())
                    .eq(ObjUtil.isNotEmpty(pageRequest.getStatus()), Task::getStatus, pageRequest.getStatus())
                    .like(ObjUtil.isNotEmpty(pageRequest.getReserve()), Task::getReserve, pageRequest.getReserve())
                    .like(ObjUtil.isNotEmpty(pageRequest.getRemark()), Task::getRemark, pageRequest.getRemark());
        return repository.selectPage(pageRequest.getPage(Task.class), queryWrapper);
    }

    @Override
    public IPage<PageAggregateTaskResponse> findAggregateAll(PageAggregateTaskRequest pageRequest) {

        // 构建分页边界
        Page<Task> page = pageRequest.getPage(Task.class);
        Long offset = (page.getCurrent() - 1) * page.getSize();

        // 查询聚合记录与总数
        List<PageAggregateTaskResponse> records = repository.selectAggregatePage(pageRequest, offset, page.getSize());
        Long total = repository.selectAggregateCount(pageRequest);

        Page<PageAggregateTaskResponse> result = new Page<>(page.getCurrent(), page.getSize(), total);
        result.setRecords(records);
        return result;
    }

    @Override
    public List<Task> findAll(FindAllTaskRequest findAllRequest, FindAllTaskRequest neRequest) {
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findAllRequest.getId()), Task::getId, findAllRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getAgentMemoryId()), Task::getAgentMemoryId, findAllRequest.getAgentMemoryId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getTaskName()), Task::getTaskName, findAllRequest.getTaskName())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getParentTaskId()), Task::getParentTaskId, findAllRequest.getParentTaskId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getNextTaskId()), Task::getNextTaskId, findAllRequest.getNextTaskId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getStepType()), Task::getStepType, findAllRequest.getStepType())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getBranchCondition()), Task::getBranchCondition, findAllRequest.getBranchCondition())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getBranchRoute()), Task::getBranchRoute, findAllRequest.getBranchRoute())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getRequestParams()), Task::getRequestParams, findAllRequest.getRequestParams())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getReturnParams()), Task::getReturnParams, findAllRequest.getReturnParams())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getExecStatus()), Task::getExecStatus, findAllRequest.getExecStatus())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getFailureReason()), Task::getFailureReason, findAllRequest.getFailureReason())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getStatus()), Task::getStatus, findAllRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getReserve()), Task::getReserve, findAllRequest.getReserve())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getRemark()), Task::getRemark, findAllRequest.getRemark())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), Task::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getAgentMemoryId()), Task::getAgentMemoryId, neRequest.getAgentMemoryId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTaskName()), Task::getTaskName, neRequest.getTaskName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getParentTaskId()), Task::getParentTaskId, neRequest.getParentTaskId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getNextTaskId()), Task::getNextTaskId, neRequest.getNextTaskId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStepType()), Task::getStepType, neRequest.getStepType())
                    .ne(ObjUtil.isNotEmpty(neRequest.getBranchCondition()), Task::getBranchCondition, neRequest.getBranchCondition())
                    .ne(ObjUtil.isNotEmpty(neRequest.getBranchRoute()), Task::getBranchRoute, neRequest.getBranchRoute())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRequestParams()), Task::getRequestParams, neRequest.getRequestParams())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReturnParams()), Task::getReturnParams, neRequest.getReturnParams())
                    .ne(ObjUtil.isNotEmpty(neRequest.getExecStatus()), Task::getExecStatus, neRequest.getExecStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getFailureReason()), Task::getFailureReason, neRequest.getFailureReason())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), Task::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReserve()), Task::getReserve, neRequest.getReserve())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRemark()), Task::getRemark, neRequest.getRemark());

        return repository.selectList(queryWrapper);
    }

    @Override
    public Task findOne(FindOneTaskRequest findOneRequest, FindOneTaskRequest neRequest) {
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), Task::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getAgentMemoryId()), Task::getAgentMemoryId, findOneRequest.getAgentMemoryId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getTaskName()), Task::getTaskName, findOneRequest.getTaskName())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getParentTaskId()), Task::getParentTaskId, findOneRequest.getParentTaskId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getNextTaskId()), Task::getNextTaskId, findOneRequest.getNextTaskId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStepType()), Task::getStepType, findOneRequest.getStepType())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getBranchCondition()), Task::getBranchCondition, findOneRequest.getBranchCondition())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getBranchRoute()), Task::getBranchRoute, findOneRequest.getBranchRoute())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getRequestParams()), Task::getRequestParams, findOneRequest.getRequestParams())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getReturnParams()), Task::getReturnParams, findOneRequest.getReturnParams())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getExecStatus()), Task::getExecStatus, findOneRequest.getExecStatus())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getFailureReason()), Task::getFailureReason, findOneRequest.getFailureReason())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStatus()), Task::getStatus, findOneRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getReserve()), Task::getReserve, findOneRequest.getReserve())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getRemark()), Task::getRemark, findOneRequest.getRemark())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), Task::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getAgentMemoryId()), Task::getAgentMemoryId, neRequest.getAgentMemoryId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTaskName()), Task::getTaskName, neRequest.getTaskName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getParentTaskId()), Task::getParentTaskId, neRequest.getParentTaskId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getNextTaskId()), Task::getNextTaskId, neRequest.getNextTaskId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStepType()), Task::getStepType, neRequest.getStepType())
                    .ne(ObjUtil.isNotEmpty(neRequest.getBranchCondition()), Task::getBranchCondition, neRequest.getBranchCondition())
                    .ne(ObjUtil.isNotEmpty(neRequest.getBranchRoute()), Task::getBranchRoute, neRequest.getBranchRoute())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRequestParams()), Task::getRequestParams, neRequest.getRequestParams())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReturnParams()), Task::getReturnParams, neRequest.getReturnParams())
                    .ne(ObjUtil.isNotEmpty(neRequest.getExecStatus()), Task::getExecStatus, neRequest.getExecStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getFailureReason()), Task::getFailureReason, neRequest.getFailureReason())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), Task::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReserve()), Task::getReserve, neRequest.getReserve())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRemark()), Task::getRemark, neRequest.getRemark());

        List<Task> list = repository.selectList(queryWrapper);
        if (list.isEmpty()) {
            return null;
        } else if (list.size() > 1) {
            AssertUtils.error("数据异常", "参数为[{}]查询只需要一条数据，但是查询出来多条", JsonUtils.toJsonStr(findOneRequest));
        }
        return list.get(0);
    }

    @Override
    public Long findCount(FindOneTaskRequest findOneRequest, FindOneTaskRequest neRequest) {
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), Task::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getAgentMemoryId()), Task::getAgentMemoryId, findOneRequest.getAgentMemoryId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getTaskName()), Task::getTaskName, findOneRequest.getTaskName())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getParentTaskId()), Task::getParentTaskId, findOneRequest.getParentTaskId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getNextTaskId()), Task::getNextTaskId, findOneRequest.getNextTaskId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStepType()), Task::getStepType, findOneRequest.getStepType())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getBranchCondition()), Task::getBranchCondition, findOneRequest.getBranchCondition())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getBranchRoute()), Task::getBranchRoute, findOneRequest.getBranchRoute())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getRequestParams()), Task::getRequestParams, findOneRequest.getRequestParams())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getReturnParams()), Task::getReturnParams, findOneRequest.getReturnParams())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getExecStatus()), Task::getExecStatus, findOneRequest.getExecStatus())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getFailureReason()), Task::getFailureReason, findOneRequest.getFailureReason())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStatus()), Task::getStatus, findOneRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getReserve()), Task::getReserve, findOneRequest.getReserve())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getRemark()), Task::getRemark, findOneRequest.getRemark())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), Task::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getAgentMemoryId()), Task::getAgentMemoryId, neRequest.getAgentMemoryId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTaskName()), Task::getTaskName, neRequest.getTaskName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getParentTaskId()), Task::getParentTaskId, neRequest.getParentTaskId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getNextTaskId()), Task::getNextTaskId, neRequest.getNextTaskId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStepType()), Task::getStepType, neRequest.getStepType())
                    .ne(ObjUtil.isNotEmpty(neRequest.getBranchCondition()), Task::getBranchCondition, neRequest.getBranchCondition())
                    .ne(ObjUtil.isNotEmpty(neRequest.getBranchRoute()), Task::getBranchRoute, neRequest.getBranchRoute())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRequestParams()), Task::getRequestParams, neRequest.getRequestParams())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReturnParams()), Task::getReturnParams, neRequest.getReturnParams())
                    .ne(ObjUtil.isNotEmpty(neRequest.getExecStatus()), Task::getExecStatus, neRequest.getExecStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getFailureReason()), Task::getFailureReason, neRequest.getFailureReason())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), Task::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReserve()), Task::getReserve, neRequest.getReserve())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRemark()), Task::getRemark, neRequest.getRemark());
        return repository.selectCount(queryWrapper);
    }

    @Override
    public Task findById(String id) {
        return repository.selectById(id);
    }

    @Override
    public void save(Task task) {
        repository.insert(task);
    }

    @Override
    public void updateById(Task task) {
        repository.updateById(task);
    }

    @Override
    public void updateById(List<Task> list) {
        repository.updateById(list);
    }

    @Override
    public void saves(List<Task> list) {
        if (CollectionUtil.isNotEmpty(list)) {
            repository.insert(list);
        }
    }

    @Override
    public void deleteByIds(List<String> ids) {
        repository.deleteByIds(ids);
    }

    @Override
    public List<Task> findAllByIds(List<String> ids) {
        return repository.selectBatchIds(ids);
    }

    @Override
    public void delete(DeleteTaskRequest request) {
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(request.getId()), Task::getId, request.getId())
                    .eq(ObjUtil.isNotEmpty(request.getAgentMemoryId()), Task::getAgentMemoryId, request.getAgentMemoryId())
                    .eq(ObjUtil.isNotEmpty(request.getTaskName()), Task::getTaskName, request.getTaskName())
                    .eq(ObjUtil.isNotEmpty(request.getParentTaskId()), Task::getParentTaskId, request.getParentTaskId())
                    .eq(ObjUtil.isNotEmpty(request.getNextTaskId()), Task::getNextTaskId, request.getNextTaskId())
                    .eq(ObjUtil.isNotEmpty(request.getStepType()), Task::getStepType, request.getStepType())
                    .eq(ObjUtil.isNotEmpty(request.getBranchCondition()), Task::getBranchCondition, request.getBranchCondition())
                    .eq(ObjUtil.isNotEmpty(request.getBranchRoute()), Task::getBranchRoute, request.getBranchRoute())
                    .eq(ObjUtil.isNotEmpty(request.getRequestParams()), Task::getRequestParams, request.getRequestParams())
                    .eq(ObjUtil.isNotEmpty(request.getReturnParams()), Task::getReturnParams, request.getReturnParams())
                    .eq(ObjUtil.isNotEmpty(request.getExecStatus()), Task::getExecStatus, request.getExecStatus())
                    .eq(ObjUtil.isNotEmpty(request.getFailureReason()), Task::getFailureReason, request.getFailureReason())
                    .eq(ObjUtil.isNotEmpty(request.getStatus()), Task::getStatus, request.getStatus()).eq(ObjUtil.isNotEmpty(request.getReserve()), Task::getReserve, request.getReserve())
                    .eq(ObjUtil.isNotEmpty(request.getRemark()), Task::getRemark, request.getRemark());
        repository.delete(queryWrapper);
    }
}

