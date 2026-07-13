export interface AgentChatSessionDto {
  id: string;
  agentId: string;
  agentName: string;
  sessionName: string;
  lastMessageAt: string;
}

export interface AgentChatMessageDto {
  id: string;
  taskId: string;
  role: "USER" | "ASSISTANT" | "SYSTEM_ERROR";
  content: string;
  contentFormat: "PLAIN_TEXT" | "RESTRICTED_MARKDOWN";
  sequenceNo: number;
  createTime: string;
  /** 模型供应商名称快照 */
  providerName: string;
  /** 模型编码快照 */
  modelCode: string;
}

export interface CreateAgentChatSessionRequestDto {
  agentId: string;
}

export interface SendAgentChatMessageRequestDto {
  sessionId: string;
  content: string;
  /** 显式模型主键 */
  modelId?: string;
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
