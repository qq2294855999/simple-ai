import type { PageRequest } from "../common/R";

/**
 * 智能体技能聚合分页请求参数。
 *
 * @author qty
 */
export interface AgentSkillPageRequestDto extends PageRequest {
  /** 关键字（模糊匹配智能体名称或描述） */
  keyword?: string;
  /** 智能体ID精确筛选 */
  agentId?: string;
  /** 状态筛选 */
  status?: string;
}
