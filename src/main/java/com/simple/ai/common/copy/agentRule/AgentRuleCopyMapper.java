package com.simple.ai.common.copy.agentRule;

import java.util.Date;

import com.simple.ai.common.entity.agentRule.AgentRule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.simple.ai.common.dto.agentRule.PageAgentRuleResponse;
import com.simple.ai.common.dto.agentRule.InfoAgentRuleResponse;
import com.simple.ai.common.dto.agentRule.CreateAgentRuleRequest;
import com.simple.ai.common.dto.agentRule.UpdateAgentRuleRequest;

import java.util.List;

/**
 * 智能体规则(agent_rule)对象属性复制
 *
 * @author qty
 */
@Mapper(componentModel = "spring")
public interface AgentRuleCopyMapper {

    /**
     * 将数据对象赋值到page返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    PageAgentRuleResponse toPageResponse(AgentRule entity);

    /**
     * 将数据对象赋值到info返回对象
     *
     * @param entity 数据对象
     * @return page 数据
     */
    InfoAgentRuleResponse toInfoResponse(AgentRule entity);

    /**
     * 将创建数据的数据接收对象复制到数据对象
     *
     * @param createRequest 创建接收数据对象
     * @return AgentRule 数据对象
     */
    AgentRule toEntity(CreateAgentRuleRequest createRequest);

    /**
     * 将修改数据的数据接收对象复制到数据对象
     *
     * @param updateRequest 创建接收数据对象
     * @return AgentRule 数据对象
     */
    AgentRule toEntity(UpdateAgentRuleRequest updateRequest);

}

