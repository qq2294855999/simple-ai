package com.simple.ai.common.dto.agentMemory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * AI 查询匹配记忆的工具入参。
 *
 * @author qty
 */
@Data
@Schema(title = "查询记忆工具入参")
public class QueryMemoryToolRequest {

    /**
     * 搜索关键词，用于模糊匹配记忆名称
     */
    @Schema(description = "搜索关键词")
    private String keyword;
}
