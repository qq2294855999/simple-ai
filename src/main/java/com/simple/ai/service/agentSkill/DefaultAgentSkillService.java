package com.simple.ai.service.agentSkill;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.copy.agentSkill.AgentSkillCopyMapper;
import com.simple.ai.common.dto.agentDefinition.FindOneAgentDefinitionRequest;
import com.simple.ai.common.dto.agentSkill.CreateAgentSkillRequest;
import com.simple.ai.common.dto.agentSkill.FindOneAgentSkillRequest;
import com.simple.ai.common.dto.agentSkill.InfoAgentSkillResponse;
import com.simple.ai.common.dto.agentSkill.PageAgentSkillRequest;
import com.simple.ai.common.dto.agentSkill.PageAgentSkillResponse;
import com.simple.ai.common.dto.agentSkill.PageAggregateAgentSkillRequest;
import com.simple.ai.common.dto.agentSkill.PageAggregateAgentSkillResponse;
import com.simple.ai.common.dto.agentSkill.UpdateAgentSkillRequest;
import com.simple.ai.common.entity.agentDefinition.AgentDefinition;
import com.simple.ai.common.entity.agentSkill.AgentSkill;
import com.simple.ai.common.service.agentSkill.AgentSkillService;
import com.simple.ai.common.view.agentDefinition.AgentDefinitionView;
import com.simple.ai.common.view.agentSkill.AgentSkillView;
import com.simple.ai.view.agentSkill.AgentSkillRepository;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.mp.common.enums.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private AgentDefinitionView agentDefinitionView;

    @Autowired
    private AgentSkillRepository agentSkillRepository;

    @Autowired
    private AgentSkillCopyMapper copy;

    @Override
    public IPage<PageAgentSkillResponse> findAll(PageAgentSkillRequest pageRequest) {
        var pageInfo = agentSkillView.findAll(pageRequest);
        return pageInfo.convert(entity -> copy.toPageResponse(entity));
    }

    @Override
    public IPage<PageAggregateAgentSkillResponse> findAggregateAll(PageAggregateAgentSkillRequest pageRequest) {

        // 查询前端展示需要的聚合分页数据
        return agentSkillView.findAggregateAll(pageRequest);
    }

    @Override
    public InfoAgentSkillResponse findById(String id) {
        var agentSkill = agentSkillView.findById(id);
        AssertUtils.notEmpty(agentSkill, "主键为[{}]的数据为空", id);
        return copy.toInfoResponse(agentSkill);
    }

    @Override
    public String save(CreateAgentSkillRequest createRequest) {

        // 校验智能体存在性
        validateAgentExists(createRequest.getAgentId());

        // 校验同智能体下技能定义唯一性
        validateDefinitionUnique(createRequest);

        // 保存技能配置
        AgentSkill entity = copy.toEntity(createRequest);
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

        // 校验所有技能存在
        validateSkillsExist(ids);

        // 先解除原子命令关联，保留全局命令和命令本体
        agentSkillRepository.unlinkAtomicCommandBySkillIds(ids);

        // 删除技能主表
        agentSkillView.deleteByIds(ids);
    }

    @Override
    public void enableStatus(String id) {
        AssertUtils.notEmpty(id, "主键不能为空");

        // 查询当前技能
        AgentSkill entity = agentSkillView.findById(id);
        AssertUtils.notEmpty(entity, "技能[{}]不存在", id);

        // 设置为启用状态
        entity.setStatus(Status.ON);

        // 持久化更新
        agentSkillView.updateById(entity);
    }

    @Override
    public void disableStatus(String id) {
        AssertUtils.notEmpty(id, "主键不能为空");

        // 查询当前技能
        AgentSkill entity = agentSkillView.findById(id);
        AssertUtils.notEmpty(entity, "技能[{}]不存在", id);

        // 设置为禁用状态
        entity.setStatus(Status.OFF);

        // 持久化更新
        agentSkillView.updateById(entity);
    }

    /**
     * 校验所有技能存在。
     *
     * @param ids 技能主键列表
     */
    private void validateSkillsExist(List<String> ids) {

        // 批量查询技能数据
        List<AgentSkill> skills = agentSkillRepository.selectBatchIds(ids);

        // 校验数量一致，确保所有主键有效
        AssertUtils.isTrue(skills.size() == ids.size(), "存在无效的技能主键，无法删除");
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
     * 校验同智能体下技能定义唯一性。
     *
     * @param createRequest 创建请求
     */
    private void validateDefinitionUnique(CreateAgentSkillRequest createRequest) {
        FindOneAgentSkillRequest request = new FindOneAgentSkillRequest();
        request.setAgentId(createRequest.getAgentId());
        request.setDefinitionDesc(createRequest.getDefinitionDesc());
        AgentSkill existsSkill = agentSkillView.findOne(request);
        AssertUtils.isTrue(existsSkill == null, "同智能体下技能定义[{}]已存在", createRequest.getDefinitionDesc());
    }
}

