package com.simple.ai.common.dto.agentDashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 智能体工作台摘要响应。
 *
 * @author qty
 */
@Data
@Schema(title = "智能体工作台摘要响应")
public class AgentDashboardSummaryResponse {

    /**
     * 智能体总数。
     */
    @Schema(description = "智能体总数")
    private Long agentCount;

    /**
     * 启用智能体数量。
     */
    @Schema(description = "启用智能体数量")
    private Long enabledAgentCount;

    /**
     * 技能数量。
     */
    @Schema(description = "技能数量")
    private Long skillCount;

    /**
     * 运行中任务数量。
     */
    @Schema(description = "运行中任务数量")
    private Long runningTaskCount;

    /**
     * 失败待排查任务数量。
     */
    @Schema(description = "失败待排查任务数量")
    private Long failedTaskCount;
}
