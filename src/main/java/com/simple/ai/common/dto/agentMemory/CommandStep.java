package com.simple.ai.common.dto.agentMemory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 修订记忆的命令步骤入参。
 * <p>承载重新探索产出的单条命令序列，由 AI reviseMemory 工具传入。</p>
 *
 * @author qty
 */
@Data
@Schema(title = "修订记忆命令步骤")
public class CommandStep {

    /**
     * 原子命令编码
     */
    @Schema(description = "原子命令编码")
    private String atomicCommandCode;

    /**
     * 步骤名称
     */
    @Schema(description = "步骤名称")
    private String stepName;

    /**
     * 参数模板JSON（含 {占位符}）
     */
    @Schema(description = "参数模板JSON")
    private String argsTemplate;

    /**
     * 超时时间(ms)
     */
    @Schema(description = "超时时间(ms)")
    private Integer timeoutMs;

    /**
     * 失败策略
     */
    @Schema(description = "失败策略")
    private String failureStrategy;
}
