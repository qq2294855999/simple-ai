package com.simple.ai.common.view.task;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.entity.task.Task;
import com.simple.ai.common.dto.task.PageTaskRequest;
import com.simple.ai.common.dto.task.PageAggregateTaskRequest;
import com.simple.ai.common.dto.task.PageAggregateTaskResponse;
import com.simple.ai.common.dto.task.FindOneTaskRequest;
import com.simple.ai.common.dto.task.FindAllTaskRequest;
import com.simple.ai.common.dto.task.DeleteTaskRequest;

/**
 * 任务(task)数据库视图接口
 *
 * @author qty
 */
public interface TaskView {

    /**
     * 分页列表
     *
     * @param pageRequest 分页参数
     * @return 分页数据
     */
    IPage<Task> findAll(PageTaskRequest pageRequest);

    /**
     * 聚合分页列表。
     *
     * @param pageRequest 聚合分页请求
     * @return 聚合分页数据
     */
    IPage<PageAggregateTaskResponse> findAggregateAll(PageAggregateTaskRequest pageRequest);

    /**
     * 获取所有数据
     *
     * @param findAllRequest 查询条件
     * @param neRequest      排除条件
     * @return Task 原始表数据
     */
    List<Task> findAll(FindAllTaskRequest findAllRequest, FindAllTaskRequest neRequest);

    /**
     * 获取单条数据
     *
     * @param id 主键
     * @return Task 原始表数据
     */
    Task findById(String id);

    /**
     * 获取单条数据
     *
     * @param findOneRequest 查询条件
     * @param neRequest      排除条件
     * @return Task 原始表数据
     */
    Task findOne(FindOneTaskRequest findOneRequest, FindOneTaskRequest neRequest);

    /**
     * count
     *
     * @param findOneRequest 查询条件
     * @return Long 数据count和
     */
    Long findCount(FindOneTaskRequest findOneRequest, FindOneTaskRequest neRequest);

    /**
     * 新增
     *
     * @param task 任务对象
     */
    void save(Task task);

    /**
     * 根据id修改
     *
     * @param task 任务对象
     */
    void updateById(Task task);

    /**
     * 根据id批量修改
     *
     * @param list 对象
     */
    void updateById(List<Task> list);

    /**
     * 批量新增
     *
     * @param list 对象
     */
    void saves(List<Task> list);

    /**
     * 删除
     *
     * @param ids 主键
     */
    void deleteByIds(List<String> ids);

    /**
     * 删除
     *
     * @param request 条件
     */
    void delete(DeleteTaskRequest request);

    /**
     * 获取单条数据
     *
     * @param findOneRequest 查询条件
     * @return Task 原始表数据
     */
    default Task findOne(FindOneTaskRequest findOneRequest) {
        return findOne(findOneRequest, new FindOneTaskRequest());
    }

    /**
     * 获取所有数据
     *
     * @param findAllRequest 查询条件
     * @return Task 原始表数据
     */
    default List<Task> findAll(FindAllTaskRequest findAllRequest) {
        return findAll(findAllRequest, new FindAllTaskRequest());
    }

    /**
     * count
     *
     * @param findOneRequest 查询条件
     * @return Long 数据count和
     */
    default Long findCount(FindOneTaskRequest findOneRequest) {
        return findCount(findOneRequest, new FindOneTaskRequest());
    }

}

