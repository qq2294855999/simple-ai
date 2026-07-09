package com.simple.ai.common.dto.agentMemory;

import java.util.Date;

import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
@Schema(title = "智能体记忆(agent_memory)创建请求参数")
public class CreateAgentMemoryRequest {

    @Schema(description = "智能体ID")
    @NotEmpty(message = "智能体ID不能为空")
    private String agentId;

    @Schema(description = "记忆名称")
    @NotEmpty(message = "记忆名称不能为空")
    private String memoryName;

    @Schema(description = "触发条件")
    @NotEmpty(message = "触发条件不能为空")
    private String triggerCondition;
}

