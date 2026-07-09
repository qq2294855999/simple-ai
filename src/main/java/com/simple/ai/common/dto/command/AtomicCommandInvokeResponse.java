package com.simple.ai.common.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 原子命令调用响应参数。
 *
 * @author qty
 */
@Data
@Schema(title = "原子命令调用响应参数")
public class AtomicCommandInvokeResponse {

    /**
     * 是否执行成功
     */
    @Schema(description = "是否执行成功")
    private Boolean success;

    /**
     * 响应内容
     */
    @Schema(description = "响应内容")
    private String responseContent;

    /**
     * 失败原因
     */
    @Schema(description = "失败原因")
    private String failureReason;

}
