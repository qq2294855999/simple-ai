package com.simple.ai.common.dto.agentMemory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * AI 蒸馏记忆的工具入参。
 * <p>AI 探索成功后调用，传入来源任务ID，
 * 由蒸馏器自动从 task 执行轨迹提炼记忆和步骤序列。</p>
 *
 * @author qty
 */
@Data
@Schema(title = "蒸馏记忆工具入参")
public class CreateMemoryToolRequest {

    /**
     * 来源任务ID，AI 探索成功后产生的 task 主键
     */
    @Schema(description = "来源任务ID")
    private String taskId;

    /**
     * 记忆名称提示，可选，用于辅助命名蒸馏产物
     */
    @Schema(description = "记忆名称提示")
    private String memoryNameHint;
}
