package com.simple.ai.common.dto.subAgentRelation;

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
@Schema(title = "子智能体关联(sub_agent_relation)创建请求参数")
public class CreateSubAgentRelationRequest {

    @Schema(description = "主智能体")
    @NotEmpty(message = "主智能体不能为空")
    private String mainAgentId;

    @Schema(description = "子智能体")
    @NotEmpty(message = "子智能体不能为空")
    private String subAgentId;

    @Schema(description = "扩展")
    @NotEmpty(message = "扩展不能为空")
    private String reserver;

    @Schema(description = "备注")
    private String remark;
}

