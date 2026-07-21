package com.simple.ai.common.dto.agentExecutor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 执行器类型(agent_executor)删除请求参数。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "执行器类型删除请求参数")
public class DeleteAgentExecutorRequest {

    @Schema(description = "主键")
    private String id;
}
