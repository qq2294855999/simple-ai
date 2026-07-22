package com.simple.ai.common.dto.chatTurn;

import com.simple.common.mp.common.enums.Status;
import com.simple.common.mp.page.PageBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(title = "对话轮次(chat_turn)列表请求参数")
public class PageChatTurnRequest extends PageBase {

    @Schema(description = "会话主键，关联 agent_chat_session.id")
    private String sessionId;

    @Schema(description = "会话内轮次序号，从1递增")
    private Integer turnNumber;

    @Schema(description = "该轮用户消息ID，关联 agent_chat_message.id")
    private String userMessageId;

    @Schema(description = "该轮AI回复消息ID，关联 agent_chat_message.id（AI回复完成前为NULL）")
    private String assistantMessageId;

    @Schema(description = "关联的调度任务ID（冗余便于查询）")
    private String taskId;

    @Schema(description = "受控推理摘要 (JSON格式，不包含模型原始思维链)")

    private String reasoningSummary;

    @Schema(description = "状态: ON/DISABLE")
    private Status status;

    @Schema(description = "备注")
    private String remark;
}

