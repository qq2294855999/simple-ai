package com.simple.ai.common.dto.subAgentRelation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 子智能体关联(sub_agent_relation)创建请求参数。
 *
 * @author qty
 */
@Data
@Schema(title = "子智能体关联(sub_agent_relation)创建请求参数")
public class CreateSubAgentRelationRequest {

    @Schema(description = "主智能体")
    @NotEmpty(message = "主智能体不能为空")
    private String mainAgentId;

    @Schema(description = "子智能体")
    @NotEmpty(message = "子智能体不能为空")
    private String subAgentId;

    @Schema(description = "备注")
    private String remark;
}

