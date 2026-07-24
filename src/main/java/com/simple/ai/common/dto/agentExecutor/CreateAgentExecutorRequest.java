package com.simple.ai.common.dto.agentExecutor;

import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 执行器类型(agent_executor)新增请求参数。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "执行器类型新增请求参数")
public class CreateAgentExecutorRequest {

    @Schema(description = "执行器编码")
    @NotEmpty(message = "执行器编码不能为空")
    private String executorCode;

    @Schema(description = "执行器名称")
    @NotEmpty(message = "执行器名称不能为空")
    private String executorName;

    @Schema(description = "执行器描述")
    private String description;

    @Schema(description = "协议外键")
    private String protocolId;

    @Schema(description = "状态")
    private Status status;

    @Schema(description = "备注")
    private String remark;
}