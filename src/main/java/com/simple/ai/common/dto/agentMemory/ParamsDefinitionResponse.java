package com.simple.ai.common.dto.agentMemory;

import com.simple.ai.common.entity.agentMemoryStep.AgentMemoryStep;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 记忆参数定义响应参数。
 * <p>用于前端动态生成执行表单。</p>
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "记忆参数定义响应参数")
public class ParamsDefinitionResponse {

    @Schema(description = "记忆ID")
    private String memoryId;

    @Schema(description = "记忆名称模板")
    private String memoryName;

    @Schema(description = "参数定义JSON，描述每个占位符的类型和含义")
    private String paramsDefinition;

    @Schema(description = "步骤列表")
    private List<AgentMemoryStep> steps;
}