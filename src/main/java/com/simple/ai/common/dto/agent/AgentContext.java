package com.simple.ai.common.dto.agent;

import com.simple.ai.common.entity.agentClient.AgentClient;
import com.simple.ai.common.entity.agentDefinition.AgentDefinition;
import com.simple.ai.common.entity.agentExecutor.AgentExecutor;
import com.simple.ai.common.entity.agentMemory.AgentMemory;
import com.simple.ai.common.entity.agentRule.AgentRule;
import com.simple.ai.common.entity.agentSkill.AgentSkill;
import com.simple.ai.common.entity.atomicCommand.AtomicCommand;
import com.simple.ai.common.entity.protocol.Protocol;
import com.simple.ai.common.entity.subAgentRelation.SubAgentRelation;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 智能体上下文参数。
 *
 * <p>以完整对象封装智能体所有资产（定义、规则、技能、子智能体、记忆、客户端、执行器、协议），
 * 不暴露散落的 ID 字段，提示词构建时从对象中取名称/编码/状态。</p>
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
     * 启用记忆列表（供AI意图识别参考）
     */
    @Schema(description = "启用记忆列表")
    private List<AgentMemory> memories;

    /**
     * 当前会话绑定的客户端实例（完整对象，含名称、在线状态等）
     */
    @Schema(description = "当前会话客户端实例")
    private AgentClient client;

    /**
     * 当前客户端关联的执行器类型（完整对象，含编码、名称、描述等）
     */
    @Schema(description = "当前执行器类型")
    private AgentExecutor executor;

    /**
     * 当前执行器关联的通信协议（完整对象，含协议名称等）
     */
    @Schema(description = "当前通信协议")
    private Protocol protocol;

    /**
     * 提示词内容
     */
    @Schema(description = "提示词内容")
    private String promptContent;

    /**
     * 当前登录用户ID，用于按用户过滤资产和校验权限。
     */
    @Schema(description = "当前登录用户ID")
    private String userId;

    /**
     * 当前会话ID，用于AI调用时传递会话上下文。
     */
    @Schema(description = "当前会话ID")
    private String sessionId;

    /**
     * 已启用的原子命令列表（供 AI 通过 executeAtomicCommand 工具调用）。
     */
    @Schema(description = "已启用的原子命令列表")
    private List<AtomicCommand> atomicCommands;
}
