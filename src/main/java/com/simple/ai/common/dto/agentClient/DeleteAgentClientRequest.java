package com.simple.ai.common.dto.agentClient;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 客户端实例(agent_client)删除请求参数。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "客户端实例删除请求参数")
public class DeleteAgentClientRequest {

    @Schema(description = "主键")
    private String id;
}
