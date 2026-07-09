package com.simple.ai.common.view.agentDefinition;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.entity.agentDefinition.AgentDefinition;
import com.simple.ai.common.dto.agentDefinition.PageAgentDefinitionRequest;
import com.simple.ai.common.dto.agentDefinition.FindOneAgentDefinitionRequest;
import com.simple.ai.common.dto.agentDefinition.FindAllAgentDefinitionRequest;
import com.simple.ai.common.dto.agentDefinition.DeleteAgentDefinitionRequest;

/**
 * 智能体定义(agent_definition)数据库视图接口
 *
 * @author qty
 */
public interface AgentDefinitionView {

    /**
     * 分页列表
     *
     * @param pageRequest 分页参数
     * @return 分页数据
     */
    IPage<AgentDefinition> findAll(PageAgentDefinitionRequest pageRequest);

    /**
     * 获取所有数据
     *
     * @param findAllRequest 查询条件
     * @param neRequest      排除条件
     * @return AgentDefinition 原始表数据
     */
    List<AgentDefinition> findAll(FindAllAgentDefinitionRequest findAllRequest, FindAllAgentDefinitionRequest neRequest);

    /**
     * 获取单条数据
     *
     * @param id 主键
     * @return AgentDefinition 原始表数据
     */
    AgentDefinition findById(String id);

    /**
     * 获取单条数据
     *
     * @param findOneRequest 查询条件
     * @param neRequest      排除条件
     * @return AgentDefinition 原始表数据
     */
    AgentDefinition findOne(FindOneAgentDefinitionRequest findOneRequest, FindOneAgentDefinitionRequest neRequest);

    /**
     * count
     *
     * @param findOneRequest 查询条件
     * @return Long 数据count和
     */
    Long findCount(FindOneAgentDefinitionRequest findOneRequest, FindOneAgentDefinitionRequest neRequest);

    /**
     * 新增
     *
     * @param agentDefinition 智能体定义对象
     */
    void save(AgentDefinition agentDefinition);

    /**
     * 根据id修改
     *
     * @param agentDefinition 智能体定义对象
     */
    void updateById(AgentDefinition agentDefinition);

    /**
     * 根据id批量修改
     *
     * @param list 对象
     */
    void updateById(List<AgentDefinition> list);

    /**
     * 批量新增
     *
     * @param list 对象
     */
    void saves(List<AgentDefinition> list);

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
    void delete(DeleteAgentDefinitionRequest request);

    /**
     * 获取单条数据
     *
     * @param findOneRequest 查询条件
     * @return AgentDefinition 原始表数据
     */
    default AgentDefinition findOne(FindOneAgentDefinitionRequest findOneRequest) {
        return findOne(findOneRequest, new FindOneAgentDefinitionRequest());
    }

    /**
     * 获取所有数据
     *
     * @param findAllRequest 查询条件
     * @return AgentDefinition 原始表数据
     */
    default List<AgentDefinition> findAll(FindAllAgentDefinitionRequest findAllRequest) {
        return findAll(findAllRequest, new FindAllAgentDefinitionRequest());
    }

    /**
     * count
     *
     * @param findOneRequest 查询条件
     * @return Long 数据count和
     */
    default Long findCount(FindOneAgentDefinitionRequest findOneRequest) {
        return findCount(findOneRequest, new FindOneAgentDefinitionRequest());
    }

}

