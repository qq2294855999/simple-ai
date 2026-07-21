package com.simple.ai.common.dto.agent;

import com.simple.ai.common.entity.agentDefinition.AgentDefinition;
import com.simple.ai.common.entity.agentExecutor.AgentExecutor;
import com.simple.ai.common.entity.agentMemory.AgentMemory;
import com.simple.ai.common.entity.agentMemoryDetail.AgentMemoryDetail;
import com.simple.ai.common.entity.agentRule.AgentRule;
import com.simple.ai.common.entity.agentSkill.AgentSkill;
import com.simple.ai.common.entity.subAgentRelation.SubAgentRelation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 智能体上下文参数。
 *
 * @author qty
 */
@Data
@Schema(title = "智能体上下文参数")
public class AgentContext {

    /**
     * 智能体定义
     */
    @Schema(description = "智能体定义")
    private AgentDefinition agentDefinition;

    /**
     * 系统铁律内容
     */
    @Schema(description = "系统铁律内容")
    private String systemIronRule;

    /**
     * 规则列表
     */
    @Schema(description = "规则列表")
    private List<AgentRule> rules;

    /**
     * 技能列表
     */
    @Schema(description = "技能列表")
    private List<AgentSkill> skills;

    /**
     * 子智能体关系列表
     */
    @Schema(description = "子智能体关系列表")
    private List<SubAgentRelation> subAgentRelations;

    /**
     * 候选记忆列表
     */
    @Schema(description = "候选记忆列表")
    private List<AgentMemory> memories;

    /**
     * 候选记忆详情列表
     */
    @Schema(description = "候选记忆详情列表")
    private List<AgentMemoryDetail> memoryDetails;

    /**
     * 会话摘要
     */
    @Schema(description = "会话摘要")
    private String sessionSummary;

    /**
     * 提示词内容
     */
    @Schema(description = "提示词内容")
    private String promptContent;

    /**
     * 执行器类型列表。
     * <p>当前用户下所有启用的执行器类型，用于 AI 决策时按 executor_type 筛选可用命令。</p>
     */
    @Schema(description = "执行器类型列表")
    private List<AgentExecutor> executors;
}
