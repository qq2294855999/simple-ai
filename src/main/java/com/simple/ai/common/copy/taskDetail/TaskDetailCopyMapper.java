package com.simple.ai.common.copy.taskDetail;

import java.util.Date;

import com.simple.ai.common.entity.taskDetail.TaskDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.simple.ai.common.dto.taskDetail.PageTaskDetailResponse;
import com.simple.ai.common.dto.taskDetail.InfoTaskDetailResponse;
import com.simple.ai.common.dto.taskDetail.CreateTaskDetailRequest;
import com.simple.ai.common.dto.taskDetail.UpdateTaskDetailRequest;

import java.util.List;

/**
 * 任务详情(task_detail)对象属性复制
 *
 * @author qty
 */
@Mapper(componentModel = "spring")
public interface TaskDetailCopyMapper {

    /**
     * 将数据对象赋值到page返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    PageTaskDetailResponse toPageResponse(TaskDetail entity);

    /**
     * 将数据对象赋值到info返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    InfoTaskDetailResponse toInfoResponse(TaskDetail entity);

    /**
     * 将创建数据的数据接收对象复制到数据对象
     *
     * @param createRequest 创建接收数据对象
     * @return TaskDetail 数据对象
     */
    TaskDetail toEntity(CreateTaskDetailRequest createRequest);

    /**
     * 将修改数据的数据接收对象复制到数据对象
     *
     * @param updateRequest 创建接收数据对象
     * @return TaskDetail 数据对象
     */
    TaskDetail toEntity(UpdateTaskDetailRequest updateRequest);

}

