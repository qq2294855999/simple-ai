package com.simple.ai.common.dto.agentMemoryVersionDetail;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 记忆版本步骤(agent_memory_version_detail)列表查询请求参数。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "记忆版本步骤列表查询请求参数")
public class FindAllAgentMemoryVersionDetailRequest {

    @Schema(description = "记忆版本ID")
    private String versionId;
}
