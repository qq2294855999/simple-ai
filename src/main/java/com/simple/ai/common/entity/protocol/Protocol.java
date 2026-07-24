package com.simple.ai.common.entity.protocol;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 执行器协议(agent_protocol)实体类。
 *
 * @author qty
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@TableName(value = "agent_protocol", autoResultMap = true)
@Schema(title = "执行器协议(agent_protocol)实体类")
public class Protocol {

    /**
     * 主键。
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 协议编码。
     */
    @TableField(value = "protocol_code")
    private String protocolCode;

    /**
     * 协议名称。
     */
    @TableField(value = "protocol_name")
    private String protocolName;

    /**
     * 协议版本。
     */
    @TableField(value = "protocol_version")
    private String protocolVersion;

    /**
     * 协议内容。
     */
    @TableField(value = "content")
    private String content;

    /**
     * 状态。
     */
    @TableField(value = "status")
    private Status status;

    /**
     * 创建人用户ID。
     */
    @TableField(value = "create_user_id")
    private String createUserId;

    /**
     * 创建人用户名称。
     */
    @TableField(value = "create_user_name")
    private String createUserName;

    /**
     * 创建时间。
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 修改人用户ID。
     */
    @TableField(value = "update_user_id")
    private String updateUserId;

    /**
     * 修改人用户名称。
     */
    @TableField(value = "update_user_name")
    private String updateUserName;

    /**
     * 修改时间。
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 扩展字段。
     */
    @TableField(value = "reserve")
    private String reserve;
}