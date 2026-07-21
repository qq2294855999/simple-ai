package com.simple.ai.common.dto.agentMemory;

import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 智能体记忆聚合分页响应。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "智能体记忆聚合分页响应")
public class PageAggregateAgentMemoryResponse {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "智能体ID")
    private String agentId;

    @Schema(description = "智能体名称")
    private String agentName;

    @Schema(description = "记忆名称")
    private String memoryName;

    @Schema(description = "步骤名称")
    private String stepName;

    @Schema(description = "触发条件")
    private String triggerCondition;

    @Schema(description = "触发动作")
    private String triggerAction;

    @Schema(description = "步骤数量")
    private Long stepCount;

    @Schema(description = "任务数量")
    private Long taskCount;

    @Schema(description = "最近任务状态")
    private String latestTaskStatus;

    @Schema(description = "最近任务状态标签")
    private String latestTaskStatusLabel;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "修改时间")
    private Date updateTime;

    @Schema(description = "状态")
    private Status status;

    @Schema(description = "备注")
    private String remark;
}