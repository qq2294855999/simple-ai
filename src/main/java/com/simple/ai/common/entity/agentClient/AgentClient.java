package com.simple.ai.common.entity.agentClient;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.simple.ai.common.enums.AgentClientStatusProcess;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 客户端实例(agent_client)实体类。
 *
 * @author qty
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@TableName(value = "agent_client", autoResultMap = true)
@Schema(title = "客户端实例(agent_client)实体类")
public class AgentClient {

    /**
     * 主键，也是WebSocket客户端标识。
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 用户归属ID。
     */
    @TableField(value = "user_id")
    private String userId;

    /**
     * 执行器类型ID。
     */
    @TableField(value = "executor_id")
    private String executorId;

    /**
     * 客户端名称。
     */
    @TableField(value = "client_name")
    private String clientName;

    /**
     * 客户端密钥哈希值。
     */
    @TableField(value = "client_secret_hash")
    private String clientSecretHash;

    /**
     * 客户端状态。
     */
    @TableField(value = "status")
    private AgentClientStatusProcess status;

    /**
     * 过期时间。
     */
    @TableField(value = "expire_time")
    private Date expireTime;

    /**
     * 最后成功连接时间。
     */
    @TableField(value = "last_connected_at")
    private Date lastConnectedAt;

    /**
     * 最后断开连接时间。
     */
    @TableField(value = "last_disconnected_at")
    private Date lastDisconnectedAt;

    /**
     * 最近一次握手失败原因。
     */
    @TableField(value = "last_handshake_error")
    private String lastHandshakeError;

    /**
     * 执行器软件版本号。
     */
    @TableField(value = "agent_version")
    private String agentVersion;

    /**
     * 机器名称。
     */
    @TableField(value = "machine_name")
    private String machineName;

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
