package com.simple.ai.common.dto.command;

import com.simple.ai.common.enums.AgentChatMessageFormatProcess;
import com.simple.ai.common.enums.AgentExecutionStatusProcess;
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
    private AgentExecutionStatusProcess execStatus;

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

    /** 运行供应商主键快照 */
    private String providerId;

    /** 运行供应商名称快照 */
    private String providerName;

    /** 运行模型主键快照 */
    private String modelId;

    /** 运行模型编码快照 */
    private String modelCode;

    /**
     * AI 思考推理过程完整文本（当前 SDK 返回为空，后续 Spring AI reasoning 升级后自动回填）
     */
    private String thinkingContent;

    /**
     * 思考内容格式（PLAIN_TEXT / RESTRICTED_MARKDOWN）
     */
    private AgentChatMessageFormatProcess thinkingContentFormat;

}