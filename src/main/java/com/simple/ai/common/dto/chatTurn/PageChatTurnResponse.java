package com.simple.ai.common.dto.chatTurn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(chain = true)
@Schema(title = "对话轮次(chat_turn)分页明细响应")
public class PageChatTurnResponse {

    @Schema(description = "轮次主键，UUID")
    private String id;

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

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "修改时间")
    private Date updateTime;

    @Schema(description = "状态: ON/DISABLE")
    private Status status;

    @Schema(description = "扩展字段，JSON格式")
    private Map<String, Object> reserve;

    @Schema(description = "备注")
    private String remark;
}

