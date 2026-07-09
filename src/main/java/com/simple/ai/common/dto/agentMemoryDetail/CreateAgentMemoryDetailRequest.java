package com.simple.ai.common.dto.agentMemoryDetail;

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
@Schema(title = "智能体记忆详情(agent_memory_detail)创建请求参数")
public class CreateAgentMemoryDetailRequest {

    @Schema(description = "智能体记忆ID")
    @NotEmpty(message = "智能体记忆ID不能为空")
    private String agentMemoryId;

    @Schema(description = "步骤名称")
    @NotEmpty(message = "步骤名称不能为空")
    private String stepName;

    @Schema(description = "父步骤ID")
    @NotEmpty(message = "父步骤ID不能为空")
    private String parentStepId;

    @Schema(description = "下一个步骤ID")
    @NotEmpty(message = "下一个步骤ID不能为空")
    private String nextStepId;

    @Schema(description = "步骤类型")
    @NotEmpty(message = "步骤类型不能为空")
    private String stepType;

    @Schema(description = "分支条件")
    @NotEmpty(message = "分支条件不能为空")
    private String branchCondition;

    @Schema(description = "分支路由")
    @NotEmpty(message = "分支路由不能为空")
    private String branchRoute;
}

