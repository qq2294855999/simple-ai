package com.simple.ai.common.dto.agentChat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 对话轮次状态响应，用于断线重连时查询轮次是否已完成。
 *
 * @author qty
 */
@Data
@Schema(title = "对话轮次状态响应")
public class AgentChatTurnStatusResponse {

    /**
     * 轮次主键
     */
    @Schema(description = "轮次主键")
    private String turnId;

    /**
     * 会话主键
     */
    @Schema(description = "会话主键")
    private String sessionId;

    /**
     * 轮次序号
     */
    @Schema(description = "会话内轮次序号，从1递增")
    private Integer turnNumber;

    /**
     * 轮次状态：IN_PROGRESS / COMPLETED
     */
    @Schema(description = "轮次状态：IN_PROGRESS 进行中 / COMPLETED 已完成")
    private String turnStatus;

    /**
     * AI回复消息主键（完成时有值）
     */
    @Schema(description = "AI回复消息主键")
    private String assistantMessageId;

    /**
     * 调度任务主键
     */
    @Schema(description = "关联的调度任务主键")
    private String taskId;
}
