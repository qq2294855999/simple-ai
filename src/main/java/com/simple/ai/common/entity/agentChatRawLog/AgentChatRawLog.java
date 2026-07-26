package com.simple.ai.common.entity.agentChatRawLog;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * AI 原始消息日志实体。
 * <p>记录每次 AI 调用时发送给大模型的原始请求和返回的原始响应，用于调试和审计追溯。</p>
 *
 * @author qty
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(chain = true)
@TableName(value = "agent_chat_raw_log", autoResultMap = true)
@Schema(title = "AI 原始消息日志实体")
public class AgentChatRawLog {

    /**
     * 原始日志主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 会话主键
     */
    @TableField(value = "session_id")
    private String sessionId;

    /**
     * 对话轮次主键
     */
    @TableField(value = "turn_id")
    private String turnId;

    /**
     * 调度任务主键
     */
    @TableField(value = "task_id")
    private String taskId;

    /**
     * 关联 agent_chat_message.id
     */
    @TableField(value = "message_id")
    private String messageId;

    /**
     * 方向：REQUEST（发送给AI的原始请求）/ RESPONSE（AI返回的原始响应）
     */
    @TableField(value = "direction")
    private String direction;

    /**
     * 原始 JSON 内容
     */
    @TableField(value = "raw_content")
    private String rawContent;

    /**
     * 模型编码快照
     */
    @TableField(value = "model_code")
    private String modelCode;

    /**
     * 供应商主键快照
     */
    @TableField(value = "provider_id")
    private String providerId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 状态：ON(1) / OFF(0)
     */
    @TableField(value = "status")
    private Status status;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;
}