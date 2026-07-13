package com.simple.ai.common.service.agentDashboard;

import com.simple.ai.common.dto.agentDashboard.AgentDashboardRecentTaskResponse;
import com.simple.ai.common.dto.agentDashboard.AgentDashboardSummaryResponse;

import java.util.List;

/**
 * 智能体工作台服务。
 *
 * @author qty
 */
public interface AgentDashboardService {

    /**
     * 查询工作台摘要。
     *
     * @return 工作台摘要
     */
    AgentDashboardSummaryResponse findSummary();

    /**
     * 查询近期任务。
     *
     * @param size 返回数量
     * @return 近期任务列表
     */
    List<AgentDashboardRecentTaskResponse> findRecentTasks(Long size);
}
