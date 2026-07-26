import {http} from "./http";
import type {PageResult} from "../dto/common/R";
import type {
    AgentRuleInfoResponseDto,
    AgentRulePageRequestDto,
    AgentRulePageResponseDto,
    CreateAgentRuleRequestDto,
    UpdateAgentRuleRequestDto
} from "../dto/agentRule/AgentRuleDto";

/**
 * 智能体规则 API 封装。
 *
 * @author qty
 */
export const AgentRuleApi = {
  /** 聚合分页查询 */
  page: (params: AgentRulePageRequestDto) =>
    http.get<PageResult<AgentRulePageResponseDto>>("/sys/agent-rule/aggregate-list", { params }),

    /** 查询单个详情 */
  findOne: (id: string) =>
        http.get<AgentRuleInfoResponseDto>(`/sys/agent-rule/find/${id}`),

  /** 创建 */
  create: (data: CreateAgentRuleRequestDto) =>
    http.post<string>("/sys/agent-rule/create", data),

  /** 更新 */
  update: (id: string, data: UpdateAgentRuleRequestDto) =>
    http.put(`/sys/agent-rule/update/${id}`, data),

  /** 批量删除 */
  deleteByIds: (ids: string[]) =>
    http.delete("/sys/agent-rule/deletes", { data: ids }),

  /** 启用 */
  enable: (id: string) =>
    http.put(`/sys/agent-rule/enable/${id}`),

  /** 禁用 */
  disable: (id: string) =>
      http.put(`/sys/agent-rule/disable/${id}`),

    /** 切换启用/停用状态 */
    toggleStatus: (id: string) =>
        http.put<string>(`/sys/agent-rule/toggle-status/${id}`)
};