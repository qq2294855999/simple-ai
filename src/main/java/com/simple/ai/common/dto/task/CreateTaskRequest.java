package com.simple.ai.common.dto.task;

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
@Schema(title = "任务(task)创建请求参数")
public class CreateTaskRequest {

    @Schema(description = "智能体记忆主键")
    @NotEmpty(message = "智能体记忆主键不能为空")
    private String agentMemoryId;

    @Schema(description = "任务名称")
    @NotEmpty(message = "任务名称不能为空")
    private String taskName;

    @Schema(description = "父任务ID")
    @NotEmpty(message = "父任务ID不能为空")
    private String parentTaskId;

    @Schema(description = "下一个任务ID")
    @NotEmpty(message = "下一个任务ID不能为空")
    private String nextTaskId;

    @Schema(description = "步骤类型：智能体步骤类型")
    @NotEmpty(message = "步骤类型：智能体步骤类型不能为空")
    private String stepType;

    @Schema(description = "分支条件")
    @NotEmpty(message = "分支条件不能为空")
    private String branchCondition;

    @Schema(description = "分支路由")
    @NotEmpty(message = "分支路由不能为空")
    private String branchRoute;

    @Schema(description = "请求参数")
    @NotEmpty(message = "请求参数不能为空")
    private String requestParams;

    @Schema(description = "返回参数")
    @NotEmpty(message = "返回参数不能为空")
    private String returnParams;

    @Schema(description = "执行状态")
    @NotEmpty(message = "执行状态不能为空")
    private String execStatus;

    @Schema(description = "失败原因")
    @NotEmpty(message = "失败原因不能为空")
    private String failureReason;

    @Schema(description = "扩展")
    @NotEmpty(message = "扩展不能为空")
    private String reserver;

    @Schema(description = "备注")
    private String remark;
}

