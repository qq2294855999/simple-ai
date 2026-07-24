package com.simple.ai.common.dto.protocol;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 执行器协议(agent_protocol)分页明细响应。
 *
 * @author qty
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(chain = true)
@Schema(title = "执行器协议分页明细响应")
public class PageProtocolResponse {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "协议编码")
    private String protocolCode;

    @Schema(description = "协议名称")
    private String protocolName;

    @Schema(description = "协议版本")
    private String protocolVersion;

    @Schema(description = "状态")
    private Status status;

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
}