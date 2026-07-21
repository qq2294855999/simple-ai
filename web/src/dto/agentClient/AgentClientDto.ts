/**
 * 客户端实例分页请求参数。
 *
 * @author qty
 */
export interface AgentClientPageRequestDto {
    /** 当前页 */
    current: number;
    /** 每页条数 */
    size: number;
    /** 排序 */
    pageSort?: string;
    /** 关键字（模糊匹配名称） */
    keyword?: string;
    /** 执行器类型ID筛选 */
    executorId?: string;
    /** 状态筛选 */
    status?: string;
}

/**
 * 客户端实例分页响应条目。
 *
 * @author qty
 */
export interface AgentClientPageResponseDto {
    /** 主键 */
    id: string;
    /** 执行器类型ID */
    executorId: string;
    /** 执行器名称 */
    executorName?: string;
    /** 客户端名称 */
    clientName: string;
    /** 客户端状态 */
    status: string;
    /** 过期时间 */
    expireTime: string;
    /** 最后成功连接时间 */
    lastConnectedAt?: string;
    /** 机器名称 */
    machineName?: string;
    /** 创建时间 */
    createTime: string;
    /** 更新时间 */
    updateTime: string;
    /** 备注 */
    remark?: string;
}

/**
 * 客户端实例创建请求。
 *
 * @author qty
 */
export interface CreateAgentClientRequestDto {
    /** 执行器类型ID */
    executorId: string;
    /** 客户端名称 */
    clientName: string;
    /** 过期时间数字 */
    expireDuration?: number;
    /** 过期时间单位 */
    expireUnit?: string;
    /** 备注 */
    remark?: string;
}

/**
 * 客户端实例更新请求。
 *
 * @author qty
 */
export interface UpdateAgentClientRequestDto {
    /** 主键 */
    id: string;
    /** 执行器类型ID */
    executorId?: string;
    /** 客户端名称 */
    clientName?: string;
    /** 过期时间数字 */
    expireDuration?: number;
    /** 过期时间单位 */
    expireUnit?: string;
    /** 客户端状态 */
    status?: string;
    /** 备注 */
    remark?: string;
}

/**
 * 客户端实例创建响应（含明文密钥）。
 *
 * @author qty
 */
export interface AgentClientCreateResponseDto {
    /** 主键 */
    id: string;
    /** 客户端密钥（仅创建时返回） */
    clientSecret: string;
    /** 客户端名称 */
    clientName: string;
    /** 过期时间 */
    expireTime: string;
}
