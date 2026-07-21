package com.simple.ai.common.view.agentExecutor;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.dto.agentExecutor.PageAgentExecutorRequest;
import com.simple.ai.common.dto.agentExecutor.PageAgentExecutorResponse;
import com.simple.ai.common.entity.agentExecutor.AgentExecutor;

import java.util.List;

/**
 * 执行器类型(agent_executor)数据库视图接口。
 *
 * @author qty
 */
public interface AgentExecutorView {

    /**
     * 分页查询执行器类型列表。
     *
     * @param pageRequest 分页查询请求
     * @param page        MyBatis-Plus 分页对象
     * @return 分页结果
     */
    List<PageAgentExecutorResponse> findAll(PageAgentExecutorRequest pageRequest, Page<PageAgentExecutorResponse> page);

    /**
     * 按主键查询执行器类型。
     *
     * @param id 主键
     * @return 执行器类型实体，不存在返回 null
     */
    AgentExecutor findById(String id);

    /**
     * 保存执行器类型。
     *
     * @param entity 执行器类型实体
     */
    void save(AgentExecutor entity);

    /**
     * 按主键更新执行器类型。
     *
     * @param entity 执行器类型实体
     */
    void updateById(AgentExecutor entity);

    /**
     * 按主键删除执行器类型。
     *
     * @param id 主键
     */
    void deleteById(String id);
}
