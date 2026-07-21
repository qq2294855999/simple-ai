package com.simple.ai.common.dto.agentMemoryVersion;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 记忆版本(agent_memory_version)新增请求参数。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "记忆版本新增请求参数")
public class CreateAgentMemoryVersionRequest {

    @Schema(description = "记忆ID")
    @NotEmpty(message = "记忆ID不能为空")
    private String memoryId;

    @Schema(description = "版本号")
    @NotNull(message = "版本号不能为空")
    private Integer versionNo;

    @Schema(description = "版本状态")
    private String versionStatus;

    @Schema(description = "来源任务ID")
    private String sourceTaskId;

    @Schema(description = "成功判定规则")
    private String successAssertion;

    @Schema(description = "版本摘要")
    private String summary;

    @Schema(description = "创建原因")
    private String createReason;
}
