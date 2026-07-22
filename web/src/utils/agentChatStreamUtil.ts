import type {AgentChatMessageDto, AgentChatProgressEventDto} from "../dto/agentChat/AgentChatDto";

const messageEventTypes = new Set(["MESSAGE_ACCEPTED", "AI_TOKEN", "MESSAGE_COMPLETED", "CHAT_FAILED"]);

/** 执行协议 JSON 特征关键字，用于识别机器对机器的协议数据。 */
const protocolKeywords = ["\"event\"", "\"action\"", "\"schedule\"", "\"call_win_rpa\""];

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

/**
 * 判断文本是否包含执行协议特征关键字。
 *
 * @param text 待检测文本
 * @returns 是否包含协议关键字
 */
function hasProtocolKeyword(text: string): boolean {
    return protocolKeywords.some(keyword => text.includes(keyword));
}

/**
 * 从 AI 响应内容中移除执行协议 JSON 块。
 * 去除 Markdown JSON 代码块和内嵌的执行协议 JSON 对象，
 * 保留用户可见的自然语言对话内容。
 *
 * @param content 原始 AI 响应内容
 * @returns 过滤后的纯文本/Markdown 内容
 * @author qty
 */
export function stripProtocolJson(content: string): string {
    if (!content) {
        return content;
    }

    let result = content;

    // 移除 Markdown JSON 代码块（```json ... ``` 或 ``` ... ```）
    // 当代码块内容以 { 开头且包含协议特征关键字时移除整个代码块
    result = result.replace(/```(?:json)?\s*\n([\s\S]*?)```/g, (_match, codeContent: string) => {
        const trimmedContent = codeContent.trim();
        if (trimmedContent.startsWith("{") && hasProtocolKeyword(trimmedContent)) {
            return "";
        }
        return _match;
    });

    // 移除独立行上的 JSON 对象字符串（可能跨多行）
    // 匹配以 { 开头、} 结尾的文本块，包含协议特征关键字时移除
    result = result.replace(/^\s*\{[\s\S]*?\}\s*$/gm, match => {
        if (hasProtocolKeyword(match)) {
            return "";
        }
        return match;
    });

    // 清理多余的空行（连续 4 个及以上换行压缩为 3 个换行，即 2 个空行）
    result = result.replace(/\n{4,}/g, "\n\n\n");

    // 去除首尾空白
    result = result.trim();

    return result;
}
