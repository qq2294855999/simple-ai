package com.simple.ai.common.dto.protocol;

import com.simple.common.mp.page.PageBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 执行器协议(agent_protocol)分页请求参数。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "执行器协议分页请求参数")
public class PageProtocolRequest extends PageBase {

    @Schema(description = "协议编码")
    private String protocolCode;

    @Schema(description = "协议名称")
    private String protocolName;

    @Schema(description = "协议版本")
    private String protocolVersion;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "备注")
    private String remark;
}