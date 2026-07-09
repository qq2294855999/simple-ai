package com.simple.ai.service.taskDetail;

import java.util.Date;

import com.simple.common.core.utils.BeanUtils;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.eventbus.common.service.EventBusService;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.simple.ai.common.service.taskDetail.TaskDetailService;
import com.simple.ai.common.entity.taskDetail.TaskDetail;
import com.simple.ai.common.view.taskDetail.TaskDetailView;
import com.simple.ai.common.dto.taskDetail.PageTaskDetailResponse;
import com.simple.ai.common.dto.taskDetail.InfoTaskDetailResponse;
import com.simple.ai.common.dto.taskDetail.CreateTaskDetailRequest;
import com.simple.ai.common.dto.taskDetail.UpdateTaskDetailRequest;
import com.simple.ai.common.dto.taskDetail.PageTaskDetailRequest;
import com.simple.ai.common.copy.taskDetail.TaskDetailCopyMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * 任务详情(task_detail)默认接口实现
 *
 * @author qty
 */
@Service
@Transactional
class DefaultTaskDetailService implements TaskDetailService {

    @Autowired
    private TaskDetailView taskDetailView;

    @Autowired
    private TaskDetailCopyMapper copy;

    @Autowired
    private EventBusService eventBusService;

    @Override
    public IPage<PageTaskDetailResponse> findAll(PageTaskDetailRequest pageRequest) {
        var pageInfo = taskDetailView.findAll(pageRequest);
        return pageInfo.convert(entity -> copy.toPageResponse(entity));
    }

    @Override
    public InfoTaskDetailResponse findById(String id) {
        var taskDetail = taskDetailView.findById(id);
        AssertUtils.notEmpty(taskDetail, "主键为[{}]的数据为空", id);
        return copy.toInfoResponse(taskDetail);
    }

    @Override
    public String save(CreateTaskDetailRequest createRequest) {
        var entity = copy.toEntity(createRequest);
        taskDetailView.save(entity);
        return entity.getId();
    }

    @Override
    public String updateById(UpdateTaskDetailRequest updateRequest) {
        var taskDetail = taskDetailView.findById(updateRequest.getId());
        AssertUtils.notEmpty(taskDetail, "主键[{}]的数据不存在", updateRequest.getId());

        var entity = copy.toEntity(updateRequest);
        taskDetailView.updateById(entity);
        return entity.getId();
    }

    @Override
    public void deleteByIds(List<String> ids) {
        taskDetailView.deleteByIds(ids);
    }
}

