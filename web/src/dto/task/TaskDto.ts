/**
 * 任务聚合分页请求参数。
 *
 * @author qty
 */
export interface TaskPageRequestDto {
  /** 当前页 */
  current: number;
  /** 每页条数 */
  size: number;
  /** 排序 */
  pageSort?: string;
  /** 关键字（模糊匹配任务名称或记忆名称） */
  keyword?: string;
  /** 智能体ID精确筛选 */
  agentId?: string;
  /** 执行状态筛选 */
  execStatus?: string;
  /** 状态筛选 */
  status?: string;
}

/**
 * 任务聚合分页响应条目。
 *
 * @author qty
 */
export interface TaskPageResponseDto {
  /** 主键 */
  id: string;
  /** 记忆ID */
  agentMemoryId: string;
  /** 记忆名称 */
  memoryName: string;
  /** 智能体ID */
  agentId: string;
  /** 智能体名称 */
  agentName: string;
  /** 任务名称 */
  taskName: string;
  /** 父任务ID */
  parentTaskId: string;
  /** 父任务名称 */
  parentTaskName: string;
  /** 下一个任务ID */
  nextTaskId: string;
  /** 下一个任务名称 */
  nextTaskName: string;
  /** 步骤类型 */
  stepType: string;
  /** 步骤类型标签 */
  stepTypeLabel: string;
  /** 执行状态 */
  execStatus: string;
  /** 执行状态标签 */
  execStatusLabel: string;
  /** 失败原因 */
  failureReason: string;
  /** 分支条件（详情接口返回） */
  branchCondition?: string;
  /** 分支路由（详情接口返回） */
  branchRoute?: string;
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
 * 任务创建请求。
 *
 * @author qty
 */
export interface CreateTaskRequestDto {
  /** 记忆ID */
  agentMemoryId: string;
  /** 任务名称 */
  taskName: string;
  /** 父任务ID */
  parentTaskId?: string;
  /** 下一个任务ID */
  nextTaskId?: string;
  /** 步骤类型 */
  stepType: string;
  /** 分支条件 */
  branchCondition?: string;
  /** 分支路由 */
  branchRoute?: string;
  /** 备注 */
  remark?: string;
}

/**
 * 任务更新请求。
 *
 * @author qty
 */
export interface UpdateTaskRequestDto extends CreateTaskRequestDto {
  /** 主键 */
  id: string;
}
