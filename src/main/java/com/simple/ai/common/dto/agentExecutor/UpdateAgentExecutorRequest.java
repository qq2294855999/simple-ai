package com.simple.ai.common.dto.agentExecutor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 执行器类型(agent_executor)修改请求参数。
 *
 * @author qty
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@Schema(title = "执行器类型修改请求参数")
public class UpdateAgentExecutorRequest extends CreateAgentExecutorRequest {

    @Schema(description = "主键")
    @NotEmpty(message = "主键不能为空")
    private String id;
}
