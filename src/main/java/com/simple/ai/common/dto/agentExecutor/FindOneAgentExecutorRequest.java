package com.simple.ai.common.dto.agentExecutor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 执行器类型(agent_executor)单条数据请求参数。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "执行器类型单条数据请求参数")
public class FindOneAgentExecutorRequest {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "执行器编码")
    private String executorCode;

    @Schema(description = "执行器名称")
    private String executorName;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "备注")
    private String remark;
}
