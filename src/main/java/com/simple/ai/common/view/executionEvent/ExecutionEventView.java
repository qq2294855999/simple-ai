package com.simple.ai.common.view.executionEvent;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.executionEvent.DeleteExecutionEventRequest;
import com.simple.ai.common.dto.executionEvent.FindAllExecutionEventRequest;
import com.simple.ai.common.dto.executionEvent.FindOneExecutionEventRequest;
import com.simple.ai.common.dto.executionEvent.PageExecutionEventRequest;
import com.simple.ai.common.entity.executionEvent.ExecutionEvent;

import java.util.List;

/**
 * 执行事件(execution_event)数据库视图接口
 *
 * @author qty
 */
public interface ExecutionEventView {

    /**
     * 分页列表
     *
     * @param pageRequest 分页参数
     * @return 分页数据
     */
    IPage<ExecutionEvent> findAll(PageExecutionEventRequest pageRequest);

    /**
     * 获取所有数据
     *
     * @param findAllRequest 查询条件
     * @param neRequest      排除条件
     * @return ExecutionEvent 原始表数据
     */
    List<ExecutionEvent> findAll(FindAllExecutionEventRequest findAllRequest, FindAllExecutionEventRequest neRequest);

    /**
     * 获取单条数据
     *
     * @param id 主键
     * @return ExecutionEvent 原始表数据
     */
    ExecutionEvent findById(String id);

    /**
     * 获取单条数据
     *
     * @param findOneRequest 查询条件
     * @param neRequest      排除条件
     * @return ExecutionEvent 原始表数据
     */
    ExecutionEvent findOne(FindOneExecutionEventRequest findOneRequest, FindOneExecutionEventRequest neRequest);

    /**
     * count
     *
     * @param findOneRequest 查询条件
     * @return Long 数据count和
     */
    Long findCount(FindOneExecutionEventRequest findOneRequest, FindOneExecutionEventRequest neRequest);

    /**
     * 新增
     *
     * @param executionEvent 执行事件对象
     */
    void save(ExecutionEvent executionEvent);

    /**
     * 根据id修改
     *
     * @param executionEvent 执行事件对象
     */
    void updateById(ExecutionEvent executionEvent);

    /**
     * 根据id批量修改
     *
     * @param list 对象
     */
    void updateById(List<ExecutionEvent> list);

    /**
     * 批量新增
     *
     * @param list 对象
     */
    void saves(List<ExecutionEvent> list);

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
    void delete(DeleteExecutionEventRequest request);

    /**
     * 按任务主键批量查询执行事件。
     *
     * @param taskIds 任务主键列表
     * @return 执行事件列表
     */
    List<ExecutionEvent> findAllByTaskIds(List<String> taskIds);

    /**
     * 按轮次主键回填任务主键。
     * <p>调度完成后将当前轮次所有执行事件的 taskId 从空串更新为真实任务主键，
     * 确保历史回显时能通过消息的 taskId 查询到执行事件。</p>
     *
     * @param turnId 轮次主键
     * @param taskId 任务主键
     */
    void updateTaskIdByTurnId(String turnId, String taskId);

    /**
     * 获取单条数据
     *
     * @param findOneRequest 查询条件
     * @return ExecutionEvent 原始表数据
     */
    default ExecutionEvent findOne(FindOneExecutionEventRequest findOneRequest) {
        return findOne(findOneRequest, new FindOneExecutionEventRequest());
    }

    /**
     * 获取所有数据
     *
     * @param findAllRequest 查询条件
     * @return ExecutionEvent 原始表数据
     */
    default List<ExecutionEvent> findAll(FindAllExecutionEventRequest findAllRequest) {
        return findAll(findAllRequest, new FindAllExecutionEventRequest());
    }

    /**
     * count
     *
     * @param findOneRequest 查询条件
     * @return Long 数据count和
     */
    default Long findCount(FindOneExecutionEventRequest findOneRequest) {
        return findCount(findOneRequest, new FindOneExecutionEventRequest());
    }

}

