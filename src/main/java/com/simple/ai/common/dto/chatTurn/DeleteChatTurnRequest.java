package com.simple.ai.common.dto.chatTurn;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 对话轮次(chat_turn)删除请求参数。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "对话轮次删除请求参数")
public class DeleteChatTurnRequest {

    @Schema(description = "轮次主键")
    @NotEmpty(message = "轮次主键不能为空")
    private String id;
}