package com.simple.ai.common.view.agentMemoryStep;

import com.simple.ai.common.entity.agentMemoryStep.AgentMemoryStep;

import java.util.List;

/**
 * 智能体记忆步骤(agent_memory_step)数据库视图接口
 *
 * @author qty
 */
public interface AgentMemoryStepView {

    /**
     * 按记忆ID查询所有步骤（按step_no排序）
     *
     * @param memoryId 记忆ID
     * @return 步骤列表
     */
    List<AgentMemoryStep> findAllByMemoryId(String memoryId);

    /**
     * 获取单条数据
     *
     * @param id 主键
     * @return 步骤数据
     */
    AgentMemoryStep findById(String id);

    /**
     * 新增
     *
     * @param step 步骤对象
     */
    void save(AgentMemoryStep step);

    /**
     * 批量新增
     *
     * @param list 步骤列表
     */
    void saves(List<AgentMemoryStep> list);

    /**
     * 根据id修改
     *
     * @param step 步骤对象
     */
    void updateById(AgentMemoryStep step);

    /**
     * 按记忆ID删除所有步骤
     *
     * @param memoryId 记忆ID
     */
    void deleteByMemoryId(String memoryId);

    /**
     * 删除
     *
     * @param ids 主键列表
     */
    void deleteByIds(List<String> ids);
}