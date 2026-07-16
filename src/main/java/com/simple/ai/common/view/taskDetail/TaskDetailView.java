package com.simple.ai.common.view.taskDetail;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.entity.taskDetail.TaskDetail;
import com.simple.ai.common.dto.taskDetail.PageTaskDetailRequest;
import com.simple.ai.common.dto.taskDetail.FindOneTaskDetailRequest;
import com.simple.ai.common.dto.taskDetail.FindAllTaskDetailRequest;
import com.simple.ai.common.dto.taskDetail.DeleteTaskDetailRequest;

/**
 * 任务详情(task_detail)数据库视图接口
 *
 * @author qty
 */
public interface TaskDetailView {

    /**
     * 分页列表
     *
     * @param pageRequest 分页参数
     * @return 分页数据
     */
    IPage<TaskDetail> findAll(PageTaskDetailRequest pageRequest);

    /**
     * 获取所有数据
     *
     * @param findAllRequest 查询条件
     * @param neRequest      排除条件
     * @return TaskDetail 原始表数据
     */
    List<TaskDetail> findAll(FindAllTaskDetailRequest findAllRequest, FindAllTaskDetailRequest neRequest);

    /**
     * 获取单条数据
     *
     * @param id 主键
     * @return TaskDetail 原始表数据
     */
    TaskDetail findById(String id);

    /**
     * 获取单条数据
     *
     * @param findOneRequest 查询条件
     * @param neRequest      排除条件
     * @return TaskDetail 原始表数据
     */
    TaskDetail findOne(FindOneTaskDetailRequest findOneRequest, FindOneTaskDetailRequest neRequest);

    /**
     * count
     *
     * @param findOneRequest 查询条件
     * @return Long 数据count和
     */
    Long findCount(FindOneTaskDetailRequest findOneRequest, FindOneTaskDetailRequest neRequest);

    /**
     * 新增
     *
     * @param taskDetail 任务详情对象
     */
    void save(TaskDetail taskDetail);

    /**
     * 根据id修改
     *
     * @param taskDetail 任务详情对象
     */
    void updateById(TaskDetail taskDetail);

    /**
     * 根据id批量修改
     *
     * @param list 对象
     */
    void updateById(List<TaskDetail> list);

    /**
     * 批量新增
     *
     * @param list 对象
     */
    void saves(List<TaskDetail> list);

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
    void delete(DeleteTaskDetailRequest request);

    /**
     * 根据任务ID列表批量查询任务详情。
     *
     * @param taskIds 任务ID列表
     * @return 任务详情列表
     */
    List<TaskDetail> findAllByTaskIds(List<String> taskIds);

    /**
     * 根据任务ID列表批量删除任务详情。
     *
     * @param taskIds 任务ID列表
     */
    void deleteByTaskIds(List<String> taskIds);

    /**
     * 获取单条数据
     *
     * @param findOneRequest 查询条件
     * @return TaskDetail 原始表数据
     */
    default TaskDetail findOne(FindOneTaskDetailRequest findOneRequest) {
        return findOne(findOneRequest, new FindOneTaskDetailRequest());
    }

    /**
     * 获取所有数据
     *
     * @param findAllRequest 查询条件
     * @return TaskDetail 原始表数据
     */
    default List<TaskDetail> findAll(FindAllTaskDetailRequest findAllRequest) {
        return findAll(findAllRequest, new FindAllTaskDetailRequest());
    }

    /**
     * count
     *
     * @param findOneRequest 查询条件
     * @return Long 数据count和
     */
    default Long findCount(FindOneTaskDetailRequest findOneRequest) {
        return findCount(findOneRequest, new FindOneTaskDetailRequest());
    }

}

