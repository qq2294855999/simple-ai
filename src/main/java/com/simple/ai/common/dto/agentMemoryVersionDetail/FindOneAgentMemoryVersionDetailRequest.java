package com.simple.ai.common.dto.agentMemoryVersionDetail;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 记忆版本步骤(agent_memory_version_detail)单条查询请求参数。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "记忆版本步骤单条查询请求参数")
public class FindOneAgentMemoryVersionDetailRequest {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "记忆版本ID")
    private String versionId;
}
