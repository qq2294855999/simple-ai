/**
 * 原子命令聚合分页请求参数。
 *
 * @author qty
 */
export interface AtomicCommandPageRequestDto {
  /** 当前页 */
  current: number;
  /** 每页条数 */
  size: number;
  /** 排序 */
  pageSort?: string;
  /** 关键字（模糊匹配名称或命令） */
  keyword?: string;
  /** 技能ID精确筛选 */
  skillId?: string;
    /** 执行器ID筛选 */
    executorId?: string;
  /** 状态筛选 */
  status?: string;
}

/**
 * 原子命令聚合分页响应条目。
 *
 * @author qty
 */
export interface AtomicCommandPageResponseDto {
  /** 主键 */
  id: string;
  /** 名称 */
  name: string;
  /** 命令 */
  command: string;
  /** 作用 */
  role: string;
  /** 技能ID */
  skillId: string;
  /** 技能描述 */
  skillDesc: string;
    /** 执行器ID */
    executorId?: string;
    /** 执行器名称 */
    executorName?: string;
  /** 智能体ID */
  agentId: string;
  /** 智能体名称 */
  agentName: string;
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
 * 原子命令创建请求。
 *
 * @author qty
 */
export interface CreateAtomicCommandRequestDto {
  /** 名称 */
  name: string;
  /** 命令 */
  command: string;
  /** 作用 */
  role: string;
  /** 技能ID */
  skillId?: string;
    /** 执行器ID */
    executorId?: string;
  /** 备注 */
  remark?: string;
}

/**
 * 原子命令更新请求。
 *
 * @author qty
 */
export interface UpdateAtomicCommandRequestDto extends CreateAtomicCommandRequestDto {
  /** 主键 */
  id: string;
}
