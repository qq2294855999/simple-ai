package com.simple.ai.common.dto.agentMemory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * AI 覆盖修订记忆的工具入参。
 * <p>重新探索成功后调用，传入修订后的命令序列和参数定义，
 * 蒸馏器内部做删旧步骤 + 插新步骤 + 覆盖记忆的原子操作。</p>
 *
 * @author qty
 */
@Data
@Schema(title = "修订记忆工具入参")
public class ReviseMemoryToolRequest {

    /**
     * 被修订的记忆ID
     */
    @Schema(description = "被修订的记忆ID")
    private String memoryId;

    /**
     * 新命令序列，重新探索产出的完整步骤
     */
    @Schema(description = "新命令序列")
    private List<CommandStep> steps;

    /**
     * 参数定义JSON，描述占位符的类型和含义
     */
    @Schema(description = "参数定义JSON")
    private String paramsDefinition;

    /**
     * 记忆名称提示，可选，为空则保留原名
     */
    @Schema(description = "记忆名称提示")
    private String memoryNameHint;
}
