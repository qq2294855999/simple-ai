package com.simple.ai.service.agentRule;

import java.util.Date;

import com.simple.common.core.utils.BeanUtils;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.eventbus.common.service.EventBusService;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.simple.ai.common.service.agentRule.AgentRuleService;
import com.simple.ai.common.entity.agentRule.AgentRule;
import com.simple.ai.common.view.agentRule.AgentRuleView;
import com.simple.ai.common.dto.agentRule.PageAgentRuleResponse;
import com.simple.ai.common.dto.agentRule.InfoAgentRuleResponse;
import com.simple.ai.common.dto.agentRule.CreateAgentRuleRequest;
import com.simple.ai.common.dto.agentRule.UpdateAgentRuleRequest;
import com.simple.ai.common.dto.agentRule.PageAgentRuleRequest;
import com.simple.ai.common.copy.agentRule.AgentRuleCopyMapper;

import java.util.ArrayList;
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
    private AgentRuleCopyMapper copy;

    @Autowired
    private EventBusService eventBusService;

    @Override
    public IPage<PageAgentRuleResponse> findAll(PageAgentRuleRequest pageRequest) {
        var pageInfo = agentRuleView.findAll(pageRequest);
        return pageInfo.convert(entity -> copy.toPageResponse(entity));
    }

    @Override
    public InfoAgentRuleResponse findById(String id) {
        var agentRule = agentRuleView.findById(id);
        AssertUtils.notEmpty(agentRule, "主键为[{}]的数据为空", id);
        return copy.toInfoResponse(agentRule);
    }

    @Override
    public String save(CreateAgentRuleRequest createRequest) {
        var entity = copy.toEntity(createRequest);
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
}

