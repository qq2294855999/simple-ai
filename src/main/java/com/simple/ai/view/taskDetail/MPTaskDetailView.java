package com.simple.ai.view.taskDetail;

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
import com.simple.ai.common.view.taskDetail.TaskDetailView;
import com.simple.ai.common.entity.taskDetail.TaskDetail;
import com.simple.ai.common.dto.taskDetail.PageTaskDetailRequest;
import com.simple.ai.common.dto.taskDetail.FindOneTaskDetailRequest;
import com.simple.ai.common.dto.taskDetail.FindAllTaskDetailRequest;
import com.simple.ai.common.dto.taskDetail.DeleteTaskDetailRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.simple.common.core.utils.JsonUtils;

/**
 * 任务详情(task_detail)数据库视图实现
 *
 * @author qty
 */
@Component
class MPTaskDetailView implements TaskDetailView {

    @Autowired
    private TaskDetailRepository repository;

    @Override
    public IPage<TaskDetail> findAll(PageTaskDetailRequest pageRequest) {
        LambdaQueryWrapper<TaskDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(ObjUtil.isNotEmpty(pageRequest.getTaskId()), TaskDetail::getTaskId, pageRequest.getTaskId())
                    .like(ObjUtil.isNotEmpty(pageRequest.getTaskName()), TaskDetail::getTaskName, pageRequest.getTaskName())
                    .like(ObjUtil.isNotEmpty(pageRequest.getParentTaskId()), TaskDetail::getParentTaskId, pageRequest.getParentTaskId())
                    .like(ObjUtil.isNotEmpty(pageRequest.getNextTaskId()), TaskDetail::getNextTaskId, pageRequest.getNextTaskId())
                    .like(ObjUtil.isNotEmpty(pageRequest.getStepType()), TaskDetail::getStepType, pageRequest.getStepType())
                    .like(ObjUtil.isNotEmpty(pageRequest.getBranchCondition()), TaskDetail::getBranchCondition, pageRequest.getBranchCondition())
                    .like(ObjUtil.isNotEmpty(pageRequest.getBranchRoute()), TaskDetail::getBranchRoute, pageRequest.getBranchRoute())
                    .like(ObjUtil.isNotEmpty(pageRequest.getRequestParams()), TaskDetail::getRequestParams, pageRequest.getRequestParams())
                    .like(ObjUtil.isNotEmpty(pageRequest.getReturnParams()), TaskDetail::getReturnParams, pageRequest.getReturnParams())
                    .like(ObjUtil.isNotEmpty(pageRequest.getExecStatus()), TaskDetail::getExecStatus, pageRequest.getExecStatus())
                    .eq(ObjUtil.isNotEmpty(pageRequest.getStatus()), TaskDetail::getStatus, pageRequest.getStatus())
                    .like(ObjUtil.isNotEmpty(pageRequest.getReserver()), TaskDetail::getReserver, pageRequest.getReserver())
                    .like(ObjUtil.isNotEmpty(pageRequest.getRemark()), TaskDetail::getRemark, pageRequest.getRemark());
        return repository.selectPage(pageRequest.getPage(TaskDetail.class), queryWrapper);
    }

    @Override
    public List<TaskDetail> findAll(FindAllTaskDetailRequest findAllRequest, FindAllTaskDetailRequest neRequest) {
        LambdaQueryWrapper<TaskDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findAllRequest.getId()), TaskDetail::getId, findAllRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getTaskId()), TaskDetail::getTaskId, findAllRequest.getTaskId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getTaskName()), TaskDetail::getTaskName, findAllRequest.getTaskName())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getParentTaskId()), TaskDetail::getParentTaskId, findAllRequest.getParentTaskId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getNextTaskId()), TaskDetail::getNextTaskId, findAllRequest.getNextTaskId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getStepType()), TaskDetail::getStepType, findAllRequest.getStepType())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getBranchCondition()), TaskDetail::getBranchCondition, findAllRequest.getBranchCondition())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getBranchRoute()), TaskDetail::getBranchRoute, findAllRequest.getBranchRoute())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getRequestParams()), TaskDetail::getRequestParams, findAllRequest.getRequestParams())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getReturnParams()), TaskDetail::getReturnParams, findAllRequest.getReturnParams())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getExecStatus()), TaskDetail::getExecStatus, findAllRequest.getExecStatus())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getStatus()), TaskDetail::getStatus, findAllRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getReserver()), TaskDetail::getReserver, findAllRequest.getReserver())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getRemark()), TaskDetail::getRemark, findAllRequest.getRemark())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), TaskDetail::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTaskId()), TaskDetail::getTaskId, neRequest.getTaskId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTaskName()), TaskDetail::getTaskName, neRequest.getTaskName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getParentTaskId()), TaskDetail::getParentTaskId, neRequest.getParentTaskId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getNextTaskId()), TaskDetail::getNextTaskId, neRequest.getNextTaskId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStepType()), TaskDetail::getStepType, neRequest.getStepType())
                    .ne(ObjUtil.isNotEmpty(neRequest.getBranchCondition()), TaskDetail::getBranchCondition, neRequest.getBranchCondition())
                    .ne(ObjUtil.isNotEmpty(neRequest.getBranchRoute()), TaskDetail::getBranchRoute, neRequest.getBranchRoute())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRequestParams()), TaskDetail::getRequestParams, neRequest.getRequestParams())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReturnParams()), TaskDetail::getReturnParams, neRequest.getReturnParams())
                    .ne(ObjUtil.isNotEmpty(neRequest.getExecStatus()), TaskDetail::getExecStatus, neRequest.getExecStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), TaskDetail::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReserver()), TaskDetail::getReserver, neRequest.getReserver())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRemark()), TaskDetail::getRemark, neRequest.getRemark());

        return repository.selectList(queryWrapper);
    }

    @Override
    public TaskDetail findOne(FindOneTaskDetailRequest findOneRequest, FindOneTaskDetailRequest neRequest) {
        LambdaQueryWrapper<TaskDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), TaskDetail::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getTaskId()), TaskDetail::getTaskId, findOneRequest.getTaskId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getTaskName()), TaskDetail::getTaskName, findOneRequest.getTaskName())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getParentTaskId()), TaskDetail::getParentTaskId, findOneRequest.getParentTaskId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getNextTaskId()), TaskDetail::getNextTaskId, findOneRequest.getNextTaskId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStepType()), TaskDetail::getStepType, findOneRequest.getStepType())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getBranchCondition()), TaskDetail::getBranchCondition, findOneRequest.getBranchCondition())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getBranchRoute()), TaskDetail::getBranchRoute, findOneRequest.getBranchRoute())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getRequestParams()), TaskDetail::getRequestParams, findOneRequest.getRequestParams())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getReturnParams()), TaskDetail::getReturnParams, findOneRequest.getReturnParams())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getExecStatus()), TaskDetail::getExecStatus, findOneRequest.getExecStatus())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStatus()), TaskDetail::getStatus, findOneRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getReserver()), TaskDetail::getReserver, findOneRequest.getReserver())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getRemark()), TaskDetail::getRemark, findOneRequest.getRemark())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), TaskDetail::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTaskId()), TaskDetail::getTaskId, neRequest.getTaskId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTaskName()), TaskDetail::getTaskName, neRequest.getTaskName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getParentTaskId()), TaskDetail::getParentTaskId, neRequest.getParentTaskId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getNextTaskId()), TaskDetail::getNextTaskId, neRequest.getNextTaskId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStepType()), TaskDetail::getStepType, neRequest.getStepType())
                    .ne(ObjUtil.isNotEmpty(neRequest.getBranchCondition()), TaskDetail::getBranchCondition, neRequest.getBranchCondition())
                    .ne(ObjUtil.isNotEmpty(neRequest.getBranchRoute()), TaskDetail::getBranchRoute, neRequest.getBranchRoute())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRequestParams()), TaskDetail::getRequestParams, neRequest.getRequestParams())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReturnParams()), TaskDetail::getReturnParams, neRequest.getReturnParams())
                    .ne(ObjUtil.isNotEmpty(neRequest.getExecStatus()), TaskDetail::getExecStatus, neRequest.getExecStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), TaskDetail::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReserver()), TaskDetail::getReserver, neRequest.getReserver())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRemark()), TaskDetail::getRemark, neRequest.getRemark());

        List<TaskDetail> list = repository.selectList(queryWrapper);
        if (list.isEmpty()) {
            return null;
        } else if (list.size() > 1) {
            AssertUtils.error("数据异常", "参数为[{}]查询只需要一条数据，但是查询出来多条", JsonUtils.toJsonStr(findOneRequest));
        }
        return list.get(0);
    }

    @Override
    public Long findCount(FindOneTaskDetailRequest findOneRequest, FindOneTaskDetailRequest neRequest) {
        LambdaQueryWrapper<TaskDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), TaskDetail::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getTaskId()), TaskDetail::getTaskId, findOneRequest.getTaskId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getTaskName()), TaskDetail::getTaskName, findOneRequest.getTaskName())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getParentTaskId()), TaskDetail::getParentTaskId, findOneRequest.getParentTaskId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getNextTaskId()), TaskDetail::getNextTaskId, findOneRequest.getNextTaskId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStepType()), TaskDetail::getStepType, findOneRequest.getStepType())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getBranchCondition()), TaskDetail::getBranchCondition, findOneRequest.getBranchCondition())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getBranchRoute()), TaskDetail::getBranchRoute, findOneRequest.getBranchRoute())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getRequestParams()), TaskDetail::getRequestParams, findOneRequest.getRequestParams())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getReturnParams()), TaskDetail::getReturnParams, findOneRequest.getReturnParams())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getExecStatus()), TaskDetail::getExecStatus, findOneRequest.getExecStatus())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStatus()), TaskDetail::getStatus, findOneRequest.getStatus())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getReserver()), TaskDetail::getReserver, findOneRequest.getReserver())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getRemark()), TaskDetail::getRemark, findOneRequest.getRemark())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), TaskDetail::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTaskId()), TaskDetail::getTaskId, neRequest.getTaskId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTaskName()), TaskDetail::getTaskName, neRequest.getTaskName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getParentTaskId()), TaskDetail::getParentTaskId, neRequest.getParentTaskId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getNextTaskId()), TaskDetail::getNextTaskId, neRequest.getNextTaskId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStepType()), TaskDetail::getStepType, neRequest.getStepType())
                    .ne(ObjUtil.isNotEmpty(neRequest.getBranchCondition()), TaskDetail::getBranchCondition, neRequest.getBranchCondition())
                    .ne(ObjUtil.isNotEmpty(neRequest.getBranchRoute()), TaskDetail::getBranchRoute, neRequest.getBranchRoute())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRequestParams()), TaskDetail::getRequestParams, neRequest.getRequestParams())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReturnParams()), TaskDetail::getReturnParams, neRequest.getReturnParams())
                    .ne(ObjUtil.isNotEmpty(neRequest.getExecStatus()), TaskDetail::getExecStatus, neRequest.getExecStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), TaskDetail::getStatus, neRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getReserver()), TaskDetail::getReserver, neRequest.getReserver())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRemark()), TaskDetail::getRemark, neRequest.getRemark());
        return repository.selectCount(queryWrapper);
    }

    @Override
    public TaskDetail findById(String id) {
        return repository.selectById(id);
    }

    @Override
    public void save(TaskDetail taskDetail) {
        repository.insert(taskDetail);
    }

    @Override
    public void updateById(TaskDetail taskDetail) {
        repository.updateById(taskDetail);
    }

    @Override
    public void updateById(List<TaskDetail> list) {
        repository.updateById(list);
    }

    @Override
    public void saves(List<TaskDetail> list) {
        if (CollectionUtil.isNotEmpty(list)) {
            repository.insert(list);
        }
    }

    @Override
    public void deleteByIds(List<String> ids) {
        repository.deleteByIds(ids);
    }

    @Override
    public void delete(DeleteTaskDetailRequest request) {
        LambdaQueryWrapper<TaskDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(request.getId()), TaskDetail::getId, request.getId())
                    .eq(ObjUtil.isNotEmpty(request.getTaskId()), TaskDetail::getTaskId, request.getTaskId())
                    .eq(ObjUtil.isNotEmpty(request.getTaskName()), TaskDetail::getTaskName, request.getTaskName())
                    .eq(ObjUtil.isNotEmpty(request.getParentTaskId()), TaskDetail::getParentTaskId, request.getParentTaskId())
                    .eq(ObjUtil.isNotEmpty(request.getNextTaskId()), TaskDetail::getNextTaskId, request.getNextTaskId())
                    .eq(ObjUtil.isNotEmpty(request.getStepType()), TaskDetail::getStepType, request.getStepType())
                    .eq(ObjUtil.isNotEmpty(request.getBranchCondition()), TaskDetail::getBranchCondition, request.getBranchCondition())
                    .eq(ObjUtil.isNotEmpty(request.getBranchRoute()), TaskDetail::getBranchRoute, request.getBranchRoute())
                    .eq(ObjUtil.isNotEmpty(request.getRequestParams()), TaskDetail::getRequestParams, request.getRequestParams())
                    .eq(ObjUtil.isNotEmpty(request.getReturnParams()), TaskDetail::getReturnParams, request.getReturnParams())
                    .eq(ObjUtil.isNotEmpty(request.getExecStatus()), TaskDetail::getExecStatus, request.getExecStatus())
                    .eq(ObjUtil.isNotEmpty(request.getStatus()), TaskDetail::getStatus, request.getStatus())
                    .eq(ObjUtil.isNotEmpty(request.getReserver()), TaskDetail::getReserver, request.getReserver())
                    .eq(ObjUtil.isNotEmpty(request.getRemark()), TaskDetail::getRemark, request.getRemark());
        repository.delete(queryWrapper);
    }
}

