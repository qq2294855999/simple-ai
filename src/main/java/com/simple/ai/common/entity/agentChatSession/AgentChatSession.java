package com.simple.ai.common.entity.agentChatSession;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 智能体聊天会话实体。
 *
 * @author qty
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(chain = true)
@TableName(value = "agent_chat_session", autoResultMap = true)
@Schema(title = "智能体聊天会话实体")
public class AgentChatSession {

    /** 会话主键 */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /** 绑定的智能体主键 */
    @TableField(value = "agent_id")
    private String agentId;

    /**
     * 用户归属ID，确保会话归属到具体用户
     */
    @TableField(value = "user_id")
    private String userId;

    /** 会话名称 */
    @TableField(value = "session_name")
    private String sessionName;

    /** 最后消息时间 */
    @TableField(value = "last_message_at")
    private Date lastMessageAt;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /** 修改时间 */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 创建者用户ID，用于归属校验
     */
    @TableField(value = "create_user_id")
    private String createUserId;

    /**
     * 模型主键，会话级默认模型
     */
    @TableField(value = "model_id")
    private String modelId;

    /**
     * 客户端主键，会话级默认执行客户端
     */
    @TableField(value = "client_id")
    private String clientId;

    /** 状态 */
    @TableField(value = "status")
    private Status status;

    /**
     * 扩展字段，JSON格式
     */
    @TableField(value = "reserve")
    private String reserve;

    /** 备注 */
    @TableField(value = "remark")
    private String remark;
}