package com.simple.ai.common.entity.agentChatSession;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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

    /** 状态 */
    @TableField(value = "status")
    private Status status;

    /** 扩展 */
    @TableField(value = "reserver")
    private String reserver;

    /** 备注 */
    @TableField(value = "remark")
    private String remark;
}
