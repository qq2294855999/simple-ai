package com.simple.ai.common.service.task;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.task.PageTaskResponse;
import com.simple.ai.common.dto.task.InfoTaskResponse;
import com.simple.ai.common.dto.task.CreateTaskRequest;
import com.simple.ai.common.dto.task.UpdateTaskRequest;
import com.simple.ai.common.dto.task.PageTaskRequest;
import com.simple.ai.common.dto.task.PageAggregateTaskRequest;
import com.simple.ai.common.dto.task.PageAggregateTaskResponse;

/**
 * 任务(task)接口
 *
 * @author qty
 */
public interface TaskService {

    /**
     * 分页列表
     *
     * @param pageRequest 请求参数
     * @return 分页数据
     */
    IPage<PageTaskResponse> findAll(PageTaskRequest pageRequest);

    /**
     * 聚合分页列表。
     *
     * @param pageRequest 请求参数
     * @return 聚合分页数据
     */
    IPage<PageAggregateTaskResponse> findAggregateAll(PageAggregateTaskRequest pageRequest);

    /**
     * 获取单条数据
     *
     * @param id 主键
     * @return TaskFullInfoResponse  任务 详细数据
     */
    InfoTaskResponse findById(String id);

    /**
     * 新增
     *
     * @param createRequest 任务 请求对象
     */
    String save(CreateTaskRequest createRequest);

    /**
     * 根据主键修改
     *
     * @param updateRequest 任务 请求对象
     */
    String updateById(UpdateTaskRequest updateRequest);

    /**
     * 删除
     *
     * @param ids 主键
     */
    void deleteByIds(List<String> ids);
}

