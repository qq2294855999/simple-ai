package com.simple.ai.common.dto.agentDefinition;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 智能体定义(agent_definition)删除请求参数。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "智能体定义删除请求参数")
public class DeleteAgentDefinitionRequest {

    @Schema(description = "主键")
    @NotEmpty(message = "主键不能为空")
    private String id;
}