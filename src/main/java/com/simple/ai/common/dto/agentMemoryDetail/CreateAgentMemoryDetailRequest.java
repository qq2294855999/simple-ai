package com.simple.ai.common.dto.agentMemoryDetail;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Agent memory detail create request.
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "Agent memory detail create request")
public class CreateAgentMemoryDetailRequest {

    @Schema(description = "Agent memory id")
    @NotEmpty(message = "Agent memory id cannot be empty")
    private String agentMemoryId;

    @Schema(description = "Step name")
    @NotEmpty(message = "Step name cannot be empty")
    private String stepName;

    @Schema(description = "Step type")
    @NotEmpty(message = "Step type cannot be empty")
    private String stepType;

    @Schema(description = "Execution content")
    @NotEmpty(message = "Execution content cannot be empty")
    private String execContent;

    @Schema(description = "Return data format")
    @NotEmpty(message = "Return data format cannot be empty")
    private String returnDataFormat;

    @Schema(description = "Parent step id")
    private String parentStepId;

    @Schema(description = "Next step id")
    private String nextStepId;

    @Schema(description = "Branch condition")
    private String branchCondition;

    @Schema(description = "Branch route")
    private String branchRoute;

    @Schema(description = "Model")
    private String model;

    @Schema(description = "Remark")
    private String remark;
}