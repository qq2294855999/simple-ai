package com.simple.ai.common.dto.taskDetail;

import java.util.Date;

import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * 任务详情创建请求，前端仅提交用户填写字段，系统字段由服务端处理。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "任务详情(task_detail)创建请求参数")
public class CreateTaskDetailRequest {

    @Schema(description = "任务主键")
    @NotEmpty(message = "任务主键不能为空")
    private String taskId;

    @Schema(description = "任务名称")
    @NotEmpty(message = "任务名称不能为空")
    private String taskName;

    @Schema(description = "父任务ID")
    private String parentTaskId;

    @Schema(description = "下一个任务ID")
    private String nextTaskId;

    @Schema(description = "步骤类型：智能体步骤类型")
    @NotEmpty(message = "步骤类型：智能体步骤类型不能为空")
    private String stepType;

    @Schema(description = "分支条件")
    private String branchCondition;

    @Schema(description = "分支路由")
    private String branchRoute;

    @Schema(description = "备注")
    private String remark;
}

