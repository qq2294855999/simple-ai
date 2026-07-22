package com.simple.ai.common.copy.executionEvent;

import com.simple.ai.common.dto.executionEvent.CreateExecutionEventRequest;
import com.simple.ai.common.dto.executionEvent.InfoExecutionEventResponse;
import com.simple.ai.common.dto.executionEvent.PageExecutionEventResponse;
import com.simple.ai.common.dto.executionEvent.UpdateExecutionEventRequest;
import com.simple.ai.common.entity.executionEvent.ExecutionEvent;
import org.mapstruct.Mapper;

/**
 * 执行事件(execution_event)对象属性复制
 *
 * @author qty
 */
@Mapper(componentModel = "spring")
public interface ExecutionEventCopyMapper {

    /**
     * 将数据对象赋值到page返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    PageExecutionEventResponse toPageResponse(ExecutionEvent entity);

    /**
     * 将数据对象赋值到info返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    InfoExecutionEventResponse toInfoResponse(ExecutionEvent entity);

    /**
     * 将创建数据的数据接收对象复制到数据对象
     *
     * @param createRequest 创建接收数据对象
     * @return ExecutionEvent 数据对象
     */
    ExecutionEvent toEntity(CreateExecutionEventRequest createRequest);

    /**
     * 将修改数据的数据接收对象复制到数据对象
     *
     * @param updateRequest 创建接收数据对象
     * @return ExecutionEvent 数据对象
     */
    ExecutionEvent toEntity(UpdateExecutionEventRequest updateRequest);

}

