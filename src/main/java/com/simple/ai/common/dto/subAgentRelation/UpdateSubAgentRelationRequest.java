package com.simple.ai.common.dto.subAgentRelation;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(title = "子智能体关联(sub_agent_relation)修改请求参数")
public class UpdateSubAgentRelationRequest extends CreateSubAgentRelationRequest {

    @Schema(description = "主键")
    @NotEmpty(message = "主键不能为空")
    private String id;

}

