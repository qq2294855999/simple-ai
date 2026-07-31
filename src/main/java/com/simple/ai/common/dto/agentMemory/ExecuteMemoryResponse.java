package com.simple.ai.common.dto.agentMemory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 执行记忆响应参数。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "执行记忆响应参数")
public class ExecuteMemoryResponse {

    @Schema(description = "任务ID")
    private String taskId;

    @Schema(description = "执行状态")
    private String execStatus;

    @Schema(description = "记忆ID")
    private String memoryId;
}