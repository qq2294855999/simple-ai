package com.simple.ai.service.agentRule;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.copy.agentRule.AgentRuleCopyMapper;
import com.simple.ai.common.dto.agentDefinition.FindOneAgentDefinitionRequest;
import com.simple.ai.common.dto.agentRule.CreateAgentRuleRequest;
import com.simple.ai.common.dto.agentRule.FindOneAgentRuleRequest;
import com.simple.ai.common.dto.agentRule.InfoAgentRuleResponse;
import com.simple.ai.common.dto.agentRule.PageAgentRuleRequest;
import com.simple.ai.common.dto.agentRule.PageAgentRuleResponse;
import com.simple.ai.common.dto.agentRule.PageAggregateAgentRuleRequest;
import com.simple.ai.common.dto.agentRule.PageAggregateAgentRuleResponse;
import com.simple.ai.common.dto.agentRule.UpdateAgentRuleRequest;
import com.simple.ai.common.entity.agentDefinition.AgentDefinition;
import com.simple.ai.common.entity.agentRule.AgentRule;
import com.simple.ai.common.service.agentRule.AgentRuleService;
import com.simple.ai.common.view.agentDefinition.AgentDefinitionView;
import com.simple.ai.common.view.agentRule.AgentRuleView;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.mp.common.enums.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 智能体规则(agent_rule)默认接口实现
 *
 * @author qty
 */
@Service
@Transactional
class DefaultAgentRuleService implements AgentRuleService {

    @Autowired
    private AgentRuleView agentRuleView;

    @Autowired
    private AgentDefinitionView agentDefinitionView;

    @Autowired
    private AgentRuleCopyMapper copy;

    @Override
    public IPage<PageAgentRuleResponse> findAll(PageAgentRuleRequest pageRequest) {
        var pageInfo = agentRuleView.findAll(pageRequest);
        return pageInfo.convert(entity -> copy.toPageResponse(entity));
    }

    @Override
    public IPage<PageAggregateAgentRuleResponse> findAggregateAll(PageAggregateAgentRuleRequest pageRequest) {

        // 查询前端展示需要的聚合分页数据
        return agentRuleView.findAggregateAll(pageRequest);
    }

    @Override
    public InfoAgentRuleResponse findById(String id) {
        var agentRule = agentRuleView.findById(id);
        AssertUtils.notEmpty(agentRule, "主键为[{}]的数据为空", id);
        return copy.toInfoResponse(agentRule);
    }

    @Override
    public String save(CreateAgentRuleRequest createRequest) {

        // 校验智能体存在性
        validateAgentExists(createRequest.getAgentId());

        // 校验同智能体下规则定义唯一性
        validateDefinitionUnique(createRequest);

        // 保存规则配置
        AgentRule entity = copy.toEntity(createRequest);
        agentRuleView.save(entity);
        return entity.getId();
    }

    @Override
    public String updateById(UpdateAgentRuleRequest updateRequest) {
        var agentRule = agentRuleView.findById(updateRequest.getId());
        AssertUtils.notEmpty(agentRule, "主键[{}]的数据不存在", updateRequest.getId());

        var entity = copy.toEntity(updateRequest);
        agentRuleView.updateById(entity);
        return entity.getId();
    }

    @Override
    public void deleteByIds(List<String> ids) {
        agentRuleView.deleteByIds(ids);
    }

    @Override
    public void enableStatus(String id) {
        AssertUtils.notEmpty(id, "主键不能为空");

        // 查询当前规则
        AgentRule entity = agentRuleView.findById(id);
        AssertUtils.notEmpty(entity, "规则[{}]不存在", id);

        // 设置为启用状态
        entity.setStatus(Status.ON);

        // 持久化更新
        agentRuleView.updateById(entity);
    }

    @Override
    public void disableStatus(String id) {
        AssertUtils.notEmpty(id, "主键不能为空");

        // 查询当前规则
        AgentRule entity = agentRuleView.findById(id);
        AssertUtils.notEmpty(entity, "规则[{}]不存在", id);

        // 设置为禁用状态
        entity.setStatus(Status.OFF);

        // 持久化更新
        agentRuleView.updateById(entity);
    }

    /**
     * 校验智能体存在性。
     *
     * @param agentId 智能体主键
     */
    private void validateAgentExists(String agentId) {
        FindOneAgentDefinitionRequest request = new FindOneAgentDefinitionRequest();
        request.setId(agentId);
        AgentDefinition agentDefinition = agentDefinitionView.findOne(request);
        AssertUtils.notEmpty(agentDefinition, "智能体[{}]不存在", agentId);
    }

    /**
     * 校验同智能体下规则定义唯一性。
     *
     * @param createRequest 创建请求
     */
    private void validateDefinitionUnique(CreateAgentRuleRequest createRequest) {
        FindOneAgentRuleRequest request = new FindOneAgentRuleRequest();
        request.setAgentId(createRequest.getAgentId());
        request.setDefinitionDesc(createRequest.getDefinitionDesc());
        AgentRule existsRule = agentRuleView.findOne(request);
        AssertUtils.isTrue(existsRule == null, "同智能体下规则定义[{}]已存在", createRequest.getDefinitionDesc());
    }
}

