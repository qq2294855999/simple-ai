package com.simple.ai.common.dto.executionEvent;

import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@Schema(title = "执行事件(execution_event)列表请求参数")
public class FindAllExecutionEventRequest {

    @Schema(description = "事件主键，UUID")
    private String id;

    @Schema(description = "轮次主键，关联 chat_turn.id")
    private String turnId;

    @Schema(description = "调度任务主键，关联 task.id")
    private String taskId;

    @Schema(description = "任务详情主键，关联 task_detail.id")
    private String taskDetailId;

    @Schema(description = "事件类型: CONTEXT_ASSEMBLING/CONTEXT_ASSEMBLED/MEMORY_MATCHING/MEMORY_MATCHED/MEMORY_MISSED/ATOMIC_COMMAND_START/ATOMIC_COMMAND_COMPLETE/ATOMIC_COMMAND_FAILED/AI_STARTED/AI_COMPLETED/SUB_AGENT_STARTED/SUB_AGENT_COMPLETED/TURN_COMPLETED/TASK_FAILED")
    private String eventType;

    @Schema(description = "步骤名称（展示用）")
    private String stepName;

    @Schema(description = "原子命令名称")
    private String commandName;

    @Schema(description = "原子命令请求内容（截断500字符）")
    private String commandContent;

    @Schema(description = "原子命令响应内容（截断500字符，完整内容在 task_detail.return_params）")
    private String responseContent;

    @Schema(description = "失败原因")
    private String failureReason;

    @Schema(description = "轮次内事件序号，从1递增")
    private Integer sequenceNo;

    @Schema(description = "开始时间")
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

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "状态: ON/DISABLE")
    private Status status;

}

