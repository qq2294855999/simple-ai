import {http} from "./http";
import type {PageResult} from "../dto/common/R";
import type {AgentSkillPageRequestDto} from "../dto/agentSkill/AgentSkillPageRequestDto";
import type {AgentSkillPageResponseDto} from "../dto/agentSkill/AgentSkillPageResponseDto";
import type {AgentSkillInfoResponseDto, CreateAgentSkillRequestDto, UpdateAgentSkillRequestDto} from "../dto/agentSkill/CreateAgentSkillRequestDto";

/**
 * 智能体技能 API 封装。
 *
 * @author qty
 */
export const AgentSkillApi = {
  /** 聚合分页查询 */
  page: (params: AgentSkillPageRequestDto) =>
    http.get<PageResult<AgentSkillPageResponseDto>>("/sys/agent-skill/aggregate-list", { params }),

    /** 查询单个详情 */
  findOne: (id: string) =>
        http.get<AgentSkillInfoResponseDto>(`/sys/agent-skill/find/${id}`),

  /** 创建 */
  create: (data: CreateAgentSkillRequestDto) =>
    http.post<string>("/sys/agent-skill/create", data),

  /** 更新 */
  update: (id: string, data: UpdateAgentSkillRequestDto) =>
    http.put(`/sys/agent-skill/update/${id}`, data),

  /** 批量删除 */
  deleteByIds: (ids: string[]) =>
    http.delete("/sys/agent-skill/deletes", { data: ids }),

  /** 启用 */
  enable: (id: string) =>
    http.put(`/sys/agent-skill/enable/${id}`),

  /** 禁用 */
  disable: (id: string) =>
      http.put(`/sys/agent-skill/disable/${id}`),

    /** 切换启用/停用状态 */
    toggleStatus: (id: string) =>
        http.put<string>(`/sys/agent-skill/toggle-status/${id}`)
};