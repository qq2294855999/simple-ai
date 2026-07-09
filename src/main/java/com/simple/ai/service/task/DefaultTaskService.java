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
import com.simple.ai.common.view.task.TaskView;
import com.simple.ai.common.dto.task.PageTaskResponse;
import com.simple.ai.common.dto.task.InfoTaskResponse;
import com.simple.ai.common.dto.task.CreateTaskRequest;
import com.simple.ai.common.dto.task.UpdateTaskRequest;
import com.simple.ai.common.dto.task.PageTaskRequest;
import com.simple.ai.common.copy.task.TaskCopyMapper;

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
    private TaskCopyMapper copy;

    @Autowired
    private EventBusService eventBusService;

    @Override
    public IPage<PageTaskResponse> findAll(PageTaskRequest pageRequest) {
        var pageInfo = taskView.findAll(pageRequest);
        return pageInfo.convert(entity -> copy.toPageResponse(entity));
    }

    @Override
    public InfoTaskResponse findById(String id) {
        var task = taskView.findById(id);
        AssertUtils.notEmpty(task, "主键为[{}]的数据为空", id);
        return copy.toInfoResponse(task);
    }

    @Override
    public String save(CreateTaskRequest createRequest) {
        var entity = copy.toEntity(createRequest);
        taskView.save(entity);
        return entity.getId();
    }

    @Override
    public String updateById(UpdateTaskRequest updateRequest) {
        var task = taskView.findById(updateRequest.getId());
        AssertUtils.notEmpty(task, "主键[{}]的数据不存在", updateRequest.getId());

        var entity = copy.toEntity(updateRequest);
        taskView.updateById(entity);
        return entity.getId();
    }

    @Override
    public void deleteByIds(List<String> ids) {
        taskView.deleteByIds(ids);
    }
}

