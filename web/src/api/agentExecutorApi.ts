import {http} from "./http";
import type {PageResult} from "../dto/common/R";
import type {
    AgentExecutorPageRequestDto,
    AgentExecutorPageResponseDto,
    AgentExecutorProtocolResponse,
    CreateAgentExecutorRequestDto,
    UpdateAgentExecutorRequestDto
} from "../dto/agentExecutor/AgentExecutorDto";
import type {ProtocolPageResponseDto} from "../dto/protocol/ProtocolDto";

/**
 * 执行器类型 API 封装。
 *
 * @author qty
 */
export const AgentExecutorApi = {
    /** 分页查询 */
    page: (params: AgentExecutorPageRequestDto) =>
        http.get<PageResult<AgentExecutorPageResponseDto>>("/agent/executor/page", {params}),

    /** 查询单个 */
    findOne: (id: string) =>
        http.get<AgentExecutorPageResponseDto>(`/agent/executor/${id}`),

    /** 创建 */
    create: (data: CreateAgentExecutorRequestDto) =>
        http.post<string>("/agent/executor", data),

    /** 更新 */
    update: (id: string, data: UpdateAgentExecutorRequestDto) =>
        http.put(`/agent/executor`, data),

    /** 删除单个 */
    deleteById: (id: string) =>
        http.delete(`/agent/executor/${id}`),

    /** 获取 SEP v1.0 协议说明 */
    getProtocol: () =>
        http.get<AgentExecutorProtocolResponse>("/agent/executor/protocol"),

    /** 切换启用/停用状态 */
    toggleStatus: (id: string) =>
        http.put<string>(`/agent/executor/${id}/toggle-status`),

    /** 批量删除 */
    deleteByIds: (ids: string[]) =>
        Promise.all(ids.map(id => http.delete(`/agent/executor/${id}`))).then(() => undefined),

    /** 获取执行器关联的协议内容 */
    getExecutorProtocol: (executorId: string) =>
        http.get<string>(`/agent/executor/${executorId}/protocol`),

    /** 查询所有启用的协议列表（用于下拉选择） */
    findAllProtocols: () =>
        http.get<PageResult<ProtocolPageResponseDto>>("/agent/protocol/page", {params: {current: 1, size: 1000, status: "ON"}})
};