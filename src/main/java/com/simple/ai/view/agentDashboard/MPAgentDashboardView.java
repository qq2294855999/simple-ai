package com.simple.ai.view.agentDashboard;

import com.simple.ai.common.dto.agentDashboard.AgentDashboardRecentTaskResponse;
import com.simple.ai.common.dto.agentDashboard.AgentDashboardSummaryResponse;
import com.simple.ai.common.view.agentDashboard.AgentDashboardView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 智能体工作台数据库视图实现。
 *
 * @author qty
 */
@Component
class MPAgentDashboardView implements AgentDashboardView {

    /**
     * 智能体工作台数据库访问层。
     */
    @Autowired
    private AgentDashboardRepository repository;

    /**
     * 查询工作台摘要。
     *
     * @return 工作台摘要
     */
    @Override
    public AgentDashboardSummaryResponse findSummary() {
        return repository.selectSummary();
    }

    /**
     * 查询近期任务。
     *
     * @param size 返回数量
     * @return 近期任务列表
     */
    @Override
    public List<AgentDashboardRecentTaskResponse> findRecentTasks(Long size) {
        return repository.selectRecentTasks(size);
    }
}
