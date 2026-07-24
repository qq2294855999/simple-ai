package com.simple.ai.common.entity.agentExecutor;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 执行器类型(agent_executor)实体类。
 *
 * @author qty
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@TableName(value = "agent_executor", autoResultMap = true)
@Schema(title = "执行器类型(agent_executor)实体类")
public class AgentExecutor {

    /**
     * 主键。
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 执行器编码。
     */
    @TableField(value = "executor_code")
    private String executorCode;

    /**
     * 执行器名称。
     */
    @TableField(value = "executor_name")
    private String executorName;

    /**
     * 执行器描述。
     */
    @TableField(value = "description")
    private String description;

    /**
     * 协议外键。
     */
    @TableField(value = "protocol_id")
    private String protocolId;

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

    /**
     * 备注。
     */
    @TableField(value = "remark")
    private String remark;
}