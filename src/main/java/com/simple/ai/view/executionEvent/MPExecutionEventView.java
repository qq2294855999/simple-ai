package com.simple.ai.view.executionEvent;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.executionEvent.DeleteExecutionEventRequest;
import com.simple.ai.common.dto.executionEvent.FindAllExecutionEventRequest;
import com.simple.ai.common.dto.executionEvent.FindOneExecutionEventRequest;
import com.simple.ai.common.dto.executionEvent.PageExecutionEventRequest;
import com.simple.ai.common.entity.executionEvent.ExecutionEvent;
import com.simple.ai.common.view.executionEvent.ExecutionEventView;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.core.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 执行事件(execution_event)数据库视图实现
 *
 * @author qty
 */
@Component
class MPExecutionEventView implements ExecutionEventView {

    @Autowired
    private ExecutionEventRepository repository;

    @Override
    public IPage<ExecutionEvent> findAll(PageExecutionEventRequest pageRequest) {
        LambdaQueryWrapper<ExecutionEvent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(ObjUtil.isNotEmpty(pageRequest.getTurnId()), ExecutionEvent::getTurnId, pageRequest.getTurnId())
                    .like(ObjUtil.isNotEmpty(pageRequest.getTaskId()), ExecutionEvent::getTaskId, pageRequest.getTaskId())
                    .like(ObjUtil.isNotEmpty(pageRequest.getTaskDetailId()), ExecutionEvent::getTaskDetailId, pageRequest.getTaskDetailId())
                    .like(ObjUtil.isNotEmpty(pageRequest.getEventType()), ExecutionEvent::getEventType, pageRequest.getEventType())
                    .like(ObjUtil.isNotEmpty(pageRequest.getStepName()), ExecutionEvent::getStepName, pageRequest.getStepName())
                    .like(ObjUtil.isNotEmpty(pageRequest.getCommandName()), ExecutionEvent::getCommandName, pageRequest.getCommandName())
                    .like(ObjUtil.isNotEmpty(pageRequest.getCommandContent()), ExecutionEvent::getCommandContent, pageRequest.getCommandContent())
                    .like(ObjUtil.isNotEmpty(pageRequest.getResponseContent()), ExecutionEvent::getResponseContent, pageRequest.getResponseContent())
                    .like(ObjUtil.isNotEmpty(pageRequest.getFailureReason()), ExecutionEvent::getFailureReason, pageRequest.getFailureReason())
                    .eq(ObjUtil.isNotEmpty(pageRequest.getSequenceNo()), ExecutionEvent::getSequenceNo, pageRequest.getSequenceNo())
                    .eq(ObjUtil.isNotEmpty(pageRequest.getStartedAt()), ExecutionEvent::getStartedAt, pageRequest.getStartedAt())
                    .eq(ObjUtil.isNotEmpty(pageRequest.getFinishedAt()), ExecutionEvent::getFinishedAt, pageRequest.getFinishedAt())
                    .like(ObjUtil.isNotEmpty(pageRequest.getAtomicCommandId()), ExecutionEvent::getAtomicCommandId, pageRequest.getAtomicCommandId())
                    .like(ObjUtil.isNotEmpty(pageRequest.getAtomicCommandCode()), ExecutionEvent::getAtomicCommandCode, pageRequest.getAtomicCommandCode())
                    .like(ObjUtil.isNotEmpty(pageRequest.getProviderId()), ExecutionEvent::getProviderId, pageRequest.getProviderId())
                    .like(ObjUtil.isNotEmpty(pageRequest.getProviderName()), ExecutionEvent::getProviderName, pageRequest.getProviderName())
                    .like(ObjUtil.isNotEmpty(pageRequest.getModelId()), ExecutionEvent::getModelId, pageRequest.getModelId())
                    .like(ObjUtil.isNotEmpty(pageRequest.getModelCode()), ExecutionEvent::getModelCode, pageRequest.getModelCode())
                    .like(ObjUtil.isNotEmpty(pageRequest.getStatus()), ExecutionEvent::getStatus, pageRequest.getStatus());
        return repository.selectPage(pageRequest.getPage(ExecutionEvent.class), queryWrapper);
    }

    @Override
    public List<ExecutionEvent> findAll(FindAllExecutionEventRequest findAllRequest, FindAllExecutionEventRequest neRequest) {
        LambdaQueryWrapper<ExecutionEvent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findAllRequest.getId()), ExecutionEvent::getId, findAllRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getTurnId()), ExecutionEvent::getTurnId, findAllRequest.getTurnId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getTaskId()), ExecutionEvent::getTaskId, findAllRequest.getTaskId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getTaskDetailId()), ExecutionEvent::getTaskDetailId, findAllRequest.getTaskDetailId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getEventType()), ExecutionEvent::getEventType, findAllRequest.getEventType())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getStepName()), ExecutionEvent::getStepName, findAllRequest.getStepName())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getCommandName()), ExecutionEvent::getCommandName, findAllRequest.getCommandName())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getCommandContent()), ExecutionEvent::getCommandContent, findAllRequest.getCommandContent())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getResponseContent()), ExecutionEvent::getResponseContent, findAllRequest.getResponseContent())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getFailureReason()), ExecutionEvent::getFailureReason, findAllRequest.getFailureReason())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getSequenceNo()), ExecutionEvent::getSequenceNo, findAllRequest.getSequenceNo())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getStartedAt()), ExecutionEvent::getStartedAt, findAllRequest.getStartedAt())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getFinishedAt()), ExecutionEvent::getFinishedAt, findAllRequest.getFinishedAt())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getAtomicCommandId()), ExecutionEvent::getAtomicCommandId, findAllRequest.getAtomicCommandId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getAtomicCommandCode()), ExecutionEvent::getAtomicCommandCode, findAllRequest.getAtomicCommandCode())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getProviderId()), ExecutionEvent::getProviderId, findAllRequest.getProviderId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getProviderName()), ExecutionEvent::getProviderName, findAllRequest.getProviderName())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getModelId()), ExecutionEvent::getModelId, findAllRequest.getModelId())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getModelCode()), ExecutionEvent::getModelCode, findAllRequest.getModelCode())
                    .eq(ObjUtil.isNotEmpty(findAllRequest.getStatus()), ExecutionEvent::getStatus, findAllRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), ExecutionEvent::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTurnId()), ExecutionEvent::getTurnId, neRequest.getTurnId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTaskId()), ExecutionEvent::getTaskId, neRequest.getTaskId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTaskDetailId()), ExecutionEvent::getTaskDetailId, neRequest.getTaskDetailId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getEventType()), ExecutionEvent::getEventType, neRequest.getEventType())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStepName()), ExecutionEvent::getStepName, neRequest.getStepName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getCommandName()), ExecutionEvent::getCommandName, neRequest.getCommandName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getCommandContent()), ExecutionEvent::getCommandContent, neRequest.getCommandContent())
                    .ne(ObjUtil.isNotEmpty(neRequest.getResponseContent()), ExecutionEvent::getResponseContent, neRequest.getResponseContent())
                    .ne(ObjUtil.isNotEmpty(neRequest.getFailureReason()), ExecutionEvent::getFailureReason, neRequest.getFailureReason())
                    .ne(ObjUtil.isNotEmpty(neRequest.getSequenceNo()), ExecutionEvent::getSequenceNo, neRequest.getSequenceNo())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStartedAt()), ExecutionEvent::getStartedAt, neRequest.getStartedAt())
                    .ne(ObjUtil.isNotEmpty(neRequest.getFinishedAt()), ExecutionEvent::getFinishedAt, neRequest.getFinishedAt())
                    .ne(ObjUtil.isNotEmpty(neRequest.getAtomicCommandId()), ExecutionEvent::getAtomicCommandId, neRequest.getAtomicCommandId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getAtomicCommandCode()), ExecutionEvent::getAtomicCommandCode, neRequest.getAtomicCommandCode())
                    .ne(ObjUtil.isNotEmpty(neRequest.getProviderId()), ExecutionEvent::getProviderId, neRequest.getProviderId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getProviderName()), ExecutionEvent::getProviderName, neRequest.getProviderName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getModelId()), ExecutionEvent::getModelId, neRequest.getModelId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getModelCode()), ExecutionEvent::getModelCode, neRequest.getModelCode())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), ExecutionEvent::getStatus, neRequest.getStatus());

        return repository.selectList(queryWrapper);
    }

    @Override
    public ExecutionEvent findOne(FindOneExecutionEventRequest findOneRequest, FindOneExecutionEventRequest neRequest) {
        LambdaQueryWrapper<ExecutionEvent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), ExecutionEvent::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getTurnId()), ExecutionEvent::getTurnId, findOneRequest.getTurnId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getTaskId()), ExecutionEvent::getTaskId, findOneRequest.getTaskId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getTaskDetailId()), ExecutionEvent::getTaskDetailId, findOneRequest.getTaskDetailId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getEventType()), ExecutionEvent::getEventType, findOneRequest.getEventType())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStepName()), ExecutionEvent::getStepName, findOneRequest.getStepName())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getCommandName()), ExecutionEvent::getCommandName, findOneRequest.getCommandName())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getCommandContent()), ExecutionEvent::getCommandContent, findOneRequest.getCommandContent())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getResponseContent()), ExecutionEvent::getResponseContent, findOneRequest.getResponseContent())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getFailureReason()), ExecutionEvent::getFailureReason, findOneRequest.getFailureReason())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getSequenceNo()), ExecutionEvent::getSequenceNo, findOneRequest.getSequenceNo())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStartedAt()), ExecutionEvent::getStartedAt, findOneRequest.getStartedAt())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getFinishedAt()), ExecutionEvent::getFinishedAt, findOneRequest.getFinishedAt())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getAtomicCommandId()), ExecutionEvent::getAtomicCommandId, findOneRequest.getAtomicCommandId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getAtomicCommandCode()), ExecutionEvent::getAtomicCommandCode, findOneRequest.getAtomicCommandCode())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getProviderId()), ExecutionEvent::getProviderId, findOneRequest.getProviderId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getProviderName()), ExecutionEvent::getProviderName, findOneRequest.getProviderName())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getModelId()), ExecutionEvent::getModelId, findOneRequest.getModelId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getModelCode()), ExecutionEvent::getModelCode, findOneRequest.getModelCode())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStatus()), ExecutionEvent::getStatus, findOneRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), ExecutionEvent::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTurnId()), ExecutionEvent::getTurnId, neRequest.getTurnId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTaskId()), ExecutionEvent::getTaskId, neRequest.getTaskId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTaskDetailId()), ExecutionEvent::getTaskDetailId, neRequest.getTaskDetailId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getEventType()), ExecutionEvent::getEventType, neRequest.getEventType())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStepName()), ExecutionEvent::getStepName, neRequest.getStepName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getCommandName()), ExecutionEvent::getCommandName, neRequest.getCommandName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getCommandContent()), ExecutionEvent::getCommandContent, neRequest.getCommandContent())
                    .ne(ObjUtil.isNotEmpty(neRequest.getResponseContent()), ExecutionEvent::getResponseContent, neRequest.getResponseContent())
                    .ne(ObjUtil.isNotEmpty(neRequest.getFailureReason()), ExecutionEvent::getFailureReason, neRequest.getFailureReason())
                    .ne(ObjUtil.isNotEmpty(neRequest.getSequenceNo()), ExecutionEvent::getSequenceNo, neRequest.getSequenceNo())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStartedAt()), ExecutionEvent::getStartedAt, neRequest.getStartedAt())
                    .ne(ObjUtil.isNotEmpty(neRequest.getFinishedAt()), ExecutionEvent::getFinishedAt, neRequest.getFinishedAt())
                    .ne(ObjUtil.isNotEmpty(neRequest.getAtomicCommandId()), ExecutionEvent::getAtomicCommandId, neRequest.getAtomicCommandId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getAtomicCommandCode()), ExecutionEvent::getAtomicCommandCode, neRequest.getAtomicCommandCode())
                    .ne(ObjUtil.isNotEmpty(neRequest.getProviderId()), ExecutionEvent::getProviderId, neRequest.getProviderId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getProviderName()), ExecutionEvent::getProviderName, neRequest.getProviderName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getModelId()), ExecutionEvent::getModelId, neRequest.getModelId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getModelCode()), ExecutionEvent::getModelCode, neRequest.getModelCode())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), ExecutionEvent::getStatus, neRequest.getStatus());

        List<ExecutionEvent> list = repository.selectList(queryWrapper);
        if (list.isEmpty()) {
            return null;
        } else if (list.size() > 1) {
            AssertUtils.error("数据异常", "参数为[{}]查询只需要一条数据，但是查询出来多条", JsonUtils.toJsonStr(findOneRequest));
        }
        return list.get(0);
    }

    @Override
    public Long findCount(FindOneExecutionEventRequest findOneRequest, FindOneExecutionEventRequest neRequest) {
        LambdaQueryWrapper<ExecutionEvent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(findOneRequest.getId()), ExecutionEvent::getId, findOneRequest.getId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getTurnId()), ExecutionEvent::getTurnId, findOneRequest.getTurnId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getTaskId()), ExecutionEvent::getTaskId, findOneRequest.getTaskId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getTaskDetailId()), ExecutionEvent::getTaskDetailId, findOneRequest.getTaskDetailId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getEventType()), ExecutionEvent::getEventType, findOneRequest.getEventType())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStepName()), ExecutionEvent::getStepName, findOneRequest.getStepName())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getCommandName()), ExecutionEvent::getCommandName, findOneRequest.getCommandName())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getCommandContent()), ExecutionEvent::getCommandContent, findOneRequest.getCommandContent())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getResponseContent()), ExecutionEvent::getResponseContent, findOneRequest.getResponseContent())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getFailureReason()), ExecutionEvent::getFailureReason, findOneRequest.getFailureReason())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getSequenceNo()), ExecutionEvent::getSequenceNo, findOneRequest.getSequenceNo())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStartedAt()), ExecutionEvent::getStartedAt, findOneRequest.getStartedAt())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getFinishedAt()), ExecutionEvent::getFinishedAt, findOneRequest.getFinishedAt())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getAtomicCommandId()), ExecutionEvent::getAtomicCommandId, findOneRequest.getAtomicCommandId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getAtomicCommandCode()), ExecutionEvent::getAtomicCommandCode, findOneRequest.getAtomicCommandCode())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getProviderId()), ExecutionEvent::getProviderId, findOneRequest.getProviderId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getProviderName()), ExecutionEvent::getProviderName, findOneRequest.getProviderName())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getModelId()), ExecutionEvent::getModelId, findOneRequest.getModelId())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getModelCode()), ExecutionEvent::getModelCode, findOneRequest.getModelCode())
                    .eq(ObjUtil.isNotEmpty(findOneRequest.getStatus()), ExecutionEvent::getStatus, findOneRequest.getStatus())
                    .ne(ObjUtil.isNotEmpty(neRequest.getId()), ExecutionEvent::getId, neRequest.getId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTurnId()), ExecutionEvent::getTurnId, neRequest.getTurnId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTaskId()), ExecutionEvent::getTaskId, neRequest.getTaskId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getTaskDetailId()), ExecutionEvent::getTaskDetailId, neRequest.getTaskDetailId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getEventType()), ExecutionEvent::getEventType, neRequest.getEventType())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStepName()), ExecutionEvent::getStepName, neRequest.getStepName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getCommandName()), ExecutionEvent::getCommandName, neRequest.getCommandName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getCommandContent()), ExecutionEvent::getCommandContent, neRequest.getCommandContent())
                    .ne(ObjUtil.isNotEmpty(neRequest.getResponseContent()), ExecutionEvent::getResponseContent, neRequest.getResponseContent())
                    .ne(ObjUtil.isNotEmpty(neRequest.getFailureReason()), ExecutionEvent::getFailureReason, neRequest.getFailureReason())
                    .ne(ObjUtil.isNotEmpty(neRequest.getSequenceNo()), ExecutionEvent::getSequenceNo, neRequest.getSequenceNo())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStartedAt()), ExecutionEvent::getStartedAt, neRequest.getStartedAt())
                    .ne(ObjUtil.isNotEmpty(neRequest.getFinishedAt()), ExecutionEvent::getFinishedAt, neRequest.getFinishedAt())
                    .ne(ObjUtil.isNotEmpty(neRequest.getAtomicCommandId()), ExecutionEvent::getAtomicCommandId, neRequest.getAtomicCommandId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getAtomicCommandCode()), ExecutionEvent::getAtomicCommandCode, neRequest.getAtomicCommandCode())
                    .ne(ObjUtil.isNotEmpty(neRequest.getProviderId()), ExecutionEvent::getProviderId, neRequest.getProviderId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getProviderName()), ExecutionEvent::getProviderName, neRequest.getProviderName())
                    .ne(ObjUtil.isNotEmpty(neRequest.getModelId()), ExecutionEvent::getModelId, neRequest.getModelId())
                    .ne(ObjUtil.isNotEmpty(neRequest.getModelCode()), ExecutionEvent::getModelCode, neRequest.getModelCode())
                    .ne(ObjUtil.isNotEmpty(neRequest.getStatus()), ExecutionEvent::getStatus, neRequest.getStatus());
        return repository.selectCount(queryWrapper);
    }

    @Override
    public ExecutionEvent findById(String id) {
        return repository.selectById(id);
    }

    @Override
    public void save(ExecutionEvent executionEvent) {
        repository.insert(executionEvent);
    }

    @Override
    public void updateById(ExecutionEvent executionEvent) {
        repository.updateById(executionEvent);
    }

    @Override
    public void updateById(List<ExecutionEvent> list) {
        repository.updateById(list);
    }

    @Override
    public void saves(List<ExecutionEvent> list) {
        if (CollectionUtil.isNotEmpty(list)) {
            repository.insert(list);
        }
    }

    @Override
    public void deleteByIds(List<String> ids) {
        repository.deleteByIds(ids);
    }

    @Override
    public List<ExecutionEvent> findAllByTaskIds(List<String> taskIds) {
        if (CollectionUtil.isEmpty(taskIds)) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<ExecutionEvent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(ExecutionEvent::getTaskId, taskIds).orderByAsc(ExecutionEvent::getSequenceNo);
        return repository.selectList(queryWrapper);
    }

    @Override
    public void delete(DeleteExecutionEventRequest request) {
        LambdaQueryWrapper<ExecutionEvent> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotEmpty(request.getId()), ExecutionEvent::getId, request.getId())
                    .eq(ObjUtil.isNotEmpty(request.getTurnId()), ExecutionEvent::getTurnId, request.getTurnId())
                    .eq(ObjUtil.isNotEmpty(request.getTaskId()), ExecutionEvent::getTaskId, request.getTaskId())
                    .eq(ObjUtil.isNotEmpty(request.getTaskDetailId()), ExecutionEvent::getTaskDetailId, request.getTaskDetailId())
                    .eq(ObjUtil.isNotEmpty(request.getEventType()), ExecutionEvent::getEventType, request.getEventType())
                    .eq(ObjUtil.isNotEmpty(request.getStepName()), ExecutionEvent::getStepName, request.getStepName())
                    .eq(ObjUtil.isNotEmpty(request.getCommandName()), ExecutionEvent::getCommandName, request.getCommandName())
                    .eq(ObjUtil.isNotEmpty(request.getCommandContent()), ExecutionEvent::getCommandContent, request.getCommandContent())
                    .eq(ObjUtil.isNotEmpty(request.getResponseContent()), ExecutionEvent::getResponseContent, request.getResponseContent())
                    .eq(ObjUtil.isNotEmpty(request.getFailureReason()), ExecutionEvent::getFailureReason, request.getFailureReason())
                    .eq(ObjUtil.isNotEmpty(request.getSequenceNo()), ExecutionEvent::getSequenceNo, request.getSequenceNo())
                    .eq(ObjUtil.isNotEmpty(request.getStartedAt()), ExecutionEvent::getStartedAt, request.getStartedAt())
                    .eq(ObjUtil.isNotEmpty(request.getFinishedAt()), ExecutionEvent::getFinishedAt, request.getFinishedAt())
                    .eq(ObjUtil.isNotEmpty(request.getAtomicCommandId()), ExecutionEvent::getAtomicCommandId, request.getAtomicCommandId())
                    .eq(ObjUtil.isNotEmpty(request.getAtomicCommandCode()), ExecutionEvent::getAtomicCommandCode, request.getAtomicCommandCode())
                    .eq(ObjUtil.isNotEmpty(request.getProviderId()), ExecutionEvent::getProviderId, request.getProviderId())
                    .eq(ObjUtil.isNotEmpty(request.getProviderName()), ExecutionEvent::getProviderName, request.getProviderName())
                    .eq(ObjUtil.isNotEmpty(request.getModelId()), ExecutionEvent::getModelId, request.getModelId())
                    .eq(ObjUtil.isNotEmpty(request.getModelCode()), ExecutionEvent::getModelCode, request.getModelCode())
                    .eq(ObjUtil.isNotEmpty(request.getStatus()), ExecutionEvent::getStatus, request.getStatus());
        repository.delete(queryWrapper);
    }
}

