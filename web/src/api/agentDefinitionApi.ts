import { http } from "./http";
import type { PageResult } from "../dto/common/R";
import type {
  AgentDefinitionPageDto,
  AgentDefinitionInfoDto,
  CreateAgentDefinitionDto,
  UpdateAgentDefinitionDto
} from "../dto/agentDefinition/AgentDefinitionDto";

/**
 * 智能体定义管理 API 封装。
 *
 * @author qty
 */
export const AgentDefinitionApi = {
  /** 聚合分页列表 */
  page: (params: { current: number; size: number; keyword?: string; status?: number }) =>
    http.get<PageResult<AgentDefinitionPageDto>>("/sys/agent-definition/aggregate-list", { params }),

  /** 聚合全量列表（用于下拉选择） */
  listAll: () =>
    http.get<PageResult<AgentDefinitionPageDto>>("/sys/agent-definition/aggregate-list", {
      params: { current: 1, size: 1000 }
    }),

  /** 查询详情 */
  findOne: (id: string) =>
    http.get<AgentDefinitionInfoDto>(`/sys/agent-definition/find/${id}`),

  /** 查询聚合详情 */
  aggregateFindOne: (id: string) =>
    http.get<AgentDefinitionPageDto>(`/sys/agent-definition/aggregate-find/${id}`),

  /** 创建 */
  create: (data: CreateAgentDefinitionDto) =>
    http.post<string>("/sys/agent-definition/create", data),

  /** 更新 */
  update: (id: string, data: UpdateAgentDefinitionDto) =>
    http.put(`/sys/agent-definition/update/${id}`, data),

  /** 批量删除 */
  deleteByIds: (ids: string[]) =>
    http.delete("/sys/agent-definition/deletes", { data: ids }),

  /** 级联删除 */
  cascadeDelete: (ids: string[]) =>
    http.delete("/sys/agent-definition/cascade-deletes", { data: ids })
};
