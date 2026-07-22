package com.simple.ai.common.dto.chatTurn;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
@Schema(title = "对话轮次(chat_turn)创建请求参数")
public class CreateChatTurnRequest {

    @Schema(description = "会话主键，关联 agent_chat_session.id")
    @NotEmpty(message = "会话主键，关联 agent_chat_session.id不能为空")
    private String sessionId;

    @Schema(description = "会话内轮次序号，从1递增")
    @NotNull(message = "会话内轮次序号，从1递增不能为空")
    private Integer turnNumber;

    @Schema(description = "该轮用户消息ID，关联 agent_chat_message.id")
    @NotEmpty(message = "该轮用户消息ID，关联 agent_chat_message.id不能为空")
    private String userMessageId;

    @Schema(description = "该轮AI回复消息ID（AI回复完成前可为NULL）")
    private String assistantMessageId;

    @Schema(description = "关联的调度任务ID（冗余便于查询）")
    private String taskId;

    @Schema(description = "受控推理摘要 (JSON格式，不包含模型原始思维链)")
    private String reasoningSummary;

    @Schema(description = "扩展字段，JSON格式")
    private Map<String, Object> reserve;

    @Schema(description = "备注")
    private String remark;
}

