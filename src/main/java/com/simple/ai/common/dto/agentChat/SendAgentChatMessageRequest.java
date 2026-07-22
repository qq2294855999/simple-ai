package com.simple.ai.common.dto.agentChat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 发送智能体聊天消息请求。
 *
 * @author qty
 */
@Data
@Schema(title = "发送智能体聊天消息请求")
public class SendAgentChatMessageRequest {

    /** 会话主键 */
    @NotEmpty(message = "会话主键不能为空")
    @Schema(description = "会话主键")
    private String sessionId;

    /** 用户消息 */
    @NotEmpty(message = "用户消息不能为空")
    @Schema(description = "用户消息")
    private String content;

    /** 显式模型主键 */
    @Schema(description = "显式模型主键")
    private String modelId;

    /**
     * 客户端ID（指定执行目标客户端）
     */
    @Schema(description = "客户端ID")
    private String clientId;

    /**
     * 记忆操作标志（create/revise，空表示不操作记忆）
     */
    @Schema(description = "记忆操作标志")
    private String memoryAction;

    /**
     * 幂等键，用于防止断线重连后产生重复消息。
     * 前端每次发送消息时生成唯一值，后端通过 Redis SETNX 去重。
     */
    @Schema(description = "幂等键，防止重复消息")
    private String idempotencyKey;
}
