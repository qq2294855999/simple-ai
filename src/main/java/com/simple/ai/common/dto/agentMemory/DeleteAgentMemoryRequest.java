package com.simple.ai.common.dto.agentMemory;

import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 智能体记忆(agent_memory)删除请求参数
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "智能体记忆(agent_memory)删除请求参数")
public class DeleteAgentMemoryRequest {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "智能体ID")
    private String agentId;

    @Schema(description = "状态")
    private Status status;
}