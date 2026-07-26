package com.simple.ai.common.dto.agentMemory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 智能体记忆(agent_memory)删除请求参数。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "智能体记忆删除请求参数")
public class DeleteAgentMemoryRequest {

    @Schema(description = "主键")
    @NotEmpty(message = "主键不能为空")
    private String id;
}