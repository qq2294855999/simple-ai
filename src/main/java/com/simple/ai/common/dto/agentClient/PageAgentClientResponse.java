package com.simple.ai.common.dto.agentClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 客户端实例(agent_client)分页明细响应。
 *
 * @author qty
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(chain = true)
@Schema(title = "客户端实例分页明细响应")
public class PageAgentClientResponse {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "用户归属ID")
    private String userId;

    @Schema(description = "执行器类型ID")
    private String executorId;

    @Schema(description = "客户端名称")
    private String clientName;

    @Schema(description = "客户端状态")
    private String status;

    @Schema(description = "过期时间")
    private Date expireTime;

    @Schema(description = "最后成功连接时间")
    private Date lastConnectedAt;

    @Schema(description = "最后断开连接时间")
    private Date lastDisconnectedAt;

    @Schema(description = "最近一次握手失败原因")
    private String lastHandshakeError;

    @Schema(description = "执行器软件版本号")
    private String agentVersion;

    @Schema(description = "机器名称")
    private String machineName;

    @Schema(description = "创建人用户ID")
    private String createUserId;

    @Schema(description = "创建人用户名称")
    private String createUserName;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "修改人用户ID")
    private String updateUserId;

    @Schema(description = "修改人用户名称")
    private String updateUserName;

    @Schema(description = "修改时间")
    private Date updateTime;

    @Schema(description = "扩展字段")
    private String reserve;

    @Schema(description = "备注")
    private String remark;
}
