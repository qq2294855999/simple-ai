package com.simple.ai.common.dto.task;

import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * Task aggregate page response.
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "Task aggregate page response")
public class PageAggregateTaskResponse {

    @Schema(description = "Primary key")
    private String id;

    @Schema(description = "Agent memory id")
    private String agentMemoryId;

    @Schema(description = "Memory name")
    private String memoryName;

    @Schema(description = "Agent id")
    private String agentId;

    @Schema(description = "Agent name")
    private String agentName;

    @Schema(description = "Task name")
    private String taskName;

    @Schema(description = "Parent task id")
    private String parentTaskId;

    @Schema(description = "Parent task name")
    private String parentTaskName;

    @Schema(description = "Next task id")
    private String nextTaskId;

    @Schema(description = "Next task name")
    private String nextTaskName;

    @Schema(description = "Step type")
    private String stepType;

    @Schema(description = "Step type label")
    private String stepTypeLabel;

    @Schema(description = "Execution status")
    private String execStatus;

    @Schema(description = "Execution status label")
    private String execStatusLabel;

    @Schema(description = "Failure reason")
    private String failureReason;

    @Schema(description = "Create time")
    private Date createTime;

    @Schema(description = "Update time")
    private Date updateTime;

    @Schema(description = "Status")
    private Status status;

    @Schema(description = "Remark")
    private String remark;
}