package com.simple.ai.common.dto.agentMemoryDetail;

import com.simple.common.mp.common.enums.Status;
import com.simple.common.mp.page.PageBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Agent memory detail aggregate page request.
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "Agent memory detail aggregate page request")
public class PageAggregateAgentMemoryDetailRequest extends PageBase {

    @Schema(description = "Keyword")
    private String keyword;

    @Schema(description = "Agent memory id")
    private String agentMemoryId;

    @Schema(description = "Memory name")
    private String memoryName;

    @Schema(description = "Agent name")
    private String agentName;

    @Schema(description = "Step type")
    private String stepType;

    @Schema(description = "Status")
    private Status status;
}