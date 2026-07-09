package com.simple.ai.common.dto.agentSkill;

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
@Schema(title = "智能体技能(agent_skill)创建请求参数")
public class CreateAgentSkillRequest {

    @Schema(description = "名称")
    @NotEmpty(message = "名称不能为空")
    private String name;

    @Schema(description = "定义描述")
    @NotEmpty(message = "定义描述不能为空")
    private String definitionDesc;
}

