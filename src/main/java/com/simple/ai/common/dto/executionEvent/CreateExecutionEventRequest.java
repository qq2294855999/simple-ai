package com.simple.ai.common.dto.executionEvent;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 执行事件(execution_event)创建请求参数。
 * <p>系统内部创建，非前端表单提交。</p>
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "执行事件创建请求参数")
public class CreateExecutionEventRequest {

    @Schema(description = "轮次主键")
    @NotEmpty(message = "轮次主键不能为空")
    private String turnId;

    @Schema(description = "调度任务主键")
    private String taskId;

    @Schema(description = "任务详情主键")
    private String taskDetailId;

    @Schema(description = "事件类型")
    @NotEmpty(message = "事件类型不能为空")
    private String eventType;

    @Schema(description = "步骤名称")
    private String stepName;

    @Schema(description = "原子命令名称")
    private String commandName;

    @Schema(description = "原子命令请求内容")
    private String commandContent;

    @Schema(description = "原子命令响应内容")
    private String responseContent;

    @Schema(description = "失败原因")
    private String failureReason;

    @Schema(description = "轮次内事件序号")
    @NotNull(message = "事件序号不能为空")
    private Integer sequenceNo;

    @Schema(description = "开始时间")
    @NotNull(message = "开始时间不能为空")
    private Date startedAt;

    @Schema(description = "结束时间")
    private Date finishedAt;

    @Schema(description = "原子命令主键")
    private String atomicCommandId;

    @Schema(description = "原子命令编码")
    private String atomicCommandCode;

    @Schema(description = "运行供应商主键快照")
    private String providerId;

    @Schema(description = "运行供应商名称快照")
    private String providerName;

    @Schema(description = "运行模型主键快照")
    private String modelId;

    @Schema(description = "运行模型编码快照")
    private String modelCode;
}