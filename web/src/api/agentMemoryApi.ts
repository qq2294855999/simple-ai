import {http} from "./http";
import type {PageResult} from "../dto/common/R";
import type {
    AgentMemoryInfoResponseDto,
    AgentMemoryPageRequestDto,
    AgentMemoryPageResponseDto,
    CreateAgentMemoryRequestDto,
    ExecuteMemoryRequestDto,
    ExecuteMemoryResponseDto,
    ParamsDefinitionResponseDto,
    UpdateAgentMemoryRequestDto
} from "../dto/agentMemory/AgentMemoryDto";

/**
 * 智能体记忆 API 封装。
 *
 * @author qty
 */
export const AgentMemoryApi = {

    /** 分页查询 */
    page: (params: AgentMemoryPageRequestDto) =>
        http.get<PageResult<AgentMemoryPageResponseDto>>("/sys/agent-memory/list", {params}),

    /** 查询单个 */
    findOne: (id: string) =>
        http.get<AgentMemoryInfoResponseDto>(`/sys/agent-memory/find/${id}`),

    /** 创建 */
    create: (data: CreateAgentMemoryRequestDto) =>
        http.post<string>("/sys/agent-memory/create", data),

    /** 更新 */
    update: (id: string, data: UpdateAgentMemoryRequestDto) =>
        http.put(`/sys/agent-memory/update/${id}`, data),

    /** 批量删除 */
    deleteByIds: (ids: string[]) =>
        http.delete("/sys/agent-memory/deletes", {data: ids}),

    /** 获取参数定义 */
    getParamsDefinition: (id: string) =>
        http.get<ParamsDefinitionResponseDto>(`/sys/agent-memory/${id}/params-definition`),

    /** 执行记忆 */
    execute: (id: string, data: ExecuteMemoryRequestDto) =>
        http.post<ExecuteMemoryResponseDto>(`/sys/agent-memory/${id}/execute`, data)
};