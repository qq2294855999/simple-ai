import { http } from "./http";
import type { PageResult } from "../dto/common/R";
import type { AgentRulePageRequestDto, AgentRulePageResponseDto, CreateAgentRuleRequestDto, UpdateAgentRuleRequestDto } from "../dto/agentRule/AgentRuleDto";

/**
 * 智能体规则 API 封装。
 *
 * @author qty
 */
export const AgentRuleApi = {
  /** 聚合分页查询 */
  page: (params: AgentRulePageRequestDto) =>
    http.get<PageResult<AgentRulePageResponseDto>>("/sys/agent-rule/aggregate-list", { params }),

  /** 查询单个 */
  findOne: (id: string) =>
    http.get<AgentRulePageResponseDto>(`/sys/agent-rule/find/${id}`),

  /** 创建 */
  create: (data: CreateAgentRuleRequestDto) =>
    http.post<string>("/sys/agent-rule/create", data),

  /** 更新 */
  update: (id: string, data: UpdateAgentRuleRequestDto) =>
    http.put(`/sys/agent-rule/update/${id}`, data),

  /** 批量删除 */
  deleteByIds: (ids: string[]) =>
    http.delete("/sys/agent-rule/deletes", { data: ids }),

  /** 全量列表（用于下拉选择） */
  listAll: () =>
    http.get<PageResult<AgentRulePageResponseDto>>("/sys/agent-rule/aggregate-list", { params: { current: 1, size: 1000 } }),

  /** 启用 */
  enable: (id: string) =>
    http.put(`/sys/agent-rule/enable/${id}`),

  /** 禁用 */
  disable: (id: string) =>
    http.put(`/sys/agent-rule/disable/${id}`)
};
