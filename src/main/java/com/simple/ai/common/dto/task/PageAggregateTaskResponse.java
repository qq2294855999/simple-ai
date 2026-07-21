package com.simple.ai.common.dto.task;

import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 任务聚合分页响应参数。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "任务聚合分页响应参数")
public class PageAggregateTaskResponse {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "智能体记忆ID")
    private String agentMemoryId;

    @Schema(description = "记忆名称")
    private String memoryName;

    @Schema(description = "智能体ID")
    private String agentId;

    @Schema(description = "智能体名称")
    private String agentName;

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "父任务ID")
    private String parentTaskId;

    @Schema(description = "父任务名称")
    private String parentTaskName;

    @Schema(description = "下一个任务ID")
    private String nextTaskId;

    @Schema(description = "下一个任务名称")
    private String nextTaskName;

    @Schema(description = "步骤类型")
    private String stepType;

    @Schema(description = "步骤类型标签")
    private String stepTypeLabel;

    @Schema(description = "执行状态")
    private String execStatus;

    @Schema(description = "执行状态标签")
    private String execStatusLabel;

    @Schema(description = "失败原因")
    private String failureReason;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "修改时间")
    private Date updateTime;

    @Schema(description = "状态")
    private Status status;

    @Schema(description = "备注")
    private String remark;
}