/**
 * 记忆版本分页请求参数。
 *
 * @author qty
 */
export interface AgentMemoryVersionPageRequestDto {
    /** 当前页 */
    current: number;
    /** 每页条数 */
    size: number;
    /** 排序 */
    pageSort?: string;
    /** 关键字（模糊匹配摘要或原因） */
    keyword?: string;
    /** 记忆ID筛选 */
    memoryId?: string;
    /** 版本状态筛选 */
    versionStatus?: string;
}

/**
 * 记忆版本分页响应条目。
 *
 * @author qty
 */
export interface AgentMemoryVersionPageResponseDto {
    /** 主键 */
    id: string;
    /** 记忆ID */
    memoryId: string;
    /** 记忆名称 */
    memoryName?: string;
    /** 版本号 */
    versionNo: number;
    /** 版本状态 */
    versionStatus: string;
    /** 来源任务ID */
    sourceTaskId?: string;
    /** 版本摘要 */
    summary?: string;
    /** 创建原因 */
    createReason?: string;
    /** 创建时间 */
    createTime: string;
    /** 修改时间 */
    updateTime: string;
}

/**
 * 记忆版本创建请求。
 *
 * @author qty
 */
export interface CreateAgentMemoryVersionRequestDto {
    /** 记忆ID */
    memoryId: string;
    /** 版本号 */
    versionNo: number;
    /** 版本状态 */
    versionStatus?: string;
    /** 来源任务ID */
    sourceTaskId?: string;
    /** 成功判定规则 */
    successAssertion?: string;
    /** 版本摘要 */
    summary?: string;
    /** 创建原因 */
    createReason?: string;
}

/**
 * 记忆版本更新请求。
 *
 * @author qty
 */
export interface UpdateAgentMemoryVersionRequestDto {
    /** 主键 */
    id: string;
    /** 记忆ID */
    memoryId: string;
    /** 版本号 */
    versionNo: number;
    /** 版本状态 */
    versionStatus?: string;
    /** 来源任务ID */
    sourceTaskId?: string;
    /** 成功判定规则 */
    successAssertion?: string;
    /** 版本摘要 */
    summary?: string;
    /** 创建原因 */
    createReason?: string;
}
