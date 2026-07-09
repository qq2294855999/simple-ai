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

    @Schema(description = "执行状态")
    @NotNull(message = "执行状态不能为空")
    private Integer executionStatus;
}

