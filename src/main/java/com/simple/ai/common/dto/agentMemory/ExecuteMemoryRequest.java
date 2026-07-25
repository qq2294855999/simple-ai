package com.simple.ai.common.dto.agentMemory;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * 执行记忆请求参数。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "执行记忆请求参数")
public class ExecuteMemoryRequest {

    @Schema(description = "执行参数，key对应paramsDefinition中的参数名")
    private Map<String, Object> params;

    @Schema(description = "客户端ID，不传则使用记忆绑定的客户端")
    private String clientId;

    @Schema(description = "会话ID，用于关联进度事件和会话摘要")
    private String sessionId;

    @Schema(description = "用户ID，不传则使用记忆绑定的用户")
    private String userId;
}