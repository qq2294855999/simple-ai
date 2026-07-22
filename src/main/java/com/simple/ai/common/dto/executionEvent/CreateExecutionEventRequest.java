package com.simple.ai.common.dto.executionEvent;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@Schema(title = "执行事件(execution_event)创建请求参数")
public class CreateExecutionEventRequest {

    @Schema(description = "轮次主键，关联 chat_turn.id")
    @NotEmpty(message = "轮次主键，关联 chat_turn.id不能为空")
    private String turnId;

    @Schema(description = "调度任务主键，关联 task.id")
    @NotEmpty(message = "调度任务主键，关联 task.id不能为空")
    private String taskId;

    @Schema(description = "任务详情主键，关联 task_detail.id")
    @NotEmpty(message = "任务详情主键，关联 task_detail.id不能为空")
    private String taskDetailId;

    @Schema(description = "事件类型: CONTEXT_ASSEMBLING/CONTEXT_ASSEMBLED/MEMORY_MATCHING/MEMORY_MATCHED/MEMORY_MISSED/ATOMIC_COMMAND_START/ATOMIC_COMMAND_COMPLETE/ATOMIC_COMMAND_FAILED/AI_STARTED/AI_COMPLETED/SUB_AGENT_STARTED/SUB_AGENT_COMPLETED/TURN_COMPLETED/TASK_FAILED")
    @NotEmpty(message = "事件类型: CONTEXT_ASSEMBLING/CONTEXT_ASSEMBLED/MEMORY_MATCHING/MEMORY_MATCHED/MEMORY_MISSED/ATOMIC_COMMAND_START/ATOMIC_COMMAND_COMPLETE/ATOMIC_COMMAND_FAILED/AI_STARTED/AI_COMPLETED/SUB_AGENT_STARTED/SUB_AGENT_COMPLETED/TURN_COMPLETED/TASK_FAILED不能为空")
    private String eventType;

    @Schema(description = "步骤名称（展示用）")
    @NotEmpty(message = "步骤名称（展示用）不能为空")
    private String stepName;

    @Schema(description = "原子命令名称")
    @NotEmpty(message = "原子命令名称不能为空")
    private String commandName;

    @Schema(description = "原子命令请求内容（截断500字符）")
    @NotEmpty(message = "原子命令请求内容（截断500字符）不能为空")
    private String commandContent;

    @Schema(description = "原子命令响应内容（截断500字符，完整内容在 task_detail.return_params）")
    @NotEmpty(message = "原子命令响应内容（截断500字符，完整内容在 task_detail.return_params）不能为空")
    private String responseContent;

    @Schema(description = "失败原因")
    @NotEmpty(message = "失败原因不能为空")
    private String failureReason;

    @Schema(description = "轮次内事件序号，从1递增")
    @NotNull(message = "轮次内事件序号，从1递增不能为空")
    private Integer sequenceNo;

    @Schema(description = "开始时间")
    @NotNull(message = "开始时间不能为空")
    private Date startedAt;

    @Schema(description = "结束时间")
    @NotNull(message = "结束时间不能为空")
    private Date finishedAt;

    @Schema(description = "原子命令主键")
    @NotEmpty(message = "原子命令主键不能为空")
    private String atomicCommandId;

    @Schema(description = "原子命令编码")
    @NotEmpty(message = "原子命令编码不能为空")
    private String atomicCommandCode;

    @Schema(description = "运行供应商主键快照")
    @NotEmpty(message = "运行供应商主键快照不能为空")
    private String providerId;

    @Schema(description = "运行供应商名称快照")
    @NotEmpty(message = "运行供应商名称快照不能为空")
    private String providerName;

    @Schema(description = "运行模型主键快照")
    @NotEmpty(message = "运行模型主键快照不能为空")
    private String modelId;

    @Schema(description = "运行模型编码快照")
    @NotEmpty(message = "运行模型编码快照不能为空")
    private String modelCode;
}

