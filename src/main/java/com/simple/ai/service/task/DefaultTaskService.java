package com.simple.ai.service.task;

import java.util.Date;

import com.simple.common.core.utils.BeanUtils;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.eventbus.common.service.EventBusService;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.simple.ai.common.service.task.TaskService;
import com.simple.ai.common.entity.task.Task;
import com.simple.ai.common.entity.agentMemory.AgentMemory;
import com.simple.ai.common.view.task.TaskView;
import com.simple.ai.view.task.TaskRepository;
import com.simple.ai.common.view.agentMemory.AgentMemoryView;
import com.simple.ai.common.dto.task.PageTaskResponse;
import com.simple.ai.common.dto.task.InfoTaskResponse;
import com.simple.ai.common.dto.task.CreateTaskRequest;
import com.simple.ai.common.dto.task.UpdateTaskRequest;
import com.simple.ai.common.dto.task.PageTaskRequest;
import com.simple.ai.common.dto.task.PageAggregateTaskRequest;
import com.simple.ai.common.dto.task.PageAggregateTaskResponse;
import com.simple.ai.common.copy.task.TaskCopyMapper;
import com.simple.common.mp.common.enums.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务(task)默认接口实现
 *
 * @author qty
 */
@Service
@Transactional
class DefaultTaskService implements TaskService {

    @Autowired
    private TaskView taskView;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AgentMemoryView agentMemoryView;

    @Autowired
    private TaskCopyMapper copy;

    @Autowired
    private EventBusService eventBusService;

    @Override
    public IPage<PageTaskResponse> findAll(PageTaskRequest pageRequest) {
        var pageInfo = taskView.findAll(pageRequest);
        return pageInfo.convert(entity -> copy.toPageResponse(entity));
    }

    @Override
    public IPage<PageAggregateTaskResponse> findAggregateAll(PageAggregateTaskRequest pageRequest) {

        // 查询任务聚合分页数据
        return taskView.findAggregateAll(pageRequest);
    }

    @Override
    public InfoTaskResponse findById(String id) {
        var task = taskView.findById(id);
        AssertUtils.notEmpty(task, "主键为[{}]的数据为空", id);
        return copy.toInfoResponse(task);
    }

    @Override
    public String save(CreateTaskRequest createRequest) {

        // 校验记忆存在
        AgentMemory memory = agentMemoryView.findById(createRequest.getAgentMemoryId());
        AssertUtils.notEmpty(memory, "记忆[{}]不存在", createRequest.getAgentMemoryId());

        // 构建任务并根据所属记忆回填智能体归属
        var entity = copy.toEntity(createRequest);
        entity.setAgentId(memory.getAgentId());
        entity.setStatus(Status.ON);
        entity.setExecStatus("WAITING");
        taskView.save(entity);
        return entity.getId();
    }

    @Override
    public String updateById(UpdateTaskRequest updateRequest) {
        var task = taskView.findById(updateRequest.getId());
        AssertUtils.notEmpty(task, "主键[{}]的数据不存在", updateRequest.getId());

        // 校验变更后的记忆存在，并使用其智能体归属覆盖任务关联
        AgentMemory memory = agentMemoryView.findById(updateRequest.getAgentMemoryId());
        AssertUtils.notEmpty(memory, "记忆[{}]不存在", updateRequest.getAgentMemoryId());

        // 构建任务并同步记忆对应的智能体归属
        var entity = copy.toEntity(updateRequest);
        entity.setAgentId(memory.getAgentId());
        taskView.updateById(entity);
        return entity.getId();
    }

    @Override
    public void deleteByIds(List<String> ids) {

        // 先清除关联的任务详情，避免孤儿数据
        taskRepository.deleteTaskDetailsByTaskIds(ids);

        // 再删除任务主表
        taskView.deleteByIds(ids);
    }
}

