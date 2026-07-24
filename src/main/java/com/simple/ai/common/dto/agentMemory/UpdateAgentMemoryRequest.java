package com.simple.ai.common.dto.agentMemory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 智能体记忆修改请求参数
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "智能体记忆(agent_memory)修改请求参数")
public class UpdateAgentMemoryRequest extends CreateAgentMemoryRequest {

    @Schema(description = "主键")
    @NotEmpty(message = "主键不能为空")
    private String id;
}