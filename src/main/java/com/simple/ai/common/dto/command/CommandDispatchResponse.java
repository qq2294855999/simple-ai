package com.simple.ai.common.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 智能体命令调度响应参数。
 *
 * @author qty
 */
@Data
@Schema(title = "智能体命令调度响应参数")
public class CommandDispatchResponse {

    /**
     * 任务ID
     */
    @Schema(description = "任务ID")
    private String taskId;

    /**
     * 执行状态
     */
    @Schema(description = "执行状态")
    private String execStatus;

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
