/**
 * 智能体记忆聚合分页请求参数。
 *
 * @author qty
 */
export interface AgentMemoryPageRequestDto {
  /** 当前页 */
  current: number;
  /** 每页条数 */
  size: number;
  /** 排序 */
  pageSort?: string;
  /** 关键字（模糊匹配记忆名称或智能体名称） */
  keyword?: string;
  /** 智能体ID精确筛选 */
  agentId?: string;
  /** 状态筛选 */
  status?: string;
}

/**
 * 智能体记忆聚合分页响应条目。
 *
 * @author qty
 */
export interface AgentMemoryPageResponseDto {
  /** 主键 */
  id: string;
  /** 智能体ID */
  agentId: string;
  /** 智能体名称 */
  agentName: string;
  /** 记忆名称 */
  memoryName: string;
  /** 步骤名称 */
  stepName: string;
  /** 触发条件 */
  triggerCondition: string;
  /** 触发动作 */
  triggerAction: string;
  /** 步骤数量 */
  stepCount: number;
  /** 任务数量 */
  taskCount: number;
  /** 最近任务状态 */
  latestTaskStatus: string;
  /** 最近任务状态标签 */
  latestTaskStatusLabel: string;
  /** 创建时间 */
  createTime: string;
  /** 更新时间 */
  updateTime: string;
  /** 状态 */
  status: string;
  /** 备注 */
  remark: string;
}

/**
 * 智能体记忆创建请求。
 *
 * @author qty
 */
export interface CreateAgentMemoryRequestDto {
  /** 智能体ID */
  agentId: string;
  /** 记忆名称 */
  memoryName: string;
  /** 步骤名称 */
  stepName: string;
  /** 触发条件 */
  triggerCondition: string;
  /** 触发动作 */
  triggerAction: string;
  /** 备注 */
  remark?: string;
}

/**
 * 智能体记忆更新请求。
 *
 * @author qty
 */
export interface UpdateAgentMemoryRequestDto extends CreateAgentMemoryRequestDto {
  /** 主键 */
  id: string;
}
