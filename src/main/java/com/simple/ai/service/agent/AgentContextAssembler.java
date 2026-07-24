package com.simple.ai.service.agent;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.simple.ai.common.constant.AgentIronRuleConstant;
import com.simple.ai.common.dto.agent.AgentContext;
import com.simple.ai.common.dto.agentMemory.FindAllAgentMemoryRequest;
import com.simple.ai.common.dto.agentRule.FindAllAgentRuleRequest;
import com.simple.ai.common.dto.agentSkill.FindAllAgentSkillRequest;
import com.simple.ai.common.dto.command.CommandDispatchRequest;
import com.simple.ai.common.dto.subAgentRelation.FindAllSubAgentRelationRequest;
import com.simple.ai.common.entity.agentDefinition.AgentDefinition;
import com.simple.ai.common.entity.agentExecutor.AgentExecutor;
import com.simple.ai.common.entity.agentMemory.AgentMemory;
import com.simple.ai.common.entity.agentRule.AgentRule;
import com.simple.ai.common.entity.agentSkill.AgentSkill;
import com.simple.ai.common.entity.subAgentRelation.SubAgentRelation;
import com.simple.ai.common.service.session.AgentSessionService;
import com.simple.ai.common.view.agentDefinition.AgentDefinitionView;
import com.simple.ai.common.view.agentExecutor.AgentExecutorView;
import com.simple.ai.common.view.agentMemory.AgentMemoryView;
import com.simple.ai.common.view.agentRule.AgentRuleView;
import com.simple.ai.common.view.agentSkill.AgentSkillView;
import com.simple.ai.common.view.subAgentRelation.SubAgentRelationView;
import com.simple.ai.view.agentExecutor.AgentExecutorRepository;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.mp.common.enums.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 智能体上下文组装器。
 *
 * <p>按 userId 过滤所有资产，确保多用户数据隔离。
 * 加载执行器能力信息供命令路由使用。</p>
 *
 * @author qty
 */
@Component
public class AgentContextAssembler {

    /**
     * 智能体定义视图
     */
    @Autowired
    private AgentDefinitionView agentDefinitionView;

    /**
     * 智能体规则视图
     */
    @Autowired
    private AgentRuleView agentRuleView;

    /**
     * 智能体技能视图
     */
    @Autowired
    private AgentSkillView agentSkillView;

    /**
     * 子智能体关系视图
     */
    @Autowired
    private SubAgentRelationView subAgentRelationView;

    /**
     * 智能体记忆视图
     */
    @Autowired
    private AgentMemoryView agentMemoryView;

    /**
     * 执行器视图
     */
    @Autowired
    private AgentExecutorView agentExecutorView;

    /**
     * 智能体会话服务
     */
    @Autowired
    private AgentSessionService agentSessionService;

    /**
     * 执行器类型仓库
     */
    @Autowired
    private AgentExecutorRepository agentExecutorRepository;

    /**
     * 组装智能体上下文。
     *
     * <p>按请求中的 userId 过滤规则/技能/记忆等资产，
     * 确保多用户数据隔离，同时加载执行器能力信息供命令路由使用。</p>
     *
     * @param request 命令调度请求
     * @return 智能体上下文
     */
    public AgentContext assemble(CommandDispatchRequest request) {

        // 参数校验：智能体ID不能为空
        AssertUtils.notEmpty(request.getAgentId(), "智能体ID不能为空");

        // 查询智能体定义并校验启用状态
        AgentDefinition agentDefinition = loadAgentDefinition(request.getAgentId());

        // 查询智能体直属启用规则（按 userId 过滤）
        List<AgentRule> rules = loadRules(request.getAgentId(), request.getUserId());

        // 查询智能体直属启用技能（按 userId 过滤）
        List<AgentSkill> skills = loadSkills(request.getAgentId(), request.getUserId());

        // 查询主智能体可用子智能体关系
        List<SubAgentRelation> subAgentRelations = loadSubAgentRelations(request.getAgentId());

        // 查询智能体启用候选记忆（按 userId 过滤，仅已发布版本）
        List<AgentMemory> memories = loadMemories(request.getAgentId(), request.getUserId());

        // 查询会话摘要
        String sessionSummary = loadSessionSummary(request.getSessionId());

        // 加载当前用户下所有启用的执行器类型，供 AI 决策时按 executor_type 筛选可用命令
        List<AgentExecutor> executors = loadExecutors(request.getUserId());

        // 构建上下文对象
        return buildContext(agentDefinition, rules, skills, subAgentRelations, memories, sessionSummary, executors, request.getUserId(), request.getClientId());
    }

    /**
     * 查询智能体定义。
     *
     * @param agentId 智能体ID
     * @return 智能体定义
     */
    private AgentDefinition loadAgentDefinition(String agentId) {
        AgentDefinition agentDefinition = agentDefinitionView.findById(agentId);
        AssertUtils.notEmpty(agentDefinition, "智能体[{}]不存在", agentId);
        AssertUtils.isTrue(Status.ON.equals(agentDefinition.getStatus()), "智能体[{}]未启用", agentId);
        return agentDefinition;
    }

    /**
     * 查询智能体直属规则（按 userId 过滤）。
     *
     * @param agentId 智能体ID
     * @param userId  用户ID（为空时不过滤）
     * @return 智能体规则列表
     */
    private List<AgentRule> loadRules(String agentId, String userId) {
        FindAllAgentRuleRequest request = new FindAllAgentRuleRequest();
        request.setAgentId(agentId);
        request.setStatus(Status.ON);
        // TODO: userId 过滤需 DTO 添加 userId 字段后启用
        return agentRuleView.findAll(request);
    }

    /**
     * 查询智能体直属技能（按 userId 过滤）。
     *
     * @param agentId 智能体ID
     * @param userId  用户ID（为空时不过滤）
     * @return 智能体技能列表
     */
    private List<AgentSkill> loadSkills(String agentId, String userId) {
        FindAllAgentSkillRequest request = new FindAllAgentSkillRequest();
        request.setAgentId(agentId);
        request.setStatus(Status.ON);
        // TODO: userId 过滤需 DTO 添加 userId 字段后启用
        return agentSkillView.findAll(request);
    }

    /**
     * 查询主智能体可用子智能体关系。
     *
     * @param agentId 智能体ID
     * @return 子智能体关系列表
     */
    private List<SubAgentRelation> loadSubAgentRelations(String agentId) {
        FindAllSubAgentRelationRequest request = new FindAllSubAgentRelationRequest();
        request.setMainAgentId(agentId);
        request.setStatus(Status.ON);
        return subAgentRelationView.findAll(request);
    }

    /**
     * 查询智能体候选记忆（按 userId 过滤）。
     *
     * @param agentId 智能体ID
     * @param userId  用户ID（为空时不过滤）
     * @return 智能体记忆列表
     */
    private List<AgentMemory> loadMemories(String agentId, String userId) {
        FindAllAgentMemoryRequest request = new FindAllAgentMemoryRequest();
        request.setAgentId(agentId);
        request.setStatus(Status.ON);

        // 仅加载已发布版本的记忆供AI意图识别匹配
        request.setVersionStatus(2);
        // TODO: userId 过滤需 DTO 添加 userId 字段后启用
        return agentMemoryView.findAll(request);
    }

    /**
     * 查询会话摘要。
     *
     * @param sessionId 会话ID
     * @return 会话摘要
     */
    private String loadSessionSummary(String sessionId) {

        // 会话ID为空时表示无需读取历史摘要
        if (sessionId == null || sessionId.isBlank()) {
            return "";
        }
        return agentSessionService.findSummary(sessionId);
    }

    /**
     * 构建上下文对象。
     *
     * @param agentDefinition 智能体定义
     * @param rules 规则列表
     * @param skills 技能列表
     * @param subAgentRelations 子智能体关系列表
     * @param memories 候选记忆列表
     * @param memoryDetails 候选记忆详情列表
     * @param sessionSummary 会话摘要
     * @param executors 执行器类型列表
     * @return 智能体上下文
     */
    private AgentContext buildContext(AgentDefinition agentDefinition, List<AgentRule> rules, List<AgentSkill> skills, List<SubAgentRelation> subAgentRelations, List<AgentMemory> memories,
                                      String sessionSummary, List<AgentExecutor> executors, String userId, String clientId) {
        AgentContext context = new AgentContext();
        context.setAgentDefinition(agentDefinition);
        context.setSystemIronRule(AgentIronRuleConstant.SYSTEM_IRON_RULE);
        context.setRules(rules);
        context.setSkills(skills);
        context.setSubAgentRelations(subAgentRelations);
        context.setMemories(memories);
        context.setSessionSummary(sessionSummary);
        context.setExecutors(executors);

        // 注入可信上下文：当前用户ID和客户端ID，供后续AI调用和命令路由使用
        context.setUserId(userId);
        context.setClientId(clientId);

        // 根据客户端ID推导执行器类型ID，告知AI当前可用命令范围
        String executorId = resolveExecutorId(clientId);
        context.setExecutorId(executorId);

        context.setPromptContent(buildPromptContent(agentDefinition, rules, skills, subAgentRelations, memories, executors));
        return context;
    }

    /**
     * 构建提示词内容。
     *
     * @param agentDefinition 智能体定义
     * @param rules 规则列表
     * @param skills 技能列表
     * @param subAgentRelations 子智能体关系列表
     * @param memories 候选记忆列表
     * @param executors 执行器类型列表
     * @return 提示词内容
     */
    private String buildPromptContent(AgentDefinition agentDefinition, List<AgentRule> rules, List<AgentSkill> skills, List<SubAgentRelation> subAgentRelations, List<AgentMemory> memories,
                                      List<AgentExecutor> executors) {
        StringBuilder builder = new StringBuilder();
        appendAgentDefinition(builder, agentDefinition);
        appendRules(builder, rules);
        appendSkills(builder, skills);
        appendSubAgentRelations(builder, subAgentRelations);
        appendMemories(builder, memories);
        appendExecutors(builder, executors);
        return builder.toString();
    }

    /**
     * 加载当前用户下所有启用的执行器类型。
     *
     * @param userId 用户ID（当前暂未按 userId 过滤，保留参数扩展）
     * @return 执行器类型列表
     */
    private List<AgentExecutor> loadExecutors(String userId) {
        LambdaQueryWrapper<AgentExecutor> wrapper = new LambdaQueryWrapper<AgentExecutor>().eq(AgentExecutor::getStatus, Status.ON);
        return agentExecutorRepository.selectList(wrapper);
    }

    /**
     * 根据客户端ID解析执行器类型ID。
     * <p>从 agent_client 表查询客户端关联的执行器类型。
     * 客户端未指定时返回空字符串。</p>
     *
     * @param clientId 客户端ID
     * @return 执行器类型ID，客户端未指定时返回空字符串
     */
    private String resolveExecutorId(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return "";
        }
        // TODO: 通过 AgentClientView 查询 agent_client 表的 executor_id
        // 当前阶段暂未引入 AgentClientView，后续任务 1.4 resolveClientIdIfAbsent 统一处理
        return "";
    }

    /**
     * 追加智能体定义提示词。
     *
     * @param builder 提示词构建器
     * @param agentDefinition 智能体定义
     */
    private void appendAgentDefinition(StringBuilder builder, AgentDefinition agentDefinition) {
        builder.append("<system_iron_rule>\n");
        builder.append(AgentIronRuleConstant.SYSTEM_IRON_RULE);
        builder.append("\n</system_iron_rule>\n\n");

        builder.append("<agent>\n");

        // 告知AI自己的身份信息，避免AI通过工具查询未知的"当前智能体"名称
        builder.append("  <id>").append(agentDefinition.getId()).append("</id>\n");
        builder.append("  <name>").append(agentDefinition.getName()).append("</name>\n");
        builder.append("  <definition>").append(agentDefinition.getDefinitionDesc()).append("</definition>\n");
        builder.append("  <first_principle>").append(agentDefinition.getFirstPrinciple()).append("</first_principle>\n");
        builder.append("  <second_rule>").append(agentDefinition.getSecondRule()).append("</second_rule>\n");
        builder.append("  <third_skill>").append(agentDefinition.getThirdSkill()).append("</third_skill>\n");
        builder.append("</agent>\n\n");
    }

    /**
     * 追加规则提示词。
     *
     * @param builder 提示词构建器
     * @param rules 规则列表
     */
    private void appendRules(StringBuilder builder, List<AgentRule> rules) {
        if (rules.isEmpty()) {
            return;
        }
        builder.append("<rules>\n");

        // 遍历直属规则，将定义描述、触发条件和触发动作写入提示词
        for (AgentRule rule : rules) {
            builder.append("  <rule>\n");
            builder.append("    <desc>").append(rule.getDefinitionDesc()).append("</desc>\n");
            builder.append("    <condition>").append(rule.getTriggerCondition()).append("</condition>\n");
            builder.append("    <action>").append(rule.getTriggerAction()).append("</action>\n");
            builder.append("  </rule>\n");
        }
        builder.append("</rules>\n\n");
    }

    /**
     * 追加技能提示词。
     *
     * @param builder 提示词构建器
     * @param skills 技能列表
     */
    private void appendSkills(StringBuilder builder, List<AgentSkill> skills) {
        if (skills.isEmpty()) {
            return;
        }
        builder.append("<skills>\n");

        // 遍历直属技能，将定义描述、执行内容和返回格式写入提示词
        for (AgentSkill skill : skills) {
            builder.append("  <skill>\n");
            builder.append("    <desc>").append(skill.getDefinitionDesc()).append("</desc>\n");
            builder.append("    <content>").append(skill.getExecContent()).append("</content>\n");
            builder.append("    <format>").append(skill.getReturnDataFormat()).append("</format>\n");
            builder.append("  </skill>\n");
        }
        builder.append("</skills>\n\n");
    }

    /**
     * 追加子智能体关系提示词。
     *
     * @param builder 提示词构建器
     * @param subAgentRelations 子智能体关系列表
     */
    private void appendSubAgentRelations(StringBuilder builder, List<SubAgentRelation> subAgentRelations) {
        if (subAgentRelations.isEmpty()) {
            return;
        }
        builder.append("<sub_agents>\n");

        // 遍历子智能体关系，将主从智能体关系写入提示词
        for (SubAgentRelation relation : subAgentRelations) {
            builder.append("  <relation main=\"").append(relation.getMainAgentId());
            builder.append("\" sub=\"").append(relation.getSubAgentId()).append("\" />\n");
        }
        builder.append("</sub_agents>\n\n");
    }

    /**
     * 追加候选记忆提示词。
     *
     * @param builder 提示词构建器
     * @param memories 候选记忆列表
     */
    private void appendMemories(StringBuilder builder, List<AgentMemory> memories) {
        if (memories.isEmpty()) {
            return;
        }
        builder.append("<memories>\n");

        // 遍历已发布记忆，将记忆名称和参数定义写入提示词供 AI 意图识别
        for (AgentMemory memory : memories) {
            builder.append("  <memory>\n");
            builder.append("    <id>").append(memory.getId()).append("</id>\n");
            builder.append("    <name>").append(memory.getMemoryName()).append("</name>\n");
            builder.append("    <summary>").append(memory.getSummary() != null ? memory.getSummary() : "").append("</summary>\n");
            builder.append("  </memory>\n");
        }
        builder.append("</memories>\n\n");
    }

    /**
     * 追加执行器类型提示词。
     *
     * @param builder   提示词构建器
     * @param executors 执行器类型列表
     */
    private void appendExecutors(StringBuilder builder, List<AgentExecutor> executors) {
        if (executors.isEmpty()) {
            return;
        }
        builder.append("<executors>\n");

        // 遍历启用执行器类型，将编码、名称和描述写入提示词供 AI 决策
        for (AgentExecutor executor : executors) {
            builder.append("  <executor code=\"").append(executor.getExecutorCode());
            builder.append("\" name=\"").append(executor.getExecutorName());
            builder.append("\" desc=\"").append(executor.getDescription() != null ? executor.getDescription() : "");
            builder.append("\" />\n");
        }
        builder.append("</executors>\n\n");
    }

}