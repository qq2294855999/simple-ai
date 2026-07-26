package com.simple.ai.common.dto.agentRule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 智能体规则(agent_rule)删除请求参数。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "智能体规则删除请求参数")
public class DeleteAgentRuleRequest {

    @Schema(description = "主键")
    @NotEmpty(message = "主键不能为空")
    private String id;
}