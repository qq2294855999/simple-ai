package com.simple.ai.common.dto.agentDefinition;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.simple.ai.common.dto.agentMemory.PageAgentMemoryResponse;
import com.simple.ai.common.dto.agentRule.PageAgentRuleResponse;
import com.simple.ai.common.dto.agentSkill.PageAgentSkillResponse;
import com.simple.ai.common.dto.atomicCommand.PageAtomicCommandResponse;
import com.simple.ai.common.dto.subAgentRelation.PageSubAgentRelationResponse;
import com.simple.ai.common.dto.task.PageTaskResponse;
import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 智能体定义聚合详情响应。
 *
 * @author qty
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(title = "智能体定义聚合详情响应")
public class InfoAggregateAgentDefinitionResponse {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "定义描述")
    private String definitionDesc;

    @Schema(description = "第一铁律")
    private String firstPrinciple;

    @Schema(description = "第二规则")
    private String secondRule;

    @Schema(description = "第三技能")
    private String thirdSkill;

    @Schema(description = "默认模型主键")
    private String defaultModelId;

    @Schema(description = "状态")
    private Status status;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "修改时间")
    private Date updateTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "技能列表")
    private List<PageAgentSkillResponse> skills = new ArrayList<>();

    @Schema(description = "规则列表")
    private List<PageAgentRuleResponse> rules = new ArrayList<>();

    @Schema(description = "子智能体关系列表")
    private List<PageSubAgentRelationResponse> subAgentRelations = new ArrayList<>();

    @Schema(description = "记忆列表")
    private List<PageAgentMemoryResponse> memories = new ArrayList<>();

    @Schema(description = "任务列表")
    private List<PageTaskResponse> tasks = new ArrayList<>();

    @Schema(description = "原子命令列表")
    private List<PageAtomicCommandResponse> atomicCommands = new ArrayList<>();
}