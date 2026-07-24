package com.simple.ai.common.dto.protocol;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 执行器协议(agent_protocol)新增请求参数。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "执行器协议新增请求参数")
public class CreateProtocolRequest {

    @Schema(description = "协议编码")
    @NotEmpty(message = "协议编码不能为空")
    private String protocolCode;

    @Schema(description = "协议名称")
    @NotEmpty(message = "协议名称不能为空")
    private String protocolName;

    @Schema(description = "协议版本")
    @NotEmpty(message = "协议版本不能为空")
    private String protocolVersion;

    @Schema(description = "协议内容")
    @NotEmpty(message = "协议内容不能为空")
    private String content;

    @Schema(description = "状态")
    private String status;
}