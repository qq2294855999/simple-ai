package com.simple.ai.common.dto.agent;

import com.simple.ai.common.entity.agentDefinition.AgentDefinition;
import com.simple.ai.common.entity.agentMemory.AgentMemory;
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
     * 已发布记忆列表（供AI意图识别参考）
     */
    @Schema(description = "已发布记忆列表")
    private List<AgentMemory> memories;

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
     * 当前使用的客户端ID，用于点对点下发命令。
     */
    @Schema(description = "当前客户端ID")
    private String clientId;

    /**
     * 当前客户端名称，写入提示词供AI识别客户端身份。
     */
    @Schema(description = "当前客户端名称")
    private String clientName;

    /**
     * 当前客户端在线状态，写入提示词供AI判断命令是否可达。
     */
    @Schema(description = "当前客户端在线状态")
    private Boolean clientOnline;

    /**
     * 当前客户端关联的执行器类型ID，用于AI决策时了解可用命令范围。
     */
    @Schema(description = "当前执行器类型ID")
    private String executorId;

    /**
     * 当前执行器编码，写入提示词供AI识别执行器类型。
     */
    @Schema(description = "当前执行器编码")
    private String executorCode;

    /**
     * 当前执行器名称，写入提示词供AI识别执行器类型。
     */
    @Schema(description = "当前执行器名称")
    private String executorName;

    /**
     * 当前会话ID，用于AI调用时传递会话上下文。
     */
    @Schema(description = "当前会话ID")
    private String sessionId;
}