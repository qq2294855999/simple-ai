package com.simple.ai.common.dto.agentMemory;

import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * Agent memory aggregate page response.
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "Agent memory aggregate page response")
public class PageAggregateAgentMemoryResponse {

    @Schema(description = "Primary key")
    private String id;

    @Schema(description = "Agent id")
    private String agentId;

    @Schema(description = "Agent name")
    private String agentName;

    @Schema(description = "Memory name")
    private String memoryName;

    @Schema(description = "Step name")
    private String stepName;

    @Schema(description = "Trigger condition")
    private String triggerCondition;

    @Schema(description = "Trigger action")
    private String triggerAction;

    @Schema(description = "Step count")
    private Long stepCount;

    @Schema(description = "Task count")
    private Long taskCount;

    @Schema(description = "Latest task status")
    private String latestTaskStatus;

    @Schema(description = "Latest task status label")
    private String latestTaskStatusLabel;

    @Schema(description = "Create time")
    private Date createTime;

    @Schema(description = "Update time")
    private Date updateTime;

    @Schema(description = "Status")
    private Status status;

    @Schema(description = "Remark")
    private String remark;
}