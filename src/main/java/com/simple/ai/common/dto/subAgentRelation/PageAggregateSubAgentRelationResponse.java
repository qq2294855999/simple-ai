package com.simple.ai.common.dto.subAgentRelation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 子智能体关系聚合分页响应。
 *
 * @author qty
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(title = "子智能体关系聚合分页响应")
public class PageAggregateSubAgentRelationResponse {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "主智能体ID")
    private String mainAgentId;

    @Schema(description = "主智能体名称")
    private String mainAgentName;

    @Schema(description = "子智能体ID")
    private String subAgentId;

    @Schema(description = "子智能体名称")
    private String subAgentName;

    @Schema(description = "修改时间")
    private Date updateTime;

    @Schema(description = "状态")
    private Status status;

    @Schema(description = "备注")
    private String remark;
}
