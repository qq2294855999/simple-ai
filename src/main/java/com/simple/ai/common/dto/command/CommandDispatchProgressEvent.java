package com.simple.ai.common.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 智能体命令调度进度事件。
 *
 * @author qty
 */
@Data
@Schema(title = "智能体命令调度进度事件")
public class CommandDispatchProgressEvent {

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
     * 步骤名称
     */
    @Schema(description = "步骤名称")
    private String stepName;

    /**
     * 执行状态
     */
    @Schema(description = "执行状态")
    private String execStatus;

    /**
     * 事件消息
     */
    @Schema(description = "事件消息")
    private String message;

    /**
     * 事件数据
     */
    @Schema(description = "事件数据")
    private String payload;

    /**
     * 是否完成
     */
    @Schema(description = "是否完成")
    private Boolean completed;

    /**
     * 失败原因
     */
    @Schema(description = "失败原因")
    private String failureReason;
}
