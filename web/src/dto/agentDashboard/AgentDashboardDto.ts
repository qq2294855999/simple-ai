/**
 * 智能体工作台摘要。
 *
 * @author qty
 */
export interface AgentDashboardSummaryDto {
  agentCount: number;
  enabledAgentCount: number;
  skillCount: number;
  runningTaskCount: number;
  failedTaskCount: number;
}

/**
 * 智能体工作台近期任务。
 *
 * @author qty
 */
export interface AgentDashboardRecentTaskDto {
  id: string;
  agentName: string;
  taskName: string;
  execStatus: string;
  execStatusLabel: string;
  failureReason: string;
  updateTime: string;
}
