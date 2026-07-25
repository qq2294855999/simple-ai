/**
 * 智能体记忆分页请求参数。
 *
 * @author qty
 */
export interface AgentMemoryPageRequestDto {
    /** 当前页 */
    current: number;
    /** 每页条数 */
    size: number;
    /** 排序 */
    pageSort?: string;
    /** 智能体ID精确筛选 */
    agentId?: string;
    /** 记忆名称模糊搜索 */
    memoryName?: string;
    /** 版本状态筛选：1=DRAFT, 2=PUBLISHED, 3=RETIRED */
    versionStatus?: number;
    /** 状态筛选 */
    status?: string;
}

/**
 * 智能体记忆分页响应条目。
 *
 * @author qty
 */
export interface AgentMemoryPageResponseDto {
    /** 主键 */
    id: string;
    /** 智能体ID */
    agentId: string;
    /** 智能体名称 */
    agentName: string;
    /** 父记忆ID */
    parentMemoryId: string;
    /** 记忆名称模板 */
    memoryName: string;
    /** 版本号 */
    versionNo: number;
    /** 版本状态：1=DRAFT, 2=PUBLISHED, 3=RETIRED */
    versionStatus: number;
    /** 记忆摘要 */
    summary: string;
    /** 创建原因 */
    createReason: string;
    /** 修改时间 */
    updateTime: string;
    /** 状态 */
    status: string;
    /** 备注 */
    remark: string;
    /** 步骤数量 */
    stepCount: number;
    /** 参数数量 */
    paramCount: number;
}

/**
 * 智能体记忆详情响应。
 *
 * @author qty
 */
export interface AgentMemoryInfoResponseDto {
    /** 主键 */
    id: string;
    /** 智能体ID */
    agentId: string;
    /** 父记忆ID */
    parentMemoryId: string;
    /** 父记忆名称 */
    parentMemoryName: string;
    /** 记忆名称模板 */
    memoryName: string;
    /** 参数定义JSON */
    paramsDefinition: string;
    /** 版本号 */
    versionNo: number;
    /** 版本状态 */
    versionStatus: number;
    /** 来源任务ID */
    sourceTaskId: string;
    /** 记忆摘要 */
    summary: string;
    /** 创建原因 */
    createReason: string;
    /** 记忆步骤列表 */
    steps: AgentMemoryStepDto[];
    /** 创建时间 */
    createTime: string;
    /** 修改时间 */
    updateTime: string;
    /** 状态 */
    status: string;
    /** 备注 */
    remark: string;
}

/**
 * 记忆步骤数据。
 *
 * @author qty
 */
export interface AgentMemoryStepDto {
    /** 主键 */
    id: string;
    /** 记忆ID */
    memoryId: string;
    /** 步骤序号 */
    sequenceNo: number;
    /** 原子命令ID */
    atomicCommandId: string;
    /** 原子命令编码 */
    atomicCommandCode: string;
    /** 步骤名称 */
    stepName: string;
    /** 参数模板JSON */
    argsTemplate: string;
    /** 延迟最小值(ms) */
    delayMinMs: number;
    /** 延迟最大值(ms) */
    delayMaxMs: number;
    /** 超时时间(ms) */
    timeoutMs: number;
    /** 成功断言 */
    successAssertion: string;
    /** 失败策略 */
    failureStrategy: string;
    /** 状态 */
    stepStatus: string;
}

/**
 * 智能体记忆创建请求。
 *
 * @author qty
 */
export interface CreateAgentMemoryRequestDto {
    /** 智能体ID */
    agentId: string;
    /** 记忆名称模板 */
    memoryName: string;
    /** 参数定义JSON */
    paramsDefinition?: string;
    /** 记忆摘要 */
    summary?: string;
    /** 备注 */
    remark?: string;
}

/**
 * 智能体记忆修改请求。
 *
 * @author qty
 */
export interface UpdateAgentMemoryRequestDto extends CreateAgentMemoryRequestDto {
    /** 主键 */
    id: string;
}

/**
 * 执行记忆请求参数。
 *
 * @author qty
 */
export interface ExecuteMemoryRequestDto {
    /** 执行参数 */
    params?: Record<string, unknown>;
    /** 客户端ID */
    clientId?: string;
}

/**
 * 执行记忆响应参数。
 *
 * @author qty
 */
export interface ExecuteMemoryResponseDto {
    /** 任务ID */
    taskId: string;
    /** 执行状态 */
    execStatus: string;
    /** 记忆ID */
    memoryId: string;
    /** 记忆版本号快照 */
    memoryVersionNo: number;
}

/**
 * 记忆参数定义响应。
 *
 * @author qty
 */
export interface ParamsDefinitionResponseDto {
    /** 记忆ID */
    memoryId: string;
    /** 记忆名称模板 */
    memoryName: string;
    /** 参数定义JSON */
    paramsDefinition: string;
    /** 步骤列表 */
    steps: AgentMemoryStepDto[];
}

/**
 * 记忆版本历史响应。
 *
 * @author qty
 */
export interface MemoryVersionHistoryResponseDto {
    /** 记忆ID */
    id: string;
    /** 版本号 */
    versionNo: number;
    /** 版本状态 */
    versionStatus: number;
    /** 父记忆ID */
    parentMemoryId: string;
    /** 创建原因 */
    createReason: string;
    /** 记忆摘要 */
    summary: string;
    /** 创建时间 */
    createTime: string;
}