package com.simple.ai.common.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * 执行器单条命令项。
 * <p>对应 SEP v1.0 协议 COMMAND_BATCH.payload.commands[] 中的单条命令。</p>
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "执行器单条命令项")
public class ExecutorCommandItem {

    @Schema(description = "命令ID（雪花ID）")
    private String commandId;

    @Schema(description = "步骤序号")
    private Integer sequenceNo;

    @Schema(description = "原子命令编码")
    private String atomicCommandCode;

    @Schema(description = "命令参数")
    private Map<String, Object> args;

    @Schema(description = "命令超时时间(毫秒)")
    private Integer timeoutMs;

    @Schema(description = "幂等键")
    private String idempotencyKey;
}
