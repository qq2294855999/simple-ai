package com.simple.ai.service.agent;

import com.simple.ai.common.constant.AgentIronRuleConstant;
import com.simple.ai.common.dto.agent.AgentContext;
import com.simple.ai.common.dto.agentMemory.FindAllAgentMemoryRequest;
import com.simple.ai.common.dto.agentRule.FindAllAgentRuleRequest;
import com.simple.ai.common.dto.agentSkill.FindAllAgentSkillRequest;
import com.simple.ai.common.dto.command.CommandDispatchRequest;
import com.simple.ai.common.dto.subAgentRelation.FindAllSubAgentRelationRequest;
import com.simple.ai.common.entity.agentClient.AgentClient;
import com.simple.ai.common.entity.agentDefinition.AgentDefinition;
import com.simple.ai.common.entity.agentExecutor.AgentExecutor;
import com.simple.ai.common.entity.agentMemory.AgentMemory;
import com.simple.ai.common.entity.agentRule.AgentRule;
import com.simple.ai.common.entity.agentSkill.AgentSkill;
import com.simple.ai.common.entity.subAgentRelation.SubAgentRelation;
import com.simple.ai.common.view.agentClient.AgentClientView;
import com.simple.ai.common.view.agentDefinition.AgentDefinitionView;
import com.simple.ai.common.view.agentExecutor.AgentExecutorView;
import com.simple.ai.common.view.agentMemory.AgentMemoryView;
import com.simple.ai.common.view.agentRule.AgentRuleView;
import com.simple.ai.common.view.agentSkill.AgentSkillView;
import com.simple.ai.common.view.subAgentRelation.SubAgentRelationView;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.mp.common.enums.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 智能体上下文组装器。
 *
 * <p>按 userId 过滤所有资产，确保多用户数据隔离。
 * 通过会话绑定的客户端解析执行器信息，将当前会话上下文写入提示词供 AI 直接感知。</p>
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
     * 客户端实例视图，用于从 clientId 解析客户端名称和执行器类型
     */
    @Autowired
    private AgentClientView agentClientView;

    /**
     * 执行器类型视图，用于从 executorId 解析执行器编码和名称
     */
    @Autowired
    private AgentExecutorView agentExecutorView;

    /**
     * 组装智能体上下文。
     *
     * <p>按请求中的 userId 过滤规则/技能/记忆等资产，确保多用户数据隔离。
     * 通过会话绑定的客户端解析执行器信息，将当前会话上下文写入提示词供 AI 直接感知。</p>
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

        // 解析当前会话的客户端和执行器信息，供提示词直接告知AI
        AgentContext sessionContext = resolveSessionContext(request.getClientId());

        // 构建上下文对象
        return buildContext(agentDefinition, rules, skills, subAgentRelations, memories, sessionContext, request.getUserId(), request.getClientId(), request.getSessionId());
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

        // 按用户ID过滤，实现多用户数据隔离
        if (userId != null && !userId.isBlank()) {
            request.setUserId(userId);
        }
        return agentMemoryView.findAll(request);
    }

    /**
     * 解析当前会话的客户端和执行器信息。
     *
     * <p>通过 clientId 查询客户端实例获取名称、在线状态和执行器类型ID，
     * 再通过执行器类型ID查询执行器编码和名称，将完整会话上下文填入 AgentContext。</p>
     *
     * @param clientId 客户端ID
     * @return 填充了会话上下文信息的 AgentContext 对象
     */
    private AgentContext resolveSessionContext(String clientId) {
        AgentContext context = new AgentContext();

        // 客户端ID为空时返回空上下文，不阻塞主流程
        if (clientId == null || clientId.isBlank()) {
            return context;
        }

        // 查询客户端实例，获取名称、在线状态和执行器类型ID
        AgentClient client = agentClientView.findById(clientId);
        if (client == null) {
            return context;
        }

        context.setClientId(clientId);
        context.setClientName(client.getClientName() != null ? client.getClientName() : "");
        context.setClientOnline(client.getIsOnline() != null ? client.getIsOnline() : false);

        // 通过客户端关联的执行器类型ID查询执行器编码和名称
        String executorId = client.getExecutorId();
        if (executorId != null && !executorId.isBlank()) {
            context.setExecutorId(executorId);
            AgentExecutor executor = agentExecutorView.findById(executorId);
            if (executor != null) {
                context.setExecutorCode(executor.getExecutorCode() != null ? executor.getExecutorCode() : "");
                context.setExecutorName(executor.getExecutorName() != null ? executor.getExecutorName() : "");
            }
        }

        return context;
    }

    /**
     * 构建上下文对象。
     *
     * @param agentDefinition 智能体定义
     * @param rules 规则列表
     * @param skills 技能列表
     * @param subAgentRelations 子智能体关系列表
     * @param memories 候选记忆列表
     * @param sessionContext 会话上下文（含客户端和执行器信息）
     * @param userId 用户ID
     * @param clientId 客户端ID
     * @param sessionId 会话ID
     * @return 智能体上下文
     */
    private AgentContext buildContext(AgentDefinition agentDefinition, List<AgentRule> rules, List<AgentSkill> skills, List<SubAgentRelation> subAgentRelations, List<AgentMemory> memories,
                                      AgentContext sessionContext, String userId, String clientId, String sessionId) {
        AgentContext context = new AgentContext();
        context.setAgentDefinition(agentDefinition);
        context.setSystemIronRule(AgentIronRuleConstant.SYSTEM_IRON_RULE);
        context.setRules(rules);
        context.setSkills(skills);
        context.setSubAgentRelations(subAgentRelations);
        context.setMemories(memories);

        // 注入可信上下文：当前用户ID和会话ID
        context.setUserId(userId);
        context.setSessionId(sessionId != null ? sessionId : "");

        // 注入会话上下文：客户端和执行器信息
        context.setClientId(sessionContext.getClientId() != null ? sessionContext.getClientId() : (clientId != null ? clientId : ""));
        context.setClientName(sessionContext.getClientName() != null ? sessionContext.getClientName() : "");
        context.setClientOnline(sessionContext.getClientOnline() != null ? sessionContext.getClientOnline() : false);
        context.setExecutorId(sessionContext.getExecutorId() != null ? sessionContext.getExecutorId() : "");
        context.setExecutorCode(sessionContext.getExecutorCode() != null ? sessionContext.getExecutorCode() : "");
        context.setExecutorName(sessionContext.getExecutorName() != null ? sessionContext.getExecutorName() : "");

        context.setPromptContent(buildPromptContent(agentDefinition, rules, skills, subAgentRelations, memories, context));
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
     * @param context 智能体上下文（含会话上下文信息）
     * @return 提示词内容
     */
    private String buildPromptContent(AgentDefinition agentDefinition, List<AgentRule> rules, List<AgentSkill> skills, List<SubAgentRelation> subAgentRelations, List<AgentMemory> memories,
                                      AgentContext context) {
        StringBuilder builder = new StringBuilder();
        appendAgentDefinition(builder, agentDefinition);
        appendCurrentSession(builder, context);
        appendRules(builder, rules);
        appendSkills(builder, skills);
        appendSubAgentRelations(builder, subAgentRelations);
        appendMemories(builder, memories);
        return builder.toString();
    }

    /**
     * 追加当前会话上下文提示词。
     *
     * <p>将当前会话绑定的用户、客户端和执行器信息写入提示词，
     * 使 AI 无需工具调用即可感知会话上下文，精确知道命令应发往哪个客户端。</p>
     *
     * @param builder 提示词构建器
     * @param context 智能体上下文
     */
    private void appendCurrentSession(StringBuilder builder, AgentContext context) {
        builder.append("<current_session>\n");

        // 会话ID和用户ID
        builder.append("  <session_id>").append(context.getSessionId()).append("</session_id>\n");
        builder.append("  <user_id>").append(context.getUserId() != null ? context.getUserId() : "").append("</user_id>\n");

        // 客户端信息：ID、名称、在线状态
        builder.append("  <client id=\"").append(context.getClientId() != null ? context.getClientId() : "");
        builder.append("\" name=\"").append(context.getClientName());
        builder.append("\" online=\"").append(context.getClientOnline()).append("\" />\n");

        // 执行器信息：ID、编码、名称
        builder.append("  <executor id=\"").append(context.getExecutorId());
        builder.append("\" code=\"").append(context.getExecutorCode());
        builder.append("\" name=\"").append(context.getExecutorName()).append("\" />\n");

        builder.append("</current_session>\n\n");
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

        // 遍历已发布记忆，将记忆名称、摘要和参数定义写入提示词供 AI 意图识别
        for (AgentMemory memory : memories) {
            builder.append("  <memory>\n");
            builder.append("    <id>").append(memory.getId()).append("</id>\n");
            builder.append("    <name>").append(memory.getMemoryName()).append("</name>\n");
            builder.append("    <summary>").append(memory.getSummary() != null ? memory.getSummary() : "").append("</summary>\n");

            // 补充参数定义，供AI判断用户输入是否提供了足够参数
            if (memory.getParamsDefinition() != null && !memory.getParamsDefinition().isBlank()) {
                builder.append("    <params_definition>").append(memory.getParamsDefinition()).append("</params_definition>\n");
            }

            builder.append("  </memory>\n");
        }
        builder.append("</memories>\n\n");
    }

}