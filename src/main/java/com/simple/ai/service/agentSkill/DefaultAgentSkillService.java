package com.simple.ai.service.agentSkill;

import java.util.Date;

import com.simple.common.core.utils.BeanUtils;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.eventbus.common.service.EventBusService;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.simple.ai.common.service.agentSkill.AgentSkillService;
import com.simple.ai.common.entity.agentSkill.AgentSkill;
import com.simple.ai.common.view.agentSkill.AgentSkillView;
import com.simple.ai.common.dto.agentSkill.PageAgentSkillResponse;
import com.simple.ai.common.dto.agentSkill.InfoAgentSkillResponse;
import com.simple.ai.common.dto.agentSkill.CreateAgentSkillRequest;
import com.simple.ai.common.dto.agentSkill.UpdateAgentSkillRequest;
import com.simple.ai.common.dto.agentSkill.PageAgentSkillRequest;
import com.simple.ai.common.copy.agentSkill.AgentSkillCopyMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * 智能体技能(agent_skill)默认接口实现
 *
 * @author qty
 */
@Service
@Transactional
class DefaultAgentSkillService implements AgentSkillService {

    @Autowired
    private AgentSkillView agentSkillView;

    @Autowired
    private AgentSkillCopyMapper copy;

    @Autowired
    private EventBusService eventBusService;

    @Override
    public IPage<PageAgentSkillResponse> findAll(PageAgentSkillRequest pageRequest) {
        var pageInfo = agentSkillView.findAll(pageRequest);
        return pageInfo.convert(entity -> copy.toPageResponse(entity));
    }

    @Override
    public InfoAgentSkillResponse findById(String id) {
        var agentSkill = agentSkillView.findById(id);
        AssertUtils.notEmpty(agentSkill, "主键为[{}]的数据为空", id);
        return copy.toInfoResponse(agentSkill);
    }

    @Override
    public String save(CreateAgentSkillRequest createRequest) {
        var entity = copy.toEntity(createRequest);
        agentSkillView.save(entity);
        return entity.getId();
    }

    @Override
    public String updateById(UpdateAgentSkillRequest updateRequest) {
        var agentSkill = agentSkillView.findById(updateRequest.getId());
        AssertUtils.notEmpty(agentSkill, "主键[{}]的数据不存在", updateRequest.getId());

        var entity = copy.toEntity(updateRequest);
        agentSkillView.updateById(entity);
        return entity.getId();
    }

    @Override
    public void deleteByIds(List<String> ids) {
        agentSkillView.deleteByIds(ids);
    }
}

