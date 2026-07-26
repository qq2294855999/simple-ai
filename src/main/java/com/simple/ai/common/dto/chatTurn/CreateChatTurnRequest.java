package com.simple.ai.common.dto.chatTurn;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * 对话轮次(chat_turn)创建请求参数。
 * <p>系统内部创建，非前端表单提交。</p>
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "对话轮次创建请求参数")
public class CreateChatTurnRequest {

    @Schema(description = "会话主键")
    @NotEmpty(message = "会话主键不能为空")
    private String sessionId;

    @Schema(description = "会话内轮次序号")
    @NotNull(message = "轮次序号不能为空")
    private Integer turnNumber;

    @Schema(description = "该轮用户消息ID")
    @NotEmpty(message = "用户消息ID不能为空")
    private String userMessageId;

    @Schema(description = "该轮AI回复消息ID")
    private String assistantMessageId;

    @Schema(description = "关联的调度任务ID")
    private String taskId;

    @Schema(description = "受控推理摘要")
    private String reasoningSummary;

    @Schema(description = "扩展字段")
    private Map<String, Object> reserve;

    @Schema(description = "备注")
    private String remark;
}