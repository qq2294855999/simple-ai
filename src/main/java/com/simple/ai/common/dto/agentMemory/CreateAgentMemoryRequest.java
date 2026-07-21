package com.simple.ai.common.dto.agentMemory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 智能体记忆新增请求参数。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "智能体记忆新增请求参数")
public class CreateAgentMemoryRequest {

    @Schema(description = "智能体ID")
    @NotEmpty(message = "智能体ID不能为空")
    private String agentId;

    @Schema(description = "记忆名称")
    @NotEmpty(message = "记忆名称不能为空")
    private String memoryName;

    @Schema(description = "步骤名称")
    @NotEmpty(message = "步骤名称不能为空")
    private String stepName;

    @Schema(description = "触发条件")
    @NotEmpty(message = "触发条件不能为空")
    private String triggerCondition;

    @Schema(description = "触发动作")
    @NotEmpty(message = "触发动作不能为空")
    private String triggerAction;

    @Schema(description = "备注")
    private String remark;
}
