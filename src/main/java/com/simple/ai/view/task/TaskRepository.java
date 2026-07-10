package com.simple.ai.view.task;

import java.util.Date;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.simple.ai.common.entity.task.Task;
import com.simple.ai.common.dto.task.PageAggregateTaskRequest;
import com.simple.ai.common.dto.task.PageAggregateTaskResponse;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 任务(task)数据库访问层
 *
 * @author qty
 */
@Mapper
public interface TaskRepository extends BaseMapper<Task> {

    /**
     * 批量新增数据（MyBatis原生foreach方法，MP表的自动化操作都无效，需要手动为集合对象赋值）
     *
     * @param entities List<Task> 实例对象列表
     * @return 影响行数
     */
    int insertBatch(@Param("entities") List<Task> entities);

    /**
     * 查询聚合分页列表。
     *
     * @param pageRequest 分页请求
     * @param offset 偏移量
     * @param size 每页数量
     * @return 聚合分页列表
     */
    List<PageAggregateTaskResponse> selectAggregatePage(@Param("pageRequest") PageAggregateTaskRequest pageRequest,
                                                         @Param("offset") Long offset,
                                                         @Param("size") Long size);

    /**
     * 查询聚合分页总数。
     *
     * @param pageRequest 分页请求
     * @return 总数
     */
    Long selectAggregateCount(@Param("pageRequest") PageAggregateTaskRequest pageRequest);

    /**
     * 按任务主键批量删除任务详情。
     *
     * @param taskIds 任务主键列表
     * @return 影响行数
     */
    int deleteTaskDetailsByTaskIds(@Param("taskIds") List<String> taskIds);

}

