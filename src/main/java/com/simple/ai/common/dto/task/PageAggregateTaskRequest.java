package com.simple.ai.common.dto.task;

import com.simple.common.mp.page.PageBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Task aggregate page request.
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "Task aggregate page request")
public class PageAggregateTaskRequest extends PageBase {

    @Schema(description = "Keyword")
    private String keyword;

    @Schema(description = "Agent id")
    private String agentId;

    @Schema(description = "Agent name")
    private String agentName;

    @Schema(description = "Memory id")
    private String memoryId;

    @Schema(description = "Memory name")
    private String memoryName;

    @Schema(description = "Step type")
    private String stepType;

    @Schema(description = "Execution status")
    private String execStatus;
}