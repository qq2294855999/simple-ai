package com.simple.ai.common.dto.agentMemoryDetail;

import com.simple.ai.common.enums.AgentStepTypeProcess;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 智能体记忆详情新增请求参数。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "智能体记忆详情新增请求参数")
public class CreateAgentMemoryDetailRequest {

    @Schema(description = "智能体记忆ID")
    @NotEmpty(message = "智能体记忆ID不能为空")
    private String agentMemoryId;

    @Schema(description = "步骤名称")
    @NotEmpty(message = "步骤名称不能为空")
    private String stepName;

    @Schema(description = "步骤类型")
    @NotEmpty(message = "步骤类型不能为空")
    private AgentStepTypeProcess stepType;

    @Schema(description = "执行内容")
    @NotEmpty(message = "执行内容不能为空")
    private String execContent;

    @Schema(description = "返回数据格式")
    @NotEmpty(message = "返回数据格式不能为空")
    private String returnDataFormat;

    @Schema(description = "父步骤ID")
    private String parentStepId;

    @Schema(description = "下一个步骤ID")
    private String nextStepId;

    @Schema(description = "分支条件")
    private String branchCondition;

    @Schema(description = "分支路由")
    private String branchRoute;

    @Schema(description = "模型")
    private String model;

    @Schema(description = "备注")
    private String remark;
}