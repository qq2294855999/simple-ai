package com.simple.ai.service.executionEvent;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.copy.executionEvent.ExecutionEventCopyMapper;
import com.simple.ai.common.dto.executionEvent.*;
import com.simple.ai.common.service.executionEvent.ExecutionEventService;
import com.simple.ai.common.view.executionEvent.ExecutionEventView;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.eventbus.common.service.EventBusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 执行事件(execution_event)默认接口实现
 *
 * @author qty
 */
@Service
@Transactional
class DefaultExecutionEventService implements ExecutionEventService {

    @Autowired
    private ExecutionEventView executionEventView;

    @Autowired
    private ExecutionEventCopyMapper copy;

    @Autowired
    private EventBusService eventBusService;

    @Override
    public IPage<PageExecutionEventResponse> findAll(PageExecutionEventRequest pageRequest) {
        var pageInfo = executionEventView.findAll(pageRequest);
        return pageInfo.convert(entity -> copy.toPageResponse(entity));
    }

    @Override
    public InfoExecutionEventResponse findById(String id) {
        var executionEvent = executionEventView.findById(id);
        AssertUtils.notEmpty(executionEvent, "主键为[{}]的数据为空", id);
        return copy.toInfoResponse(executionEvent);
    }

    @Override
    public String save(CreateExecutionEventRequest createRequest) {
        var entity = copy.toEntity(createRequest);
        executionEventView.save(entity);
        return entity.getId();
    }

    @Override
    public String updateById(UpdateExecutionEventRequest updateRequest) {
        var executionEvent = executionEventView.findById(updateRequest.getId());
        AssertUtils.notEmpty(executionEvent, "主键[{}]的数据不存在", updateRequest.getId());

        var entity = copy.toEntity(updateRequest);
        executionEventView.updateById(entity);
        return entity.getId();
    }

    @Override
    public void deleteByIds(List<String> ids) {
        executionEventView.deleteByIds(ids);
    }
}

