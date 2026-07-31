package com.simple.ai.common.dto.agentMemory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * AI 获取记忆步骤的工具入参。
 *
 * @author qty
 */
@Data
@Schema(title = "获取记忆步骤工具入参")
public class GetMemoryStepsToolRequest {

    /**
     * 记忆ID，由 queryMemory 返回的 memoryId
     */
    @Schema(description = "记忆ID")
    private String memoryId;
}
