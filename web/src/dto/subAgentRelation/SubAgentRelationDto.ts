/**
 * 子智能体关系聚合分页请求参数。
 *
 * @author qty
 */
export interface SubAgentRelationPageRequestDto {
  /** 当前页 */
  current: number;
  /** 每页条数 */
  size: number;
  /** 排序 */
  pageSort?: string;
  /** 关键字（模糊匹配主/子智能体名称） */
  keyword?: string;
  /** 主智能体ID精确筛选 */
  mainAgentId?: string;
  /** 状态筛选 */
  status?: string;
}

/**
 * 子智能体关系聚合分页响应条目。
 *
 * @author qty
 */
export interface SubAgentRelationPageResponseDto {
  /** 主键 */
  id: string;
  /** 主智能体ID */
  mainAgentId: string;
  /** 主智能体名称 */
  mainAgentName: string;
  /** 子智能体ID */
  subAgentId: string;
  /** 子智能体名称 */
  subAgentName: string;
  /** 修改时间 */
  updateTime: string;
  /** 状态 */
  status: string;
  /** 备注 */
  remark: string;
}

/**
 * 子智能体关系创建请求。
 *
 * @author qty
 */
export interface CreateSubAgentRelationRequestDto {
  /** 主智能体 */
  mainAgentId: string;
  /** 子智能体 */
  subAgentId: string;
  /** 备注 */
  remark?: string;
}

/**
 * 子智能体关系更新请求。
 *
 * @author qty
 */
export interface UpdateSubAgentRelationRequestDto extends CreateSubAgentRelationRequestDto {
  /** 主键 */
  id: string;
}
