package com.simple.ai.common.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Map;

/**
 * 原子命令调用请求参数。
 *
 * @author qty
 */
@Data
@Schema(title = "原子命令调用请求参数")
public class AtomicCommandInvokeRequest {

    /**
     * 任务ID
     */
    @Schema(description = "任务ID")
    @NotEmpty(message = "任务ID不能为空")
    private String taskId;

    /**
     * 任务详情ID
     */
    @Schema(description = "任务详情ID")
    private String taskDetailId;

    /**
     * 原子命令ID
     */
    @Schema(description = "原子命令ID")
    private String atomicCommandId;

    /**
     * 原子命令作用
     */
    @Schema(description = "原子命令作用")
    private String atomicCommandRole;

    /**
     * 命令ID（雪花ID，用于 WebSocket 等待器匹配）
     */
    @Schema(description = "命令ID")
    private String commandId;

    /**
     * 客户端ID（用于点对点 WebSocket 发送）
     */
    @Schema(description = "客户端ID")
    private String clientId;

    /**
     * 命令内容
     */
    @Schema(description = "命令内容")
    @NotEmpty(message = "命令内容不能为空")
    private String commandContent;

    /**
     * 请求参数
     */
    @Schema(description = "请求参数")
    private Map<String, Object> requestParams;

}
