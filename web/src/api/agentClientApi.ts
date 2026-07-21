import {http} from "./http";
import type {PageResult} from "../dto/common/R";
import type {
    AgentClientCreateResponseDto,
    AgentClientPageRequestDto,
    AgentClientPageResponseDto,
    CreateAgentClientRequestDto,
    UpdateAgentClientRequestDto
} from "../dto/agentClient/AgentClientDto";

/**
 * 客户端实例 API 封装。
 *
 * @author qty
 */
export const AgentClientApi = {
    /** 分页查询 */
    page: (params: AgentClientPageRequestDto) =>
        http.get<PageResult<AgentClientPageResponseDto>>("/agent/client/page", {params}),

    /** 查询单个 */
    findOne: (id: string) =>
        http.get<AgentClientPageResponseDto>(`/agent/client/${id}`),

    /** 创建（返回含明文密钥） */
    create: (data: CreateAgentClientRequestDto) =>
        http.post<AgentClientCreateResponseDto>("/agent/client", data),

    /** 更新 */
    update: (id: string, data: UpdateAgentClientRequestDto) =>
        http.put(`/agent/client`, data),

    /** 删除单个 */
    deleteById: (id: string) =>
        http.delete(`/agent/client/${id}`),

    /** 批量删除 */
    deleteByIds: (ids: string[]) =>
        Promise.all(ids.map(id => http.delete(`/agent/client/${id}`))).then(() => undefined)
};
