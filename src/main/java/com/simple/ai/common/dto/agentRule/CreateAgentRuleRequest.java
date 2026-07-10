package com.simple.ai.common.dto.agentRule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 智能体规则(agent_rule)创建请求参数。
 *
 * @author qty
 */
@Data
@Schema(title = "智能体规则(agent_rule)创建请求参数")
public class CreateAgentRuleRequest {

    @Schema(description = "智能体ID")
    @NotEmpty(message = "智能体ID不能为空")
    private String agentId;

    @Schema(description = "定义描述")
    @NotEmpty(message = "定义描述不能为空")
    private String definitionDesc;

    @Schema(description = "触发条件")
    @NotEmpty(message = "触发条件不能为空")
    private String triggerCondition;

    @Schema(description = "触发动作")
    @NotEmpty(message = "触发动作不能为空")
    private String triggerAction;

    @Schema(description = "备注")
    private String remark;
}

