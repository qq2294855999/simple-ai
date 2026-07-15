package com.simple.ai.common.dto.agentDefinition;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 智能体定义(agent_definition)创建请求参数。
 *
 * @author qty
 */
@Data
@Schema(title = "智能体定义(agent_definition)创建请求参数")
public class CreateAgentDefinitionRequest {

    @Schema(description = "名称")
    @NotEmpty(message = "名称不能为空")
    private String name;

    @Schema(description = "定义描述")
    @NotEmpty(message = "定义描述不能为空")
    private String definitionDesc;

    @Schema(description = "第一铁律（系统级常量，运行时自动注入）")
    private String firstPrinciple;

    @Schema(description = "第二规则（系统级常量，运行时自动注入）")
    private String secondRule;

    @Schema(description = "第三技能（系统级常量，运行时自动注入）")
    private String thirdSkill;

    @Schema(description = "默认模型主键")
    private String defaultModelId;

    @Schema(description = "备注")
    private String remark;
}

