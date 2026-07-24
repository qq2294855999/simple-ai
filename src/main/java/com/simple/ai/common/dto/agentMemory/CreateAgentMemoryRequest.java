package com.simple.ai.common.dto.agentMemory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 智能体记忆创建请求参数
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "智能体记忆(agent_memory)创建请求参数")
public class CreateAgentMemoryRequest {

    @Schema(description = "智能体ID")
    @NotEmpty(message = "智能体ID不能为空")
    private String agentId;

    @Schema(description = "记忆名称模板，支持{param}占位符")
    @NotEmpty(message = "记忆名称不能为空")
    private String memoryName;

    @Schema(description = "参数定义JSON，描述每个占位符的类型和含义")
    private String paramsDefinition;

    @Schema(description = "记忆摘要")
    private String summary;

    @Schema(description = "备注")
    private String remark;
}