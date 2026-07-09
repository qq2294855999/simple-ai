package com.simple.ai.view.task;

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
import com.simple.ai.common.view.task.TaskView;
import com.simple.ai.common.entity.task.Task;
import com.simple.ai.common.dto.task.PageTaskRequest;
import com.simple.ai.common.dto.task.FindOneTaskRequest;
import com.simple.ai.common.dto.task.FindAllTaskRequest;
import com.simple.ai.common.dto.task.DeleteTaskRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.simple.common.core.utils.JsonUtils;

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
                    .eq(ObjUtil.isNotEmpty(pageRequest.getExecutionStatus()), Task::getExecutionStatus, pageRequest.getExecutionStatus());
        return repository.selectPage(pageRequest.getPage(Task.class), queryWrapper);
    }

    @Override
    public List<Task> findAll(FindAllTaskRequest findAllRequest, FindAllTaskRequest neRequest) {
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findAllRequest.getId()), Task::getId, findAllRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getAgentMemoryId()), Task::getAgentMemoryId, findAllRequest.getAgentMemoryId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getTaskName()), Task::getTaskName, findAllRequest.getTaskName())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getExecutionStatus()), Task::getExecutionStatus, findAllRequest.getExecutionStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), Task::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getAgentMemoryId()), Task::getAgentMemoryId, neRequest.getAgentMemoryId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTaskName()), Task::getTaskName, neRequest.getTaskName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getExecutionStatus()), Task::getExecutionStatus, neRequest.getExecutionStatus());

        return repository.selectList(queryWrapper);
    }

    @Override
    public Task findOne(FindOneTaskRequest findOneRequest, FindOneTaskRequest neRequest) {
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), Task::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getAgentMemoryId()), Task::getAgentMemoryId, findOneRequest.getAgentMemoryId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getTaskName()), Task::getTaskName, findOneRequest.getTaskName())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getExecutionStatus()), Task::getExecutionStatus, findOneRequest.getExecutionStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), Task::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getAgentMemoryId()), Task::getAgentMemoryId, neRequest.getAgentMemoryId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTaskName()), Task::getTaskName, neRequest.getTaskName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getExecutionStatus()), Task::getExecutionStatus, neRequest.getExecutionStatus());

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
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getExecutionStatus()), Task::getExecutionStatus, findOneRequest.getExecutionStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), Task::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getAgentMemoryId()), Task::getAgentMemoryId, neRequest.getAgentMemoryId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTaskName()), Task::getTaskName, neRequest.getTaskName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getExecutionStatus()), Task::getExecutionStatus, neRequest.getExecutionStatus());
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
    public void delete(DeleteTaskRequest request) {
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(request.getId()), Task::getId, request.getId())
                    .eq(ObjUtil.isNotEmpty(request.getAgentMemoryId()), Task::getAgentMemoryId, request.getAgentMemoryId())
                    .eq(ObjUtil.isNotEmpty(request.getTaskName()), Task::getTaskName, request.getTaskName())
                    .eq(ObjUtil.isNotEmpty(request.getExecutionStatus()), Task::getExecutionStatus, request.getExecutionStatus());
        repository.delete(queryWrapper);
    }
}

