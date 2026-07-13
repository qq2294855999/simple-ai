import type { AgentChatMessageDto, AgentChatProgressEventDto } from "../dto/agentChat/AgentChatDto";

const messageEventTypes = new Set(["MESSAGE_ACCEPTED", "AI_TOKEN", "MESSAGE_COMPLETED", "CHAT_FAILED"]);

/**
 * 消费完整 SSE 数据帧。
 *
 * @param buffer 未处理文本
 * @param onProgress 事件回调
 * @param flush 是否处理末尾帧
 * @returns 剩余文本
 */
export function consumeAgentChatSseEvents(buffer: string, onProgress: (event: AgentChatProgressEventDto) => void, flush = false): string {
  const normalizedBuffer = buffer.replace(/\r\n/g, "\n");
  const frames = normalizedBuffer.split("\n\n");
  const remainingBuffer = flush ? "" : frames.pop() ?? "";

  // 逐帧合并 data 行，兼容服务端对单个事件进行多行编码
  for (const frame of frames) {
    const content = frame.split("\n")
      .filter(line => line.startsWith("data:"))
      .map(line => line.slice(5).trimStart())
      .join("\n");
    if (!content) {
      continue;
    }

    // 非法 JSON 帧不应中断已建立的聊天流
    try {
      onProgress(JSON.parse(content) as AgentChatProgressEventDto);
    } catch {
      continue;
    }
  }
  return remainingBuffer;
}

/**
 * 判断事件是否属于聊天消息流。
 *
 * @param eventType 事件类型
 * @returns 是否为消息事件
 */
export function isAgentChatMessageEvent(eventType: string): boolean {
  return messageEventTypes.has(eventType);
}

/**
 * 追加 AI 流式 token。
 *
 * @param messages 当前消息
 * @param event token 事件
 * @returns 更新后的消息
 */
export function appendAssistantToken(messages: AgentChatMessageDto[], event: AgentChatProgressEventDto): AgentChatMessageDto[] {
  const lastMessage = messages[messages.length - 1];
  if (lastMessage?.id === "streaming-assistant") {
    return [...messages.slice(0, -1), { ...lastMessage, content: lastMessage.content + event.payload }];
  }
  return [...messages, eventToStreamingMessage(event)];
}

/**
 * 替换流式临时消息为最终消息。
 *
 * @param messages 当前消息
 * @param event 最终事件
 * @returns 更新后的消息
 */
export function replaceFinalMessage(messages: AgentChatMessageDto[], event: AgentChatProgressEventDto): AgentChatMessageDto[] {
  const isFailed = event.eventType === "CHAT_FAILED";
  const message: AgentChatMessageDto = {
    id: `final-${event.taskId}`,
    taskId: event.taskId,
    role: isFailed ? "SYSTEM_ERROR" : "ASSISTANT",
    content: event.payload || event.failureReason,
    contentFormat: isFailed ? "PLAIN_TEXT" : "RESTRICTED_MARKDOWN",
    sequenceNo: Date.now(),
    createTime: "",
    providerName: "",
    modelCode: ""
  };
  const withoutStreaming = messages.filter(item => item.id !== "streaming-assistant");
  return [...withoutStreaming, message];
}

/**
 * 构建流式临时消息。
 *
 * @param event token 事件
 * @returns 临时 AI 消息
 */
function eventToStreamingMessage(event: AgentChatProgressEventDto): AgentChatMessageDto {
  return {
    id: "streaming-assistant",
    taskId: event.taskId,
    role: "ASSISTANT",
    content: event.payload,
    contentFormat: "RESTRICTED_MARKDOWN",
    sequenceNo: Date.now(),
    createTime: "",
    providerName: "",
    modelCode: ""
  };
}
