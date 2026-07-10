package com.simple.ai.common.view.agentRule;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.dto.agentRule.PageAggregateAgentRuleRequest;
import com.simple.ai.common.dto.agentRule.PageAggregateAgentRuleResponse;
import com.simple.ai.common.entity.agentRule.AgentRule;
import com.simple.ai.common.dto.agentRule.PageAgentRuleRequest;
import com.simple.ai.common.dto.agentRule.FindOneAgentRuleRequest;
import com.simple.ai.common.dto.agentRule.FindAllAgentRuleRequest;
import com.simple.ai.common.dto.agentRule.DeleteAgentRuleRequest;

/**
 * 智能体规则(agent_rule)数据库视图接口
 *
 * @author qty
 */
public interface AgentRuleView {

    /**
     * 分页列表
     *
     * @param pageRequest 分页参数
     * @return 分页数据
     */
    IPage<AgentRule> findAll(PageAgentRuleRequest pageRequest);

    /**
     * 聚合分页列表。
     *
     * @param pageRequest 分页参数
     * @return 聚合分页数据
     */
    IPage<PageAggregateAgentRuleResponse> findAggregateAll(PageAggregateAgentRuleRequest pageRequest);

    /**
     * 获取所有数据
     *
     * @param findAllRequest 查询条件
     * @param neRequest      排除条件
     * @return AgentRule 原始表数据
     */
    List<AgentRule> findAll(FindAllAgentRuleRequest findAllRequest, FindAllAgentRuleRequest neRequest);

    /**
     * 获取单条数据
     *
     * @param id 主键
     * @return AgentRule 原始表数据
     */
    AgentRule findById(String id);

    /**
     * 获取单条数据
     *
     * @param findOneRequest 查询条件
     * @param neRequest      排除条件
     * @return AgentRule 原始表数据
     */
    AgentRule findOne(FindOneAgentRuleRequest findOneRequest, FindOneAgentRuleRequest neRequest);

    /**
     * count
     *
     * @param findOneRequest 查询条件
     * @return Long 数据count和
     */
    Long findCount(FindOneAgentRuleRequest findOneRequest, FindOneAgentRuleRequest neRequest);

    /**
     * 新增
     *
     * @param agentRule 智能体规则对象
     */
    void save(AgentRule agentRule);

    /**
     * 根据id修改
     *
     * @param agentRule 智能体规则对象
     */
    void updateById(AgentRule agentRule);

    /**
     * 根据id批量修改
     *
     * @param list 对象
     */
    void updateById(List<AgentRule> list);

    /**
     * 批量新增
     *
     * @param list 对象
     */
    void saves(List<AgentRule> list);

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
    void delete(DeleteAgentRuleRequest request);

    /**
     * 获取单条数据
     *
     * @param findOneRequest 查询条件
     * @return AgentRule 原始表数据
     */
    default AgentRule findOne(FindOneAgentRuleRequest findOneRequest) {
        return findOne(findOneRequest, new FindOneAgentRuleRequest());
    }

    /**
     * 获取所有数据
     *
     * @param findAllRequest 查询条件
     * @return AgentRule 原始表数据
     */
    default List<AgentRule> findAll(FindAllAgentRuleRequest findAllRequest) {
        return findAll(findAllRequest, new FindAllAgentRuleRequest());
    }

    /**
     * count
     *
     * @param findOneRequest 查询条件
     * @return Long 数据count和
     */
    default Long findCount(FindOneAgentRuleRequest findOneRequest) {
        return findCount(findOneRequest, new FindOneAgentRuleRequest());
    }

}

