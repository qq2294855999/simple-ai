package com.simple.ai.common.dto.agentClient;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 客户端实例(agent_client)列表请求参数。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "客户端实例列表请求参数")
public class FindAllAgentClientRequest {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "用户归属ID")
    private String userId;

    @Schema(description = "执行器类型ID")
    private String executorId;

    @Schema(description = "客户端名称")
    private String clientName;

    @Schema(description = "客户端状态")
    private String status;

    @Schema(description = "机器名称")
    private String machineName;

    @Schema(description = "备注")
    private String remark;
}
