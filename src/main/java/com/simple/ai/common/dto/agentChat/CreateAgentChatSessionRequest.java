package com.simple.ai.common.dto.agentChat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 创建智能体聊天会话请求。
 *
 * @author qty
 */
@Data
@Schema(title = "创建智能体聊天会话请求")
public class CreateAgentChatSessionRequest {

    /** 智能体主键 */
    @NotEmpty(message = "智能体主键不能为空")
    @Schema(description = "智能体主键")
    private String agentId;
}
