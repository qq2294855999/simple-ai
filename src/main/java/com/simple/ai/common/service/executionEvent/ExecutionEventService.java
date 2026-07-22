package com.simple.ai.common.service.executionEvent;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.executionEvent.*;

import java.util.List;

/**
 * 执行事件(execution_event)接口
 *
 * @author qty
 */
public interface ExecutionEventService {

    /**
     * 分页列表
     *
     * @param pageRequest 请求参数
     * @return 分页数据
     */
    IPage<PageExecutionEventResponse> findAll(PageExecutionEventRequest pageRequest);

    /**
     * 获取单条数据
     *
     * @param id 主键
     * @return ExecutionEventFullInfoResponse  执行事件 详细数据
     */
    InfoExecutionEventResponse findById(String id);

    /**
     * 新增
     *
     * @param createRequest 执行事件 请求对象
     */
    String save(CreateExecutionEventRequest createRequest);

    /**
     * 根据主键修改
     *
     * @param updateRequest 执行事件 请求对象
     */
    String updateById(UpdateExecutionEventRequest updateRequest);

    /**
     * 删除
     *
     * @param ids 主键
     */
    void deleteByIds(List<String> ids);
}

