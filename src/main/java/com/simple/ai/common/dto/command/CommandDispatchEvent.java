package com.simple.ai.common.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 智能体命令调度事件参数。
 *
 * @author qty
 */
@Data
@Schema(title = "智能体命令调度事件参数")
public class CommandDispatchEvent {

    /**
     * 任务ID
     */
    @Schema(description = "任务ID")
    private String taskId;

    /**
     * 会话ID
     */
    @Schema(description = "会话ID")
    private String sessionId;

    /**
     * 事件类型
     */
    @Schema(description = "事件类型")
    private String eventType;

    /**
     * 执行状态
     */
    @Schema(description = "执行状态")
    private String execStatus;

    /**
     * 事件内容
     */
    @Schema(description = "事件内容")
    private String eventContent;

    /**
     * 失败原因
     */
    @Schema(description = "失败原因")
    private String failureReason;

}
