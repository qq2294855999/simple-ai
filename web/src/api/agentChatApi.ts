import { API_BASE_URL, getBusinessAuthorizationHeader, http } from "./http";
import { consumeAgentChatSseEvents } from "../utils/agentChatStreamUtil";
import type {
  AgentChatMessageDto,
  AgentChatProgressEventDto,
  AgentChatSessionDto,
  AgentChatTrajectoryDto,
  CreateAgentChatSessionRequestDto,
  SendAgentChatMessageRequestDto
} from "../dto/agentChat/AgentChatDto";

const chatStreamUrl = `${API_BASE_URL}/sys/agent-chat/send-stream`;

/**
 * 智能体人机对话 API。
 *
 * @author qty
 */
export const AgentChatApi = {
  createSession: (data: CreateAgentChatSessionRequestDto) =>
    http.post<AgentChatSessionDto>("/sys/agent-chat/session", data),

  findSessions: (agentId: string) =>
    http.get<AgentChatSessionDto[]>("/sys/agent-chat/session-list", { params: { agentId } }),

  findMessages: (sessionId: string) =>
    http.get<AgentChatMessageDto[]>(`/sys/agent-chat/message-list/${sessionId}`),

  /** 分页查询消息（上滑加载更早的消息）。 */
  findMessagesPage: (sessionId: string, size: number, beforeSeq: number) =>
    http.get<AgentChatMessageDto[]>(`/sys/agent-chat/message-list/${sessionId}`, { params: { size, beforeSeq } }),

  sendStream: async (data: SendAgentChatMessageRequestDto, onProgress: (event: AgentChatProgressEventDto) => void,
                     signal?: AbortSignal) => {
    const response = await fetch(chatStreamUrl, {
      method: "POST",
      headers: {
        Accept: "text/event-stream",
        "Content-Type": "application/json",
        ...getBusinessAuthorizationHeader()
      },
      body: JSON.stringify(data),
      signal
    });

    // 响应失败时读取服务端文本，确保用户可见真实失败原因
    if (!response.ok) {
      const message = await response.text();
      throw new Error(message || "流式聊天请求失败");
    }

    // SSE 必须提供可读取的响应流
    if (!response.body) {
      throw new Error("流式聊天未返回事件数据");
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder("utf-8");
    let buffer = "";

    // 持续处理服务端事件帧直至对话完成
    while (true) {
      const result = await reader.read();
      if (result.done) {
        break;
      }
      buffer += decoder.decode(result.value, { stream: true });
      buffer = consumeAgentChatSseEvents(buffer, onProgress);
    }
    consumeAgentChatSseEvents(buffer, onProgress, true);
  },

  /** 删除单个会话及其关联消息。 */
  deleteSession: (id: string) =>
    http.delete(`/sys/agent-chat/session/${id}`),

  /** 批量删除会话及其关联消息。 */
  deleteSessions: (ids: string[]) =>
    http.delete("/sys/agent-chat/sessions", { data: ids }),

  /** 查询会话的历史执行轨迹。 */
  findTrajectory: (sessionId: string) =>
    http.get<AgentChatTrajectoryDto[]>(`/sys/agent-chat/trajectory/${sessionId}`)
};
