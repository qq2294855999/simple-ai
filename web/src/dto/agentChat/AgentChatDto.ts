/**
 * 人机会话相关 DTO 类型集合。
 * 包含会话、消息、进度事件、执行轨迹、轮次状态等前端契约。
 *
 * @author qty
 */

export interface AgentChatSessionDto {
  id: string;
  agentId: string;
  agentName: string;
  sessionName: string;
  lastMessageAt: string;
    /** 模型主键，会话级默认模型 */
    modelId: string;
    /** 客户端主键，会话级默认执行客户端 */
    clientId: string;
    /** 创建时间 */
    createTime: string;
}

export interface AgentChatMessageDto {
  id: string;
  taskId: string;
    /** 对话轮次主键 */
    turnId: string;
  role: "USER" | "ASSISTANT" | "SYSTEM_ERROR";
  content: string;
  contentFormat: "PLAIN_TEXT" | "RESTRICTED_MARKDOWN";
  sequenceNo: number;
  createTime: string;
  /** 模型供应商名称快照 */
  providerName: string;
  /** 模型编码快照 */
  modelCode: string;
    /** 该消息关联的执行事件列表，用于内嵌折叠轨迹展示 */
    executionEvents: AgentChatExecutionEventDto[];
    /** AI 思考推理过程完整文本（bubbleType=THINKING 时使用） */
    thinkingContent: string;
    /** 思考内容格式，与 contentFormat 对应 */
    thinkingContentFormat: "PLAIN_TEXT" | "RESTRICTED_MARKDOWN";
    /** 气泡类型：NORMAL 正常用户/回复；PROGRESS 进度特殊气泡；THINKING 思考特殊气泡 */
    bubbleType: "NORMAL" | "PROGRESS" | "THINKING";
}

export interface CreateAgentChatSessionRequestDto {
  agentId: string;
    /** 模型主键，必填 */
    modelId: string;
    /** 客户端主键，必填 */
    clientId: string;
}

export interface SendAgentChatMessageRequestDto {
  sessionId: string;
  content: string;
    /** 记忆操作标志（create/revise，空表示不操作记忆） */
    memoryAction?: string;
    /** 幂等键，防止断线重连后产生重复消息 */
    idempotencyKey?: string;
}

export interface AgentChatProgressEventDto {
  taskId: string;
  sessionId: string;
  eventType: string;
  stepId: string;
  stepName: string;
  execStatus: string;
  message: string;
  payload: string;
  completed: boolean;
  failureReason: string;
}

/** 会话历史执行轨迹（任务详情快照）。 */
export interface AgentChatTrajectoryDto {
  id: string;
  taskId: string;
  taskName: string;
  stepType: string;
  execStatus: string;
  providerName: string;
  modelCode: string;
  createTime: string;
}

/** 智能体聊天执行事件（内嵌折叠轨迹用）。 */
export interface AgentChatExecutionEventDto {
    id: string;
    eventType: string;
    stepName: string;
    commandName: string;
    responseContent: string;
    failureReason: string;
    sequenceNo: number;
    startedAt: string;
    finishedAt: string;
    providerName: string;
    modelCode: string;
}

/** 对话轮次状态（断线重连时查询）。 */
export interface AgentChatTurnStatusDto {
    turnId: string;
    sessionId: string;
    turnNumber: number;
    /** IN_PROGRESS / COMPLETED */
    turnStatus: string;
    assistantMessageId: string;
    taskId: string;
}