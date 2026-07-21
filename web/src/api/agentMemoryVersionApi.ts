import {http} from "./http";
import type {PageResult} from "../dto/common/R";
import type {
    AgentMemoryVersionPageRequestDto,
    AgentMemoryVersionPageResponseDto,
    CreateAgentMemoryVersionRequestDto,
    UpdateAgentMemoryVersionRequestDto
} from "../dto/agentMemoryVersion/AgentMemoryVersionDto";

/**
 * 记忆版本 API 封装。
 *
 * @author qty
 */
export const AgentMemoryVersionApi = {
    /** 分页查询 */
    page: (params: AgentMemoryVersionPageRequestDto) =>
        http.get<PageResult<AgentMemoryVersionPageResponseDto>>("/agent/memory-version/page", {params}),

    /** 查询单个 */
    findOne: (id: string) =>
        http.get<AgentMemoryVersionPageResponseDto>(`/agent/memory-version/${id}`),

    /** 创建 */
    create: (data: CreateAgentMemoryVersionRequestDto) =>
        http.post<string>("/agent/memory-version", data),

    /** 更新 */
    update: (id: string, data: UpdateAgentMemoryVersionRequestDto) =>
        http.put(`/agent/memory-version`, data),

    /** 删除单个 */
    deleteById: (id: string) =>
        http.delete(`/agent/memory-version/${id}`),

    /** 批量删除 */
    deleteByIds: (ids: string[]) =>
        Promise.all(ids.map(id => http.delete(`/agent/memory-version/${id}`))).then(() => undefined),

    /** 发布记忆版本 */
    publish: (id: string) =>
        http.put(`/agent/memory-version/publish/${id}`),

    /** 废弃记忆版本 */
    retire: (id: string) =>
        http.put(`/agent/memory-version/retire/${id}`)
};
