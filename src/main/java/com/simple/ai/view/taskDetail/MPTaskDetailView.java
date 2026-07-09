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
                    .eq(ObjUtil.isNotEmpty(pageRequest.getSequence()), TaskDetail::getSequence, pageRequest.getSequence())
                    .like(ObjUtil.isNotEmpty(pageRequest.getStepName()), TaskDetail::getStepName, pageRequest.getStepName())
                    .like(ObjUtil.isNotEmpty(pageRequest.getStepType()), TaskDetail::getStepType, pageRequest.getStepType())
                    .like(ObjUtil.isNotEmpty(pageRequest.getStepContent()), TaskDetail::getStepContent, pageRequest.getStepContent())
                    .like(ObjUtil.isNotEmpty(pageRequest.getRequestParams()), TaskDetail::getRequestParams, pageRequest.getRequestParams())
                    .like(ObjUtil.isNotEmpty(pageRequest.getResponseParams()), TaskDetail::getResponseParams, pageRequest.getResponseParams())
                    .eq(ObjUtil.isNotEmpty(pageRequest.getExecutionStatus()), TaskDetail::getExecutionStatus, pageRequest.getExecutionStatus());
        return repository.selectPage(pageRequest.getPage(TaskDetail.class), queryWrapper);
    }

    @Override
    public List<TaskDetail> findAll(FindAllTaskDetailRequest findAllRequest, FindAllTaskDetailRequest neRequest) {
        LambdaQueryWrapper<TaskDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findAllRequest.getId()), TaskDetail::getId, findAllRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getTaskId()), TaskDetail::getTaskId, findAllRequest.getTaskId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getSequence()), TaskDetail::getSequence, findAllRequest.getSequence())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getStepName()), TaskDetail::getStepName, findAllRequest.getStepName())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getStepType()), TaskDetail::getStepType, findAllRequest.getStepType())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getStepContent()), TaskDetail::getStepContent, findAllRequest.getStepContent())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getRequestParams()), TaskDetail::getRequestParams, findAllRequest.getRequestParams())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getResponseParams()), TaskDetail::getResponseParams, findAllRequest.getResponseParams())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getExecutionStatus()), TaskDetail::getExecutionStatus, findAllRequest.getExecutionStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), TaskDetail::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTaskId()), TaskDetail::getTaskId, neRequest.getTaskId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getSequence()), TaskDetail::getSequence, neRequest.getSequence())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStepName()), TaskDetail::getStepName, neRequest.getStepName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStepType()), TaskDetail::getStepType, neRequest.getStepType())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStepContent()), TaskDetail::getStepContent, neRequest.getStepContent())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRequestParams()), TaskDetail::getRequestParams, neRequest.getRequestParams())
                    .ne(ObjUtil.isNotEmpty(neRequest.getResponseParams()), TaskDetail::getResponseParams, neRequest.getResponseParams())
                    .ne(ObjUtil.isNotEmpty(neRequest.getExecutionStatus()), TaskDetail::getExecutionStatus, neRequest.getExecutionStatus());

        return repository.selectList(queryWrapper);
    }

    @Override
    public TaskDetail findOne(FindOneTaskDetailRequest findOneRequest, FindOneTaskDetailRequest neRequest) {
        LambdaQueryWrapper<TaskDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), TaskDetail::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getTaskId()), TaskDetail::getTaskId, findOneRequest.getTaskId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getSequence()), TaskDetail::getSequence, findOneRequest.getSequence())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStepName()), TaskDetail::getStepName, findOneRequest.getStepName())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStepType()), TaskDetail::getStepType, findOneRequest.getStepType())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStepContent()), TaskDetail::getStepContent, findOneRequest.getStepContent())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getRequestParams()), TaskDetail::getRequestParams, findOneRequest.getRequestParams())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getResponseParams()), TaskDetail::getResponseParams, findOneRequest.getResponseParams())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getExecutionStatus()), TaskDetail::getExecutionStatus, findOneRequest.getExecutionStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), TaskDetail::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTaskId()), TaskDetail::getTaskId, neRequest.getTaskId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getSequence()), TaskDetail::getSequence, neRequest.getSequence())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStepName()), TaskDetail::getStepName, neRequest.getStepName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStepType()), TaskDetail::getStepType, neRequest.getStepType())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStepContent()), TaskDetail::getStepContent, neRequest.getStepContent())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRequestParams()), TaskDetail::getRequestParams, neRequest.getRequestParams())
                    .ne(ObjUtil.isNotEmpty(neRequest.getResponseParams()), TaskDetail::getResponseParams, neRequest.getResponseParams())
                    .ne(ObjUtil.isNotEmpty(neRequest.getExecutionStatus()), TaskDetail::getExecutionStatus, neRequest.getExecutionStatus());

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
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getSequence()), TaskDetail::getSequence, findOneRequest.getSequence())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStepName()), TaskDetail::getStepName, findOneRequest.getStepName())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStepType()), TaskDetail::getStepType, findOneRequest.getStepType())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStepContent()), TaskDetail::getStepContent, findOneRequest.getStepContent())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getRequestParams()), TaskDetail::getRequestParams, findOneRequest.getRequestParams())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getResponseParams()), TaskDetail::getResponseParams, findOneRequest.getResponseParams())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getExecutionStatus()), TaskDetail::getExecutionStatus, findOneRequest.getExecutionStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), TaskDetail::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTaskId()), TaskDetail::getTaskId, neRequest.getTaskId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getSequence()), TaskDetail::getSequence, neRequest.getSequence())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStepName()), TaskDetail::getStepName, neRequest.getStepName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStepType()), TaskDetail::getStepType, neRequest.getStepType())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStepContent()), TaskDetail::getStepContent, neRequest.getStepContent())
                    .ne(ObjUtil.isNotEmpty(neRequest.getRequestParams()), TaskDetail::getRequestParams, neRequest.getRequestParams())
                    .ne(ObjUtil.isNotEmpty(neRequest.getResponseParams()), TaskDetail::getResponseParams, neRequest.getResponseParams())
                    .ne(ObjUtil.isNotEmpty(neRequest.getExecutionStatus()), TaskDetail::getExecutionStatus, neRequest.getExecutionStatus());
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
                    .eq(ObjUtil.isNotEmpty(request.getSequence()), TaskDetail::getSequence, request.getSequence())
                    .eq(ObjUtil.isNotEmpty(request.getStepName()), TaskDetail::getStepName, request.getStepName())
                    .eq(ObjUtil.isNotEmpty(request.getStepType()), TaskDetail::getStepType, request.getStepType())
                    .eq(ObjUtil.isNotEmpty(request.getStepContent()), TaskDetail::getStepContent, request.getStepContent())
                    .eq(ObjUtil.isNotEmpty(request.getRequestParams()), TaskDetail::getRequestParams, request.getRequestParams())
                    .eq(ObjUtil.isNotEmpty(request.getResponseParams()), TaskDetail::getResponseParams, request.getResponseParams())
                    .eq(ObjUtil.isNotEmpty(request.getExecutionStatus()), TaskDetail::getExecutionStatus, request.getExecutionStatus());
        repository.delete(queryWrapper);
    }
}

