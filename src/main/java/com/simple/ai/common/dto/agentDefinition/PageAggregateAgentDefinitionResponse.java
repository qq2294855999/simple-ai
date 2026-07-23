package com.simple.ai.common.dto.agentDefinition;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 智能体定义聚合分页响应。
 *
 * @author qty
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(title = "智能体定义聚合分页响应")
public class PageAggregateAgentDefinitionResponse {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "定义描述")
    private String definitionDesc;

    @Schema(description = "默认模型主键")
    private String defaultModelId;

    @Schema(description = "状态")
    private Status status;

    @Schema(description = "技能数量")
    private Long skillCount;

    @Schema(description = "规则数量")
    private Long ruleCount;

    @Schema(description = "记忆数量")
    private Long memoryCount;

    @Schema(description = "子智能体数量")
    private Long subAgentCount;

    @Schema(description = "最近任务状态")
    private String recentTaskStatus;

    @Schema(description = "最近任务状态说明")
    private String recentTaskStatusLabel;

    @Schema(description = "最近任务时间")
    private Date latestTaskTime;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "修改时间")
    private Date updateTime;

    @Schema(description = "备注")
    private String remark;
}
