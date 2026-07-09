package com.simple.ai.common.dto.agentDefinition;

import java.util.Date;

import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
@Schema(title = "智能体定义(agent_definition)创建请求参数")
public class CreateAgentDefinitionRequest {

    @Schema(description = "名称")
    @NotEmpty(message = "名称不能为空")
    private String name;

    @Schema(description = "定义描述")
    @NotEmpty(message = "定义描述不能为空")
    private String definitionDesc;

    @Schema(description = "第一铁律")
    @NotEmpty(message = "第一铁律不能为空")
    private String firstRule;

    @Schema(description = "第二规则")
    @NotEmpty(message = "第二规则不能为空")
    private String secondRule;

    @Schema(description = "第三技能")
    @NotEmpty(message = "第三技能不能为空")
    private String thirdSkill;

    @Schema(description = "模型")
    @NotEmpty(message = "模型不能为空")
    private String model;

    @Schema(description = "创建人")
    @NotEmpty(message = "创建人不能为空")
    private String createBy;

    @Schema(description = "修改人")
    @NotEmpty(message = "修改人不能为空")
    private String updateBy;
}

