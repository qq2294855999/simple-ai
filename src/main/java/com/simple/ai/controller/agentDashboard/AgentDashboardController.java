package com.simple.ai.controller.agentDashboard;

import com.simple.ai.common.dto.agentDashboard.AgentDashboardRecentTaskResponse;
import com.simple.ai.common.dto.agentDashboard.AgentDashboardSummaryResponse;
import com.simple.ai.common.service.agentDashboard.AgentDashboardService;
import com.simple.common.auth.client.common.annotation.HasAuthority;
import com.simple.common.core.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 智能体工作台控制层。
 *
 * @author qty
 */
@Tag(name = "智能体工作台")
@RequestMapping("sys/agent-dashboard")
@RestController
public class AgentDashboardController {

    /**
     * 智能体工作台服务。
     */
    @Autowired
    private AgentDashboardService agentDashboardService;

    /**
     * 查询工作台摘要。
     *
     * @return 工作台摘要
     */
    @GetMapping("summary")
    @Operation(summary = "查询智能体工作台摘要")
    @HasAuthority("sys:agent-dashboard:summary")
    public R<AgentDashboardSummaryResponse> summary() {
        return R.ok(agentDashboardService.findSummary());
    }

    /**
     * 查询工作台近期任务。
     *
     * @param size 返回数量
     * @return 近期任务列表
     */
    @GetMapping("recent-tasks")
    @Operation(summary = "查询智能体工作台近期任务")
    @HasAuthority("sys:agent-dashboard:recent-tasks")
    public R<List<AgentDashboardRecentTaskResponse>> recentTasks(@RequestParam(defaultValue = "10") Long size) {
        return R.ok(agentDashboardService.findRecentTasks(size));
    }
}
