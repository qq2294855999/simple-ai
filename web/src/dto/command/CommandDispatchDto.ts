/**
 * 命令调度请求。
 *
 * @author qty
 */
export interface CommandDispatchRequestDto {
  /** 智能体主键 */
  agentId: string;
  /** 命令名称 */
  commandName: string;
  /** 命令内容 */
  commandContent: string;
  /** 会话标识 */
  sessionId?: string;
  /** 显式模型主键 */
  modelId?: string;
  /** 扩展请求参数 */
  requestParams?: Record<string, unknown>;
}

/**
 * 命令调度最终响应。
 *
 * @author qty
 */
export interface CommandDispatchResponseDto {
  /** 任务主键 */
  taskId: string;
  /** 执行状态 */
  execStatus: string;
  /** 响应内容 */
  responseContent: string;
  /** 失败原因 */
  failureReason: string;
}

/**
 * 命令调度进度事件。
 *
 * @author qty
 */
export interface CommandDispatchProgressEventDto {
  /** 任务主键 */
  taskId: string;
  /** 会话标识 */
  sessionId: string;
  /** 事件类型 */
  eventType: string;
  /** 步骤主键 */
  stepId: string;
  /** 步骤名称 */
  stepName: string;
  /** 执行状态 */
  execStatus: string;
  /** 事件消息 */
  message: string;
  /** 事件数据 */
  payload: string;
  /** 是否完成 */
  completed: boolean;
  /** 失败原因 */
  failureReason: string;
  /** 模型供应商名称快照 */
  providerName: string;
  /** 模型编码快照 */
  modelCode: string;
}
