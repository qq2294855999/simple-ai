package com.simple.ai.common.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.Map;

/**
 * 执行器命令执行结果响应。
 * <p>对应 SEP v1.0 协议 COMMAND_RESULT 消息体。</p>
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "执行器命令执行结果响应")
public class ExecutorCommandResultResponse {

    @Schema(description = "调度ID")
    private String dispatchId;

    @Schema(description = "任务ID")
    private String taskId;

    @Schema(description = "命令ID")
    private String commandId;

    @Schema(description = "步骤序号")
    private Integer sequenceNo;

    @Schema(description = "是否成功")
    private Boolean success;

    @Schema(description = "执行说明")
    private String message;

    @Schema(description = "返回数据")
    private Map<String, Object> data;

    @Schema(description = "错误信息")
    private ExecutorCommandError error;

    @Schema(description = "开始执行时间")
    private Instant startedAt;

    @Schema(description = "执行完成时间")
    private Instant finishedAt;

    /**
     * 执行器命令执行错误详情。
     */
    @Data
    @Accessors(chain = true)
    @Schema(title = "执行器命令执行错误详情")
    public static class ExecutorCommandError {

        @Schema(description = "错误码")
        private String code;

        @Schema(description = "错误详情")
        private String detail;

        @Schema(description = "是否可恢复")
        private Boolean recoverable;
    }
}
