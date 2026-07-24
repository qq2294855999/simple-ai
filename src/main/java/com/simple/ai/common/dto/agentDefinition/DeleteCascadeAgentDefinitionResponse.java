package com.simple.ai.common.dto.agentDefinition;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 智能体定义级联删除响应。
 *
 * @author qty
 */
@Data
@Schema(title = "智能体定义级联删除响应")
public class DeleteCascadeAgentDefinitionResponse {

    @Schema(description = "删除智能体数量")
    private Long agentCount;

    @Schema(description = "删除技能数量")
    private Long skillCount;

    @Schema(description = "删除规则数量")
    private Long ruleCount;

    @Schema(description = "删除子智能体关系数量")
    private Long subAgentRelationCount;

    @Schema(description = "删除记忆数量")
    private Long memoryCount;

    @Schema(description = "删除记忆步骤数量")
    private Long memoryStepCount;

    @Schema(description = "删除任务数量")
    private Long taskCount;

    @Schema(description = "删除任务详情数量")
    private Long taskDetailCount;

    @Schema(description = "解除技能关联的原子命令数量")
    private Long atomicCommandUnlinkCount;
}