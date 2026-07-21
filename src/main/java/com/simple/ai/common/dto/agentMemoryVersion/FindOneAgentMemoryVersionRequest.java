package com.simple.ai.common.dto.agentMemoryVersion;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 记忆版本(agent_memory_version)单条查询请求参数。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "记忆版本单条查询请求参数")
public class FindOneAgentMemoryVersionRequest {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "记忆ID")
    private String memoryId;

    @Schema(description = "版本号")
    private Integer versionNo;

    @Schema(description = "版本状态")
    private String versionStatus;
}
