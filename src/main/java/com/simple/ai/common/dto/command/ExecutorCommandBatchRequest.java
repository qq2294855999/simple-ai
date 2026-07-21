package com.simple.ai.common.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 执行器命令批量下发请求。
 * <p>对应 SEP v1.0 协议 COMMAND_BATCH 消息体。</p>
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "执行器命令批量下发请求")
public class ExecutorCommandBatchRequest {

    @Schema(description = "调度ID")
    private String dispatchId;

    @Schema(description = "任务ID")
    private String taskId;

    @Schema(description = "客户端ID")
    private String clientId;

    @Schema(description = "是否失败即停止")
    private Boolean stopOnFailure;

    @Schema(description = "执行前最小延迟(毫秒)")
    private Integer minDelayMs;

    @Schema(description = "执行前最大延迟(毫秒)")
    private Integer maxDelayMs;

    @Schema(description = "命令列表")
    private List<ExecutorCommandItem> commands;
}
