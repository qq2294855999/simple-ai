package com.simple.ai.common.dto.agentRule;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(title = "智能体规则(agent_rule)修改请求参数")
public class UpdateAgentRuleRequest extends CreateAgentRuleRequest {

    @Schema(description = "主键")
    @NotEmpty(message = "主键不能为空")
    private String id;

}

