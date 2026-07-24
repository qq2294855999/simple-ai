package com.simple.ai.common.dto.agentMemory;

import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 智能体记忆分页响应
 *
 * @author qty
 */
@Data
@Schema(title = "智能体记忆分页响应")
public class PageAgentMemoryResponse {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "智能体ID")
    private String agentId;

    @Schema(description = "智能体名称")
    private String agentName;

    @Schema(description = "记忆名称模板")
    private String memoryName;

    @Schema(description = "版本号")
    private Integer versionNo;

    @Schema(description = "版本状态：1=DRAFT, 2=PUBLISHED")
    private Integer versionStatus;

    @Schema(description = "记忆摘要")
    private String summary;

    @Schema(description = "创建原因")
    private String createReason;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "修改时间")
    private Date updateTime;

    @Schema(description = "状态")
    private Status status;

    @Schema(description = "备注")
    private String remark;
}