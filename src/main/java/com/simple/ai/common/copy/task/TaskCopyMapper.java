package com.simple.ai.common.copy.task;

import java.util.Date;

import com.simple.ai.common.entity.task.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.simple.ai.common.dto.task.PageTaskResponse;
import com.simple.ai.common.dto.task.InfoTaskResponse;
import com.simple.ai.common.dto.task.CreateTaskRequest;
import com.simple.ai.common.dto.task.UpdateTaskRequest;

import java.util.List;

/**
 * 任务(task)对象属性复制
 *
 * @author qty
 */
@Mapper(componentModel = "spring")
public interface TaskCopyMapper {

    /**
     * 将数据对象赋值到page返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    PageTaskResponse toPageResponse(Task entity);

    /**
     * 将数据对象赋值到info返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    InfoTaskResponse toInfoResponse(Task entity);

    /**
     * 将创建数据的数据接收对象复制到数据对象
     *
     * @param createRequest 创建接收数据对象
     * @return Task 数据对象
     */
    Task toEntity(CreateTaskRequest createRequest);

    /**
     * 将修改数据的数据接收对象复制到数据对象
     *
     * @param updateRequest 创建接收数据对象
     * @return Task 数据对象
     */
    Task toEntity(UpdateTaskRequest updateRequest);

}

