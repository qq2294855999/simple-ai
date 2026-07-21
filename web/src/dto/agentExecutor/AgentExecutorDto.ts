/**
 * 执行器类型分页请求参数。
 *
 * @author qty
 */
export interface AgentExecutorPageRequestDto {
    /** 当前页 */
    current: number;
    /** 每页条数 */
    size: number;
    /** 排序 */
    pageSort?: string;
    /** 关键字（模糊匹配名称或编码） */
    keyword?: string;
    /** 状态筛选 */
    status?: string;
}

/**
 * 执行器类型分页响应条目。
 *
 * @author qty
 */
export interface AgentExecutorPageResponseDto {
    /** 主键 */
    id: string;
    /** 执行器编码 */
    executorCode: string;
    /** 执行器名称 */
    executorName: string;
    /** 执行器描述 */
    description?: string;
    /** 状态 */
    status: string;
    /** 创建时间 */
    createTime: string;
    /** 修改时间 */
    updateTime: string;
    /** 备注 */
    remark?: string;
}

/**
 * 执行器类型创建请求。
 *
 * @author qty
 */
export interface CreateAgentExecutorRequestDto {
    /** 执行器编码 */
    executorCode: string;
    /** 执行器名称 */
    executorName: string;
    /** 执行器描述 */
    description?: string;
    /** 备注 */
    remark?: string;
}

/**
 * 执行器类型更新请求。
 *
 * @author qty
 */
export interface UpdateAgentExecutorRequestDto extends CreateAgentExecutorRequestDto {
    /** 主键 */
    id: string;
}

// ===== SEP v1.0 协议展示相关类型 =====

/**
 * 字段说明。
 *
 * @author qty
 */
export interface ProtocolFieldInfo {
    /** 字段名 */
    name: string;
    /** 字段类型 */
    type: string;
    /** 是否必填 */
    required: boolean;
    /** 字段描述 */
    description: string;
}

/**
 * 消息外层结构。
 *
 * @author qty
 */
export interface ProtocolMessageStructure {
    /** 结构描述 */
    description: string;
    /** JSON 示例 */
    jsonExample: string;
    /** 字段说明列表 */
    fields: ProtocolFieldInfo[];
}

/**
 * 消息类型说明。
 *
 * @author qty
 */
export interface ProtocolMessageTypeInfo {
    /** 消息类型名称 */
    typeName: string;
    /** 消息方向（Server → Executor / Executor → Server） */
    direction: string;
    /** 消息描述 */
    description: string;
    /** 字段说明列表 */
    fields: ProtocolFieldInfo[];
    /** JSON 示例 */
    jsonExample: string;
}

/**
 * 系统命令说明。
 *
 * @author qty
 */
export interface ProtocolSystemCommandInfo {
    /** 命令编码 */
    commandCode: string;
    /** 命令描述 */
    description: string;
    /** 参数字段说明 */
    args: ProtocolFieldInfo[];
    /** 返回数据说明 */
    resultDescription: string;
    /** JSON 示例 */
    jsonExample: string;
}

/**
 * 执行器协议(SEP v1.0)明细响应。
 *
 * @author qty
 */
export interface AgentExecutorProtocolResponse {
    /** 协议名称 */
    protocolName: string;
    /** 协议版本 */
    protocolVersion: string;
    /** 协议描述 */
    description: string;
    /** 外层消息结构说明 */
    outerStructure: ProtocolMessageStructure;
    /** 消息类型列表 */
    messageTypes: ProtocolMessageTypeInfo[];
    /** 内置系统命令列表 */
    systemCommands: ProtocolSystemCommandInfo[];
    /** 通信流程说明 */
    communicationFlow: string;
}
