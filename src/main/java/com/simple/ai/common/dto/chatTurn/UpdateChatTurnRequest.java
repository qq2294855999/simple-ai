package com.simple.ai.common.dto.chatTurn;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(title = "对话轮次(chat_turn)修改请求参数")
public class UpdateChatTurnRequest extends CreateChatTurnRequest {

    @Schema(description = "轮次主键，UUID")
    @NotEmpty(message = "轮次主键，UUID不能为空")
    private String id;

}

