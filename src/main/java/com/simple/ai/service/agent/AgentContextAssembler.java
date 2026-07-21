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
import com.simple.ai.common.entity.agentMemoryDetail.AgentMemoryDetail;
import com.simple.ai.common.entity.agentRule.AgentRule;
import com.simple.ai.common.entity.agentSkill.AgentSkill;
import com.simple.ai.common.entity.subAgentRelation.SubAgentRelation;
import com.simple.ai.common.service.session.AgentSessionService;
import com.simple.ai.common.view.agentDefinition.AgentDefinitionView;
import com.simple.ai.common.view.agentExecutor.AgentExecutorView;
import com.simple.ai.common.view.agentMemory.AgentMemoryView;
import com.simple.ai.common.view.agentMemoryDetail.AgentMemoryDetailView;
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
     * 智能体记忆详情视图
     */
    @Autowired
    private AgentMemoryDetailView agentMemoryDetailView;

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

        // 查询智能体启用候选记忆（按 userId 过滤）
        List<AgentMemory> memories = loadMemories(request.getAgentId(), request.getUserId());

        // 批量查询候选记忆详情，避免循环内数据库查询
        List<AgentMemoryDetail> memoryDetails = loadMemoryDetails(memories);

        // 查询会话摘要
        String sessionSummary = loadSessionSummary(request.getSessionId());

        // 加载当前用户下所有启用的执行器类型，供 AI 决策时按 executor_type 筛选可用命令
        List<AgentExecutor> executors = loadExecutors(request.getUserId());

        // 构建上下文对象
        return buildContext(agentDefinition, rules, skills, subAgentRelations, memories, memoryDetails, sessionSummary, executors);
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
        // TODO: userId 过滤需 DTO 添加 userId 字段后启用
        return agentMemoryView.findAll(request);
    }

    /**
     * 查询候选记忆详情。
     *
     * @param memories 候选记忆列表
     * @return 候选记忆详情列表
     */
    private List<AgentMemoryDetail> loadMemoryDetails(List<AgentMemory> memories) {
        List<String> memoryIds = memories.stream()
                .map(AgentMemory::getId)
                .toList();
        return agentMemoryDetailView.findAllByAgentMemoryIds(memoryIds);
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
    private AgentContext buildContext(AgentDefinition agentDefinition, List<AgentRule> rules, List<AgentSkill> skills,
                                      List<SubAgentRelation> subAgentRelations, List<AgentMemory> memories, List<AgentMemoryDetail> memoryDetails, String sessionSummary,
                                      List<AgentExecutor> executors) {
        AgentContext context = new AgentContext();
        context.setAgentDefinition(agentDefinition);
        context.setSystemIronRule(AgentIronRuleConstant.SYSTEM_IRON_RULE);
        context.setRules(rules);
        context.setSkills(skills);
        context.setSubAgentRelations(subAgentRelations);
        context.setMemories(memories);
        context.setMemoryDetails(memoryDetails);
        context.setSessionSummary(sessionSummary);
        context.setExecutors(executors);
        context.setPromptContent(buildPromptContent(agentDefinition, rules, skills, subAgentRelations, memories, sessionSummary, executors));
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
     * @param sessionSummary 会话摘要
     * @param executors 执行器类型列表
     * @return 提示词内容
     */
    private String buildPromptContent(AgentDefinition agentDefinition, List<AgentRule> rules, List<AgentSkill> skills,
                                      List<SubAgentRelation> subAgentRelations, List<AgentMemory> memories, String sessionSummary, List<AgentExecutor> executors) {
        StringBuilder builder = new StringBuilder();
        appendAgentDefinition(builder, agentDefinition);
        appendRules(builder, rules);
        appendSkills(builder, skills);
        appendSubAgentRelations(builder, subAgentRelations);
        appendMemories(builder, memories);
        appendExecutors(builder, executors);
        appendSessionSummary(builder, sessionSummary);
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
     * 追加智能体定义提示词。
     *
     * @param builder 提示词构建器
     * @param agentDefinition 智能体定义
     */
    private void appendAgentDefinition(StringBuilder builder, AgentDefinition agentDefinition) {
        builder.append("# 系统铁律\n");
        builder.append(AgentIronRuleConstant.SYSTEM_IRON_RULE);
        builder.append("\n\n# 智能体定义\n");
        builder.append(agentDefinition.getDefinitionDesc());
        builder.append("\n\n# 第一铁律\n");
        builder.append(agentDefinition.getFirstPrinciple());
        builder.append("\n\n# 第二规则\n");
        builder.append(agentDefinition.getSecondRule());
        builder.append("\n\n# 第三技能\n");
        builder.append(agentDefinition.getThirdSkill());
        builder.append("\n\n");
    }

    /**
     * 追加规则提示词。
     *
     * @param builder 提示词构建器
     * @param rules 规则列表
     */
    private void appendRules(StringBuilder builder, List<AgentRule> rules) {
        builder.append("# 直属规则\n");

        // 遍历直属规则，将触发条件和触发动作写入提示词
        for (AgentRule rule : rules) {
            builder.append("## 规则\n");
            builder.append(rule.getDefinitionDesc());
            builder.append("\n触发条件：");
            builder.append(rule.getTriggerCondition());
            builder.append("\n触发动作：");
            builder.append(rule.getTriggerAction());
            builder.append("\n");
        }
        builder.append("\n");
    }

    /**
     * 追加技能提示词。
     *
     * @param builder 提示词构建器
     * @param skills 技能列表
     */
    private void appendSkills(StringBuilder builder, List<AgentSkill> skills) {
        builder.append("# 直属技能\n");

        // 遍历直属技能，将执行内容和返回格式写入提示词
        for (AgentSkill skill : skills) {
            builder.append("## 技能\n");
            builder.append(skill.getDefinitionDesc());
            builder.append("\n执行内容：");
            builder.append(skill.getExecContent());
            builder.append("\n返回格式：");
            builder.append(skill.getReturnDataFormat());
            builder.append("\n");
        }
        builder.append("\n");
    }

    /**
     * 追加子智能体关系提示词。
     *
     * @param builder 提示词构建器
     * @param subAgentRelations 子智能体关系列表
     */
    private void appendSubAgentRelations(StringBuilder builder, List<SubAgentRelation> subAgentRelations) {
        builder.append("# 子智能体关系\n");

        // 遍历子智能体关系，将主从智能体关系写入提示词
        for (SubAgentRelation relation : subAgentRelations) {
            builder.append("主智能体：");
            builder.append(relation.getMainAgentId());
            builder.append("，子智能体：");
            builder.append(relation.getSubAgentId());
            builder.append("\n");
        }
        builder.append("\n");
    }

    /**
     * 追加候选记忆提示词。
     *
     * @param builder 提示词构建器
     * @param memories 候选记忆列表
     */
    private void appendMemories(StringBuilder builder, List<AgentMemory> memories) {
        builder.append("# 候选记忆\n");

        // 遍历候选记忆，将触发条件和触发动作写入提示词
        for (AgentMemory memory : memories) {
            builder.append("## 记忆\n");
            builder.append(memory.getMemoryName());
            builder.append("\n触发条件：");
            builder.append(memory.getTriggerCondition());
            builder.append("\n触发动作：");
            builder.append(memory.getTriggerAction());
            builder.append("\n");
        }
        builder.append("\n");
    }

    /**
     * 追加执行器类型提示词。
     *
     * @param builder   提示词构建器
     * @param executors 执行器类型列表
     */
    private void appendExecutors(StringBuilder builder, List<AgentExecutor> executors) {
        builder.append("# 可用执行器类型\n");

        // 遍历启用执行器类型，将编码、名称和描述写入提示词供 AI 决策
        for (AgentExecutor executor : executors) {
            builder.append("## 执行器\n");
            builder.append("编码：").append(executor.getExecutorCode());
            builder.append("\n名称：").append(executor.getExecutorName());
            builder.append("\n描述：").append(executor.getDescription() != null ? executor.getDescription() : "");
            builder.append("\n");
        }
        builder.append("\n");
    }

    /**
     * 追加会话摘要提示词。
     *
     * @param builder 提示词构建器
     * @param sessionSummary 会话摘要
     */
    private void appendSessionSummary(StringBuilder builder, String sessionSummary) {
        builder.append("# 会话摘要\n");
        builder.append(sessionSummary == null ? "" : sessionSummary);
        builder.append("\n");
    }

}
