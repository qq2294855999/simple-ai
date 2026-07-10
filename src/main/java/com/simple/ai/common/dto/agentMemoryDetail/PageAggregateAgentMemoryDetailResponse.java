package com.simple.ai.common.dto.agentMemoryDetail;

import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * Agent memory detail aggregate page response.
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "Agent memory detail aggregate page response")
public class PageAggregateAgentMemoryDetailResponse {

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

    @Schema(description = "Step name")
    private String stepName;

    @Schema(description = "Step type")
    private String stepType;

    @Schema(description = "Step type label")
    private String stepTypeLabel;

    @Schema(description = "Execution content")
    private String execContent;

    @Schema(description = "Return data format")
    private String returnDataFormat;

    @Schema(description = "Parent step id")
    private String parentStepId;

    @Schema(description = "Parent step name")
    private String parentStepName;

    @Schema(description = "Next step id")
    private String nextStepId;

    @Schema(description = "Next step name")
    private String nextStepName;

    @Schema(description = "Branch condition")
    private String branchCondition;

    @Schema(description = "Branch route")
    private String branchRoute;

    @Schema(description = "Model")
    private String model;

    @Schema(description = "Create time")
    private Date createTime;

    @Schema(description = "Update time")
    private Date updateTime;

    @Schema(description = "Status")
    private Status status;

    @Schema(description = "Remark")
    private String remark;
}