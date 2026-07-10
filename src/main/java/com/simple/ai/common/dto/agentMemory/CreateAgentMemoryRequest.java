package com.simple.ai.common.dto.agentMemory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Agent memory create request.
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "Agent memory create request")
public class CreateAgentMemoryRequest {

    @Schema(description = "Agent id")
    @NotEmpty(message = "Agent id cannot be empty")
    private String agentId;

    @Schema(description = "Memory name")
    @NotEmpty(message = "Memory name cannot be empty")
    private String memoryName;

    @Schema(description = "Step name")
    @NotEmpty(message = "Step name cannot be empty")
    private String stepName;

    @Schema(description = "Trigger condition")
    @NotEmpty(message = "Trigger condition cannot be empty")
    private String triggerCondition;

    @Schema(description = "Trigger action")
    @NotEmpty(message = "Trigger action cannot be empty")
    private String triggerAction;

    @Schema(description = "Remark")
    private String remark;
}
