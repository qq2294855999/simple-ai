package com.simple.ai.common.entity.agentChatMessage;

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
 * 智能体聊天消息实体。
 *
 * @author qty
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(chain = true)
@TableName(value = "agent_chat_message", autoResultMap = true)
@Schema(title = "智能体聊天消息实体")
public class AgentChatMessage {

    /** 消息主键 */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /** 会话主键 */
    @TableField(value = "session_id")
    private String sessionId;

    /** 调度任务主键 */
    @TableField(value = "task_id")
    private String taskId;

    /** 消息角色 */
    @TableField(value = "role")
    private String role;

    /** 消息内容 */
    @TableField(value = "content")
    private String content;

    /** 内容格式 */
    @TableField(value = "content_format")
    private String contentFormat;

    /** 会话内序号 */
    @TableField(value = "sequence_no")
    private Long sequenceNo;

    /** 运行供应商主键快照 */
    @TableField(value = "provider_id")
    private String providerId;

    /** 运行供应商名称快照 */
    @TableField(value = "provider_name")
    private String providerName;

    /** 运行模型主键快照 */
    @TableField(value = "model_id")
    private String modelId;

    /** 运行模型编码快照 */
    @TableField(value = "model_code")
    private String modelCode;

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
