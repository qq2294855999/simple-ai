import {API_BASE_URL, clearAndRedirectToLogin, getBusinessAuthorizationHeader, http} from "./http";
import {consumeAgentChatSseEvents} from "../utils/agentChatStreamUtil";
import type {
    AgentChatMessageDto,
    AgentChatProgressEventDto,
    AgentChatSessionDto,
    AgentChatTrajectoryDto,
    AgentChatTurnStatusDto,
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

    findSessions: (agentId: string, modelId?: string, clientId?: string) =>
        http.get<AgentChatSessionDto[]>("/sys/agent-chat/session-list", {params: {agentId, modelId, clientId}}),

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
      const text = await response.text();

      // 检测认证错误码（后端全局异常处理器将 DefaultException 映射为 HTTP 500 时携带）
      let isAuthError = false;
      try {
        const errorBody = JSON.parse(text);
        if (errorBody.code === "1000" || errorBody.code === "1001") {
          isAuthError = true;
        }
      } catch {
        // 非 JSON 响应或解析失败，忽略解析错误
      }

      // 认证失效直接触发登录跳转
      if (isAuthError) {
        clearAndRedirectToLogin();
        // clearAndRedirectToLogin 内部抛出异常中断执行，此行不会到达
        throw new Error("redirecting to login");
      }

      throw new Error(text || "流式聊天请求失败");
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
      http.get<AgentChatTrajectoryDto[]>(`/sys/agent-chat/trajectory/${sessionId}`),

    /** 查询轮次状态，用于断线重连时判断轮次是否已完成。 */
    findTurnStatus: (turnId: string) =>
        http.get<AgentChatTurnStatusDto>(`/sys/agent-chat/turn/${turnId}/status`),

    /**
     * 带断线重连的流式发送消息。
     * 在网络断开后自动重试，使用指数退避策略，最多重试5次。
     *
     * @param data 发送消息请求（含幂等键）
     * @param onProgress 进度事件回调
     * @param signal 取消信号
     * @param loadMessagesFn 重新加载消息的回调，用于检查AI是否已回复
     */
    sendStreamWithRetry: async (
        data: SendAgentChatMessageRequestDto,
        onProgress: (event: AgentChatProgressEventDto) => void,
        signal: AbortSignal,
        loadMessagesFn: () => Promise<AgentChatMessageDto[]>
    ) => {
        const maxRetries = 5;
        const maxBackoff = 30000;

        // 记录当前消息列表中的ID集合，用于判断是否有新AI回复
        const existingMessageIds = new Set<string>();

        let lastError: unknown;

        // 重试循环
        for (let attempt = 0; attempt <= maxRetries; attempt++) {
            // 首次不延时
            if (attempt > 0) {
                // 指数退避：1s, 2s, 4s, 8s, 16s, 30s
                const delay = Math.min(1000 * Math.pow(2, attempt - 1), maxBackoff);
                await new Promise(resolve => setTimeout(resolve, delay));
            }

            // 每次重试前检查取消状态
            if (signal.aborted) {
                throw new Error("请求已取消");
            }

            try {
                await AgentChatApi.sendStream(data, onProgress, signal);
                return;
            } catch (error) {
                // 用户主动取消不重试
                if (signal.aborted) {
                    throw error;
                }
                lastError = error;

                // 已达最大重试次数
                if (attempt >= maxRetries) {
                    break;
                }

                try {
                    // 重新加载消息，检查AI是否已经回复
                    const loadedMessages = await loadMessagesFn();

                    // 检查是否有新的AI回复（不在已有消息集合中）
                    const hasNewAiReply = loadedMessages.some(
                        m => (m.role === "ASSISTANT" || m.role === "SYSTEM_ERROR") && !existingMessageIds.has(m.id)
                    );

                    // AI已回复，重连成功（由调用方处理消息更新）
                    if (hasNewAiReply) {
                        return;
                    }
                } catch {
                    // 加载消息失败不影响重试流程
                }
            }
        }

        // 所有重试均失败
        throw lastError;
    }
};