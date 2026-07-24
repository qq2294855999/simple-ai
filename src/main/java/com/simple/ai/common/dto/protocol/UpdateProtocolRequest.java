package com.simple.ai.common.dto.protocol;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 执行器协议(agent_protocol)修改请求参数。
 *
 * @author qty
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(title = "执行器协议修改请求参数")
public class UpdateProtocolRequest extends CreateProtocolRequest {

    @Schema(description = "主键")
    @NotEmpty(message = "主键不能为空")
    private String id;
}