package com.simple.ai.service.agentDefinition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.copy.agentDefinition.AgentDefinitionCopyMapper;
import com.simple.ai.common.dto.agentDefinition.CreateAgentDefinitionRequest;
import com.simple.ai.common.dto.agentDefinition.DeleteCascadeAgentDefinitionResponse;
import com.simple.ai.common.dto.agentDefinition.FindOneAgentDefinitionRequest;
import com.simple.ai.common.dto.agentDefinition.InfoAgentDefinitionResponse;
import com.simple.ai.common.dto.agentDefinition.InfoAggregateAgentDefinitionResponse;
import com.simple.ai.common.dto.agentDefinition.PageAgentDefinitionRequest;
import com.simple.ai.common.dto.agentDefinition.PageAgentDefinitionResponse;
import com.simple.ai.common.dto.agentDefinition.PageAggregateAgentDefinitionRequest;
import com.simple.ai.common.dto.agentDefinition.PageAggregateAgentDefinitionResponse;
import com.simple.ai.common.dto.agentDefinition.UpdateAgentDefinitionRequest;
import com.simple.ai.common.service.agentDefinition.AgentDefinitionService;
import com.simple.ai.common.entity.agentDefinition.AgentDefinition;
import com.simple.ai.common.view.agentDefinition.AgentDefinitionView;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.mp.common.enums.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 智能体定义(agent_definition)默认接口实现
 *
 * @author qty
 */
@Service
@Transactional
class DefaultAgentDefinitionService implements AgentDefinitionService {

    @Autowired
    private AgentDefinitionView agentDefinitionView;

    @Autowired
    private AgentDefinitionCopyMapper copy;

    @Override
    public IPage<PageAgentDefinitionResponse> findAll(PageAgentDefinitionRequest pageRequest) {

        // 查询单表分页并转换响应
        var pageInfo = agentDefinitionView.findAll(pageRequest);
        return pageInfo.convert(entity -> copy.toPageResponse(entity));
    }

    @Override
    public IPage<PageAggregateAgentDefinitionResponse> findAggregateAll(PageAggregateAgentDefinitionRequest pageRequest) {

        // 查询智能体聚合分页数据
        return agentDefinitionView.findAggregateAll(pageRequest);
    }

    @Override
    public InfoAgentDefinitionResponse findById(String id) {

        // 查询并校验智能体定义存在
        var agentDefinition = agentDefinitionView.findById(id);
        AssertUtils.notEmpty(agentDefinition, "主键为[{}]的数据为空", id);
        return copy.toInfoResponse(agentDefinition);
    }

    @Override
    public InfoAggregateAgentDefinitionResponse findAggregateById(String id) {
        AssertUtils.notEmpty(id, "主键不能为空");

        // 查询聚合基础信息
        InfoAggregateAgentDefinitionResponse response = agentDefinitionView.findAggregateById(id);
        AssertUtils.notEmpty(response, "主键为[{}]的数据为空", id);

        // 填充关联配置与执行链路
        fillAggregateRelations(id, response);
        return response;
    }

    @Override
    public String save(CreateAgentDefinitionRequest createRequest) {

        // 校验智能体名称唯一性
        checkNameUnique(createRequest.getName(), null);

        // 构建并保存智能体定义
        AgentDefinition entity = copy.toEntity(createRequest);
        entity.setStatus(Status.ON);
        agentDefinitionView.save(entity);
        return entity.getId();
    }

    @Override
    public String updateById(UpdateAgentDefinitionRequest updateRequest) {

        // 查询并校验智能体定义存在
        AgentDefinition agentDefinition = agentDefinitionView.findById(updateRequest.getId());
        AssertUtils.notEmpty(agentDefinition, "主键[{}]的数据不存在", updateRequest.getId());

        // 校验智能体名称唯一性
        checkNameUnique(updateRequest.getName(), updateRequest.getId());

        // 构建并更新智能体定义
        AgentDefinition entity = copy.toEntity(updateRequest);
        agentDefinitionView.updateById(entity);
        return entity.getId();
    }

    @Override
    public void deleteByIds(List<String> ids) {

        // 兼容原删除入口，统一走级联删除
        deleteCascadeByIds(ids);
    }

    @Override
    public void enableStatus(String id) {
        AssertUtils.notEmpty(id, "主键不能为空");

        // 查询当前智能体定义
        AgentDefinition entity = agentDefinitionView.findById(id);
        AssertUtils.notEmpty(entity, "智能体定义[{}]不存在", id);

        // 设置为启用状态
        entity.setStatus(Status.ON);

        // 持久化更新
        agentDefinitionView.updateById(entity);
    }

    @Override
    public void disableStatus(String id) {
        AssertUtils.notEmpty(id, "主键不能为空");

        // 查询当前智能体定义
        AgentDefinition entity = agentDefinitionView.findById(id);
        AssertUtils.notEmpty(entity, "智能体定义[{}]不存在", id);

        // 设置为禁用状态
        entity.setStatus(Status.OFF);

        // 持久化更新
        agentDefinitionView.updateById(entity);
    }

    @Override
    public DeleteCascadeAgentDefinitionResponse deleteCascadeByIds(List<String> ids) {
        AssertUtils.notEmpty(ids, "主键不能为空");

        // 校验删除对象全部存在
        validateDeleteAgents(ids);

        // 预统计删除影响范围
        DeleteCascadeAgentDefinitionResponse response = agentDefinitionView.countCascadeByIds(ids);

        // 按依赖顺序清理聚合子数据
        executeCascadeDelete(ids);
        return response;
    }

    /**
     * 填充智能体聚合详情关联数据。
     *
     * @param id 智能体主键
     * @param response 聚合详情响应
     */
    private void fillAggregateRelations(String id, InfoAggregateAgentDefinitionResponse response) {

        // 查询并设置技能列表
        response.setSkills(agentDefinitionView.findSkillsByAgentId(id));

        // 查询并设置规则列表
        response.setRules(agentDefinitionView.findRulesByAgentId(id));

        // 查询并设置子智能体关系列表
        response.setSubAgentRelations(agentDefinitionView.findSubAgentRelationsByAgentId(id));

        // 查询并设置记忆列表
        response.setMemories(agentDefinitionView.findMemoriesByAgentId(id));

        // 查询并设置记忆详情列表
        response.setMemoryDetails(agentDefinitionView.findMemoryDetailsByAgentId(id));

        // 查询并设置任务列表
        response.setTasks(agentDefinitionView.findTasksByAgentId(id));

        // 查询并设置原子命令列表
        response.setAtomicCommands(agentDefinitionView.findAtomicCommandsByAgentId(id));
    }

    /**
     * 校验智能体名称唯一性。
     *
     * @param name 智能体名称
     * @param excludeId 排除主键
     */
    private void checkNameUnique(String name, String excludeId) {
        FindOneAgentDefinitionRequest request = new FindOneAgentDefinitionRequest();
        request.setName(name);

        FindOneAgentDefinitionRequest neRequest = new FindOneAgentDefinitionRequest();
        neRequest.setId(excludeId);

        // 查询同名智能体数量
        Long count = agentDefinitionView.findCount(request, neRequest);
        AssertUtils.isTrue(count == 0, "智能体名称[{}]已存在", name);
    }

    /**
     * 校验待删除智能体全部存在。
     *
     * @param ids 智能体主键列表
     */
    private void validateDeleteAgents(List<String> ids) {
        DeleteCascadeAgentDefinitionResponse response = agentDefinitionView.countCascadeByIds(ids);
        Long agentCount = response.getAgentCount();
        AssertUtils.isTrue(agentCount == ids.size(), "存在无效的智能体主键，无法删除");
    }

    /**
     * 执行级联删除。
     *
     * @param ids 智能体主键列表
     */
    private void executeCascadeDelete(List<String> ids) {

        // 清理聊天消息和会话，避免删除智能体后遗留可读取的对话数据
        agentDefinitionView.deleteChatMessageByAgentIds(ids);
        agentDefinitionView.deleteChatSessionByAgentIds(ids);

        // 解除技能关联的原子命令，保留全局命令和命令本体
        agentDefinitionView.unlinkAtomicCommandSkillByAgentIds(ids);

        // 清理任务详情与任务主表
        agentDefinitionView.deleteTaskDetailByAgentIds(ids);
        agentDefinitionView.deleteTaskByAgentIds(ids);

        // 清理记忆详情与记忆主表
        agentDefinitionView.deleteMemoryDetailByAgentIds(ids);
        agentDefinitionView.deleteMemoryByAgentIds(ids);

        // 清理双向子智能体关系、规则与技能
        agentDefinitionView.deleteSubAgentRelationByAgentIds(ids);
        agentDefinitionView.deleteRuleByAgentIds(ids);
        agentDefinitionView.deleteSkillByAgentIds(ids);

        // 删除智能体定义主表
        agentDefinitionView.deleteByIds(ids);
    }
}
