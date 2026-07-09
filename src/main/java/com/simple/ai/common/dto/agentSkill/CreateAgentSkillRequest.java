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

    @Schema(description = "智能体ID")
    @NotEmpty(message = "智能体ID不能为空")
    private String agentId;

    @Schema(description = "定义描述")
    @NotEmpty(message = "定义描述不能为空")
    private String definitionDesc;

    @Schema(description = "执行内容")
    @NotEmpty(message = "执行内容不能为空")
    private String execContent;

    @Schema(description = "返回的数据格式")
    @NotEmpty(message = "返回的数据格式不能为空")
    private String returnDataFormat;

    @Schema(description = "扩展")
    @NotEmpty(message = "扩展不能为空")
    private String reserver;

    @Schema(description = "备注")
    private String remark;
}

