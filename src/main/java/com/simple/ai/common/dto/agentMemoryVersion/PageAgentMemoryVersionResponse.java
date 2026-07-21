package com.simple.ai.common.dto.agentMemoryVersion;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 记忆版本(agent_memory_version)分页明细响应。
 *
 * @author qty
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(chain = true)
@Schema(title = "记忆版本分页明细响应")
public class PageAgentMemoryVersionResponse {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "记忆ID")
    private String memoryId;

    @Schema(description = "版本号")
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

    @Schema(description = "创建人用户ID")
    private String createUserId;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "修改时间")
    private Date updateTime;
}
