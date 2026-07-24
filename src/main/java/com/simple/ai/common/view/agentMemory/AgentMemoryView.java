package com.simple.ai.common.view.agentMemory;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.agentMemory.DeleteAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemory.FindAllAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemory.FindOneAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemory.PageAgentMemoryRequest;
import com.simple.ai.common.entity.agentMemory.AgentMemory;

import java.util.List;

/**
 * 智能体记忆(agent_memory)数据库视图接口
 *
 * @author qty
 */
public interface AgentMemoryView {

    /**
     * 分页列表
     *
     * @param pageRequest 分页参数
     * @return 分页数据
     */
    IPage<AgentMemory> findAll(PageAgentMemoryRequest pageRequest);

    /**
     * 获取所有数据
     *
     * @param findAllRequest 查询条件
     * @param neRequest 排除条件
     * @return AgentMemory 原始表数据
     */
    List<AgentMemory> findAll(FindAllAgentMemoryRequest findAllRequest, FindAllAgentMemoryRequest neRequest);

    /**
     * 获取单条数据
     *
     * @param id 主键
     * @return AgentMemory 原始表数据
     */
    AgentMemory findById(String id);

    /**
     * 获取单条数据
     *
     * @param findOneRequest 查询条件
     * @param neRequest 排除条件
     * @return AgentMemory 原始表数据
     */
    AgentMemory findOne(FindOneAgentMemoryRequest findOneRequest, FindOneAgentMemoryRequest neRequest);

    /**
     * 新增
     *
     * @param agentMemory 智能体记忆对象
     */
    void save(AgentMemory agentMemory);

    /**
     * 根据id修改
     *
     * @param agentMemory 智能体记忆对象
     */
    void updateById(AgentMemory agentMemory);

    /**
     * 批量新增
     *
     * @param list 对象
     */
    void saves(List<AgentMemory> list);

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
    void delete(DeleteAgentMemoryRequest request);

    /**
     * 获取所有数据
     *
     * @param findAllRequest 查询条件
     * @return AgentMemory 原始表数据
     */
    default List<AgentMemory> findAll(FindAllAgentMemoryRequest findAllRequest) {
        return findAll(findAllRequest, new FindAllAgentMemoryRequest());
    }

    /**
     * 获取单条数据
     *
     * @param findOneRequest 查询条件
     * @return AgentMemory 原始表数据
     */
    default AgentMemory findOne(FindOneAgentMemoryRequest findOneRequest) {
        return findOne(findOneRequest, new FindOneAgentMemoryRequest());
    }
}