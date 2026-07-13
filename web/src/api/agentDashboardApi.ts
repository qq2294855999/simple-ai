import { http } from "./http";
import type { AgentDashboardRecentTaskDto, AgentDashboardSummaryDto } from "../dto/agentDashboard/AgentDashboardDto";

/**
 * 智能体工作台 API 封装。
 *
 * @author qty
 */
export const AgentDashboardApi = {
  /** 查询工作台摘要 */
  summary: () =>
    http.get<AgentDashboardSummaryDto>("/sys/agent-dashboard/summary"),

  /** 查询近期任务 */
  recentTasks: (size = 10) =>
    http.get<AgentDashboardRecentTaskDto[]>("/sys/agent-dashboard/recent-tasks", { params: { size } })
};
