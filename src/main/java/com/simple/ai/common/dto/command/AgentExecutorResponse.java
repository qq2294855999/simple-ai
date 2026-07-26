package com.simple.ai.common.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 业务执行客户端回传的原子命令执行结果。
 *
 * @author qty
 */
@Data
@Schema(title = "业务执行客户端回传结果")
public class AgentExecutorResponse {

    @Schema(description = "任务ID，用于关联等待中的调度流程")
    private String taskId;

    @Schema(description = "是否执行成功")
    private Boolean success;

    @Schema(description = "执行响应内容")
    private String responseContent;

    @Schema(description = "失败原因")
    private String failureReason;
}