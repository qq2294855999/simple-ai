package com.simple.ai.common.dto.agent;

import com.simple.ai.common.dto.command.CapabilityResultDto;
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
 * 智能体上下文快照，用于会话创建期序列化存储到 reserve 字段。
 * <p>与 AgentContext 结构一致，但专门用于 JSON 序列化/反序列化，
 * 确保聊天期可以从快照恢复完整的智能体上下文，无需重复查询资产表。</p>
 *
 * @author qty
 */
@Data
@Schema(title = "智能体上下文快照")
public class AgentContextSnapshot {

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
     * 已发布记忆列表（供AI意图识别参考）
     */
    @Schema(description = "已发布记忆列表")
    private List<AgentMemory> memories;

    /**
     * 当前会话绑定的客户端实例（完整对象，含名称、在线状态等）
     */
    @Schema(description = "当前会话绑定的客户端实例")
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
     * 已启用的原子命令列表（供 AI 通过 executeAtomicCommand 工具调用）
     */
    @Schema(description = "已启用的原子命令列表")
    private List<AtomicCommand> atomicCommands;

    /**
     * 执行器实时能力命令列表（从 system.capability 获取，用于与本地命令交叉校验）
     */
    @Schema(description = "执行器实时能力命令列表")
    private List<CapabilityResultDto.CommandItem> commandCapabilities;

    /**
     * 快照版本号，用于后续兼容升级。
     * <p>版本规则：</p>
     * <ul>
     *   <li>主版本号（第一位）：快照字段结构变更时递增，如新增/删除字段</li>
     *   <li>次版本号（第二位）：快照数据内容刷新时递增，如刷新上下文</li>
     *   <li>新会话自动使用最新版本快照</li>
     *   <li>存量会话通过显式"刷新上下文"更新快照版本</li>
     * </ul>
     */
    @Schema(description = "快照版本号")
    private String version = "1.0";

    /**
     * 快照创建时间戳
     */
    @Schema(description = "快照创建时间戳")
    private Long createdAt;
}