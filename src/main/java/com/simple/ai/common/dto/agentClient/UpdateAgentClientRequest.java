package com.simple.ai.common.dto.agentClient;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 客户端实例(agent_client)修改请求参数。
 *
 * @author qty
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(title = "客户端实例修改请求参数")
public class UpdateAgentClientRequest extends CreateAgentClientRequest {

    @Schema(description = "主键")
    @NotEmpty(message = "主键不能为空")
    private String id;

    @Schema(description = "客户端状态")
    private String status;
}
