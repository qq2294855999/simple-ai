package com.simple.ai.common.service.agentRule;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.agentRule.CreateAgentRuleRequest;
import com.simple.ai.common.dto.agentRule.InfoAgentRuleResponse;
import com.simple.ai.common.dto.agentRule.PageAgentRuleRequest;
import com.simple.ai.common.dto.agentRule.PageAgentRuleResponse;
import com.simple.ai.common.dto.agentRule.PageAggregateAgentRuleRequest;
import com.simple.ai.common.dto.agentRule.PageAggregateAgentRuleResponse;
import com.simple.ai.common.dto.agentRule.UpdateAgentRuleRequest;

import java.util.List;

/**
 * 智能体规则(agent_rule)接口
 *
 * @author qty
 */
public interface AgentRuleService {

    /**
     * 分页列表
     *
     * @param pageRequest 请求参数
     * @return 分页数据
     */
    IPage<PageAgentRuleResponse> findAll(PageAgentRuleRequest pageRequest);

    /**
     * 聚合分页列表。
     *
     * @param pageRequest 请求参数
     * @return 聚合分页数据
     */
    IPage<PageAggregateAgentRuleResponse> findAggregateAll(PageAggregateAgentRuleRequest pageRequest);

    /**
     * 获取单条数据
     *
     * @param id 主键
     * @return AgentRuleFullInfoResponse  智能体规则 详细数据
     */
    InfoAgentRuleResponse findById(String id);

    /**
     * 新增
     *
     * @param createRequest 智能体规则 请求对象
     */
    String save(CreateAgentRuleRequest createRequest);

    /**
     * 根据主键修改
     *
     * @param updateRequest 智能体规则 请求对象
     */
    String updateById(UpdateAgentRuleRequest updateRequest);

    /**
     * 删除
     *
     * @param ids 主键
     */
    void deleteByIds(List<String> ids);
}

