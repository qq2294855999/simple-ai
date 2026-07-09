package com.simple.ai.common.dto.taskDetail;

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
@Schema(title = "任务详情(task_detail)创建请求参数")
public class CreateTaskDetailRequest {

    @Schema(description = "任务主键")
    @NotEmpty(message = "任务主键不能为空")
    private String taskId;

    @Schema(description = "序号")
    @NotNull(message = "序号不能为空")
    private Integer sequence;

    @Schema(description = "步骤名称")
    @NotEmpty(message = "步骤名称不能为空")
    private String stepName;

    @Schema(description = "步骤类型")
    @NotEmpty(message = "步骤类型不能为空")
    private String stepType;

    @Schema(description = "步骤内容")
    @NotEmpty(message = "步骤内容不能为空")
    private String stepContent;

    @Schema(description = "请求参数")
    @NotEmpty(message = "请求参数不能为空")
    private String requestParams;

    @Schema(description = "返回参数")
    @NotEmpty(message = "返回参数不能为空")
    private String responseParams;

    @Schema(description = "执行状态")
    @NotNull(message = "执行状态不能为空")
    private Integer executionStatus;
}

