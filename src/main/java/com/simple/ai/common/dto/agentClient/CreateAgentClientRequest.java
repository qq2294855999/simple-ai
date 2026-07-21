package com.simple.ai.common.dto.agentClient;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 客户端实例(agent_client)新增请求参数。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "客户端实例新增请求参数")
public class CreateAgentClientRequest {

    @Schema(description = "执行器类型ID")
    @NotEmpty(message = "执行器类型ID不能为空")
    private String executorId;

    @Schema(description = "客户端名称")
    @NotEmpty(message = "客户端名称不能为空")
    private String clientName;

    @Schema(description = "过期时间数字")
    private Integer expireDuration;

    @Schema(description = "过期时间单位")
    private String expireUnit;

    @Schema(description = "备注")
    private String remark;
}
