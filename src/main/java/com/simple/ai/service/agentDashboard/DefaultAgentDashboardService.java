package com.simple.ai.service.agentDashboard;

import com.simple.ai.common.dto.agentDashboard.AgentDashboardRecentTaskResponse;
import com.simple.ai.common.dto.agentDashboard.AgentDashboardSummaryResponse;
import com.simple.ai.common.service.agentDashboard.AgentDashboardService;
import com.simple.ai.common.view.agentDashboard.AgentDashboardView;
import com.simple.common.core.utils.AssertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 智能体工作台服务实现。
 *
 * @author qty
 */
@Service
@Transactional(readOnly = true)
class DefaultAgentDashboardService implements AgentDashboardService {

    /**
     * 智能体工作台数据库视图。
     */
    @Autowired
    private AgentDashboardView agentDashboardView;

    /**
     * 查询工作台摘要。
     *
     * @return 工作台摘要
     */
    @Override
    public AgentDashboardSummaryResponse findSummary() {
        return agentDashboardView.findSummary();
    }

    /**
     * 查询近期任务。
     *
     * @param size 返回数量
     * @return 近期任务列表
     */
    @Override
    public List<AgentDashboardRecentTaskResponse> findRecentTasks(Long size) {

        // 限制近期任务数量，避免工作台读取无边界运行记录
        AssertUtils.isTrue(size != null && size > 0 && size <= 100, "近期任务数量必须在1到100之间");
        return agentDashboardView.findRecentTasks(size);
    }
}
