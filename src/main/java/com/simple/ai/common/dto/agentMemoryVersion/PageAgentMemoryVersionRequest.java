package com.simple.ai.common.dto.agentMemoryVersion;

import com.simple.common.mp.page.PageBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 记忆版本(agent_memory_version)分页请求参数。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "记忆版本分页请求参数")
public class PageAgentMemoryVersionRequest extends PageBase {

    @Schema(description = "记忆ID")
    private String memoryId;

    @Schema(description = "版本号")
    private Integer versionNo;

    @Schema(description = "版本状态")
    private String versionStatus;

    @Schema(description = "来源任务ID")
    private String sourceTaskId;
}
