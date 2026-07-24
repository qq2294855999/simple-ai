/**
 * 执行器协议分页请求参数。
 *
 * @author qty
 */
export interface ProtocolPageRequestDto {
    /** 当前页 */
    current: number;
    /** 每页条数 */
    size: number;
    /** 排序 */
    pageSort?: string;
    /** 协议编码 */
    protocolCode?: string;
    /** 协议名称 */
    protocolName?: string;
    /** 协议版本 */
    protocolVersion?: string;
    /** 状态筛选 */
    status?: string;
}

/**
 * 执行器协议分页响应条目。
 *
 * @author qty
 */
export interface ProtocolPageResponseDto {
    /** 主键 */
    id: string;
    /** 协议编码 */
    protocolCode: string;
    /** 协议名称 */
    protocolName: string;
    /** 协议版本 */
    protocolVersion: string;
    /** 状态 */
    status: string;
    /** 创建时间 */
    createTime: string;
    /** 修改时间 */
    updateTime: string;
}

/**
 * 执行器协议详情响应。
 *
 * @author qty
 */
export interface ProtocolInfoResponseDto {
    /** 主键 */
    id: string;
    /** 协议编码 */
    protocolCode: string;
    /** 协议名称 */
    protocolName: string;
    /** 协议版本 */
    protocolVersion: string;
    /** 协议内容 */
    content: string;
    /** 状态 */
    status: string;
    /** 创建时间 */
    createTime: string;
    /** 修改时间 */
    updateTime: string;
}

/**
 * 执行器协议创建请求。
 *
 * @author qty
 */
export interface CreateProtocolRequestDto {
    /** 协议编码 */
    protocolCode: string;
    /** 协议名称 */
    protocolName: string;
    /** 协议版本 */
    protocolVersion: string;
    /** 协议内容 */
    content: string;
}

/**
 * 执行器协议更新请求。
 *
 * @author qty
 */
export interface UpdateProtocolRequestDto extends CreateProtocolRequestDto {
    /** 主键 */
    id: string;
}