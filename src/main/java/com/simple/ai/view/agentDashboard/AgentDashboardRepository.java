package com.simple.ai.view.agentDashboard;

import com.simple.ai.common.dto.agentDashboard.AgentDashboardRecentTaskResponse;
import com.simple.ai.common.dto.agentDashboard.AgentDashboardSummaryResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 智能体工作台数据库访问层。
 *
 * @author qty
 */
@Mapper
public interface AgentDashboardRepository {

    /**
     * 查询工作台摘要。
     *
     * @return 工作台摘要
     */
    AgentDashboardSummaryResponse selectSummary();

    /**
     * 查询近期任务。
     *
     * @param size 返回数量
     * @return 近期任务列表
     */
    List<AgentDashboardRecentTaskResponse> selectRecentTasks(@Param("size") Long size);
}
