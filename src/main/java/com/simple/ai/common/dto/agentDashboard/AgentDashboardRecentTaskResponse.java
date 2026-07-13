package com.simple.ai.common.dto.agentDashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 智能体工作台近期任务响应。
 *
 * @author qty
 */
@Data
@Schema(title = "智能体工作台近期任务响应")
public class AgentDashboardRecentTaskResponse {

    /**
     * 任务主键。
     */
    @Schema(description = "任务主键")
    private String id;

    /**
     * 智能体名称。
     */
    @Schema(description = "智能体名称")
    private String agentName;

    /**
     * 任务名称。
     */
    @Schema(description = "任务名称")
    private String taskName;

    /**
     * 执行状态。
     */
    @Schema(description = "执行状态")
    private String execStatus;

    /**
     * 执行状态说明。
     */
    @Schema(description = "执行状态说明")
    private String execStatusLabel;

    /**
     * 失败原因。
     */
    @Schema(description = "失败原因")
    private String failureReason;

    /**
     * 修改时间。
     */
    @Schema(description = "修改时间")
    private Date updateTime;
}
