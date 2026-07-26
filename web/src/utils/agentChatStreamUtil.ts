import type {AgentChatExecutionEventDto, AgentChatMessageDto, AgentChatProgressEventDto} from "../dto/agentChat/AgentChatDto";

/**
 * 人机会话流式工具集合。
 * 负责 SSE 帧解析、三种气泡（进度/思考/回复）占位生成、流式 token 追加、最终化状态落定，
 * 以及历史兼容：旧数据没有 bubbleType / thinkingContent 时的归一化与合成气泡。
 *
 * @author qty
 */

const messageEventTypes = new Set(["MESSAGE_ACCEPTED", "AI_TOKEN", "MESSAGE_COMPLETED", "CHAT_FAILED"]);

/** 进度事件类型白名单：进入 PROGRESS 气泡的 executionEvents。AI_THINKING_TOKEN 不进入执行轨迹。 */
const progressTrackedEventTypes = new Set([
    "TASK_CREATED", "CONTEXT_ASSEMBLING", "CONTEXT_ASSEMBLED",
    "MEMORY_MATCHING", "MEMORY_MATCHED", "MEMORY_MISSED", "MEMORY_EXECUTING",
    "AI_STARTED", "AI_COMPLETED",
    "ATOMIC_COMMAND_START", "ATOMIC_COMMAND_COMPLETE", "ATOMIC_COMMAND_FAILED",
    "SUB_AGENT_STARTED", "SUB_AGENT_COMPLETED", "SUB_AGENT_FAILED",
    "TASK_COMPLETED", "TASK_FAILED"
]);

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
 * 构造进度气泡占位消息（bubbleType=PROGRESS），流式期间承载执行进度 timeline，完成后折叠保留。
 *
 * @param taskId 任务ID
 * @param sessionId 会话ID
 * @returns PROGRESS 气泡 DTO
 */
export function createProgressBubble(taskId: string, sessionId: string): AgentChatMessageDto {
    return {
        id: `streaming-progress-${taskId || sessionId || Date.now()}`,
        taskId: taskId || "",
        turnId: "",
        role: "ASSISTANT",
        content: "",
        contentFormat: "PLAIN_TEXT",
        sequenceNo: Date.now() + 1,
        createTime: "",
        providerName: "",
        modelCode: "",
        executionEvents: [],
        thinkingContent: "",
        thinkingContentFormat: "PLAIN_TEXT",
        bubbleType: "PROGRESS"
    };
}

/**
 * 构造思考气泡占位消息（bubbleType=THINKING），无内容时 finalize 阶段会被整体移除。
 *
 * @param taskId 任务ID
 * @param sessionId 会话ID
 * @returns THINKING 气泡 DTO
 */
export function createThinkingBubble(taskId: string, sessionId: string): AgentChatMessageDto {
    return {
        id: `streaming-thinking-${taskId || sessionId || Date.now()}`,
        taskId: taskId || "",
        turnId: "",
        role: "ASSISTANT",
        content: "",
        contentFormat: "PLAIN_TEXT",
        sequenceNo: Date.now() + 2,
        createTime: "",
        providerName: "",
        modelCode: "",
        executionEvents: [],
        thinkingContent: "",
        thinkingContentFormat: "PLAIN_TEXT",
        bubbleType: "THINKING"
    };
}

/**
 * 构造回复气泡占位消息（bubbleType=NORMAL，流式 assistant）。
 *
 * @param taskId 任务ID
 * @param sessionId 会话ID
 * @returns REPLY 气泡 DTO
 */
export function createReplyBubble(taskId: string, sessionId: string): AgentChatMessageDto {
    return {
        id: "streaming-assistant",
        taskId: taskId || "",
        turnId: "",
        role: "ASSISTANT",
        content: "",
        contentFormat: "RESTRICTED_MARKDOWN",
        sequenceNo: Date.now() + 3,
        createTime: "",
        providerName: "",
        modelCode: "",
        executionEvents: [],
        thinkingContent: "",
        thinkingContentFormat: "PLAIN_TEXT",
        bubbleType: "NORMAL"
    };
}

/**
 * 在消息列表中查找指定 taskId 的第一个匹配气泡。
 *
 * @param messages 当前消息列表
 * @param bubbleType 气泡类型
 * @param taskId 任务ID
 * @returns 匹配消息的索引和对象，未找到返回 undefined
 */
function findBubbleByType(
    messages: AgentChatMessageDto[],
    bubbleType: AgentChatMessageDto["bubbleType"],
    taskId?: string
): { index: number; message: AgentChatMessageDto } | undefined {
    for (let i = messages.length - 1; i >= 0; i--) {
        const msg = messages[i];
        if (msg.bubbleType !== bubbleType) {
            continue;
        }
        if (taskId && msg.taskId && msg.taskId !== taskId) {
            continue;
        }
        return {index: i, message: msg};
    }
    return undefined;
}

/**
 * 追加 AI 流式 token 到回复气泡 content。
 * 优先查找 taskId 匹配的 streaming-assistant / NORMAL 气泡，找不到时兼容旧格式假设最后一条是 assistant。
 *
 * @param messages 当前消息
 * @param event token 事件
 * @returns 更新后的消息
 */
export function appendAssistantToken(messages: AgentChatMessageDto[], event: AgentChatProgressEventDto): AgentChatMessageDto[] {
    const found = findBubbleByType(messages, "NORMAL", event.taskId);
    if (found) {
        const updated = [...messages];
        updated[found.index] = {
            ...found.message,
            content: (found.message.content || "") + (event.payload || "")
        };
        return updated;
    }

    // 兼容旧路径：最后一条可能是临时 assistant
    const lastMessage = messages[messages.length - 1];
    if (lastMessage?.id === "streaming-assistant") {
        return [...messages.slice(0, -1), {...lastMessage, content: lastMessage.content + (event.payload || "")}];
    }
    return [...messages, eventToStreamingMessage(event)];
}

/**
 * 将进度事件追加到 PROGRESS 气泡的 executionEvents 列表。
 * AI_THINKING_TOKEN 事件不会进入此路径（在消费层过滤）。
 *
 * @param messages 当前消息
 * @param event 进度事件
 * @returns 更新后的消息
 */
export function appendProgressEvent(messages: AgentChatMessageDto[], event: AgentChatProgressEventDto): AgentChatMessageDto[] {
    // AI_THINKING_TOKEN 由 appendThinkingToken 处理，不进入进度事件
    if (event.eventType === "AI_THINKING_TOKEN") {
        return messages;
    }

    // 非白名单事件（MESSAGE_ACCEPTED / CHAT_FAILED / MESSAGE_COMPLETED 等）不推入 execution 数组
    if (!progressTrackedEventTypes.has(event.eventType)) {
        return messages;
    }

    const found = findBubbleByType(messages, "PROGRESS", event.taskId);
    if (!found) {
        return messages;
    }

    const executionEvent: AgentChatExecutionEventDto = {
        id: `${event.taskId || "task"}-${found.message.executionEvents.length + 1}`,
        eventType: event.eventType,
        stepName: event.stepName || event.message || event.eventType,
        commandName: "",
        responseContent: event.payload || "",
        failureReason: event.failureReason || "",
        sequenceNo: found.message.executionEvents.length + 1,
        startedAt: "",
        finishedAt: "",
        providerName: "",
        modelCode: ""
    };

    const updated = [...messages];
    updated[found.index] = {
        ...found.message,
        executionEvents: [...found.message.executionEvents, executionEvent]
    };
    return updated;
}

/**
 * 追加 AI 思考推理文本片段到 THINKING 气泡。
 *
 * @param messages 当前消息
 * @param payload 思考文本片段
 * @param taskId 任务ID
 * @returns 更新后的消息
 */
export function appendThinkingToken(
    messages: AgentChatMessageDto[],
    payload: string,
    taskId?: string
): AgentChatMessageDto[] {
    const found = findBubbleByType(messages, "THINKING", taskId);
    if (!found) {
        return messages;
    }

    const updated = [...messages];
    updated[found.index] = {
        ...found.message,
        thinkingContent: (found.message.thinkingContent || "") + (payload || "")
    };
    return updated;
}

/**
 * 完成 PROGRESS 气泡：更新折叠状态标记、可选注入最终状态文本。
 * 完成后气泡仍保留在原位，用户可展开回顾，解决「割裂」位置跳跃问题。
 *
 * @param messages 当前消息
 * @param taskId 任务ID
 * @param status "OK" | "FAILED"
 * @returns 更新后的消息
 */
export function finalizeProgressBubble(
    messages: AgentChatMessageDto[],
    taskId: string,
    status: "OK" | "FAILED"
): AgentChatMessageDto[] {
    const found = findBubbleByType(messages, "PROGRESS", taskId);
    if (!found) {
        return messages;
    }

    // 最终 id 使用 taskId 前缀，流式 id 只用于前端占位不参与持久化比较
    const updated = [...messages];
    const totalSteps = found.message.executionEvents.length;
    const badge = status === "OK" ? "✅" : "❌";
    updated[found.index] = {
        ...found.message,
        id: `final-progress-${taskId || found.message.id}`,
        content: `${badge} 执行详情 (${totalSteps} 步)`,
        bubbleType: "PROGRESS"
    };
    return updated;
}

/**
 * 完成 THINKING 气泡：无思考内容时整条移除，有内容时保留并折叠。
 *
 * @param messages 当前消息
 * @param taskId 任务ID
 * @returns 更新后的消息
 */
export function finalizeThinkingBubble(messages: AgentChatMessageDto[], taskId: string): AgentChatMessageDto[] {
    const found = findBubbleByType(messages, "THINKING", taskId);
    if (!found) {
        return messages;
    }

    const trimmed = (found.message.thinkingContent || "").trim();
    if (trimmed.length === 0) {
        // 无思考内容时整个气泡从 DOM 移除，不留下空占位
        const updated = [...messages];
        updated.splice(found.index, 1);
        return updated;
    }

    const updated = [...messages];
    const charCount = trimmed.length;
    updated[found.index] = {
        ...found.message,
        id: `final-thinking-${taskId || found.message.id}`,
        content: `✅ 思考过程 (约 ${charCount} 字)`,
        bubbleType: "THINKING"
    };
    return updated;
}

/**
 * 替换流式临时回复气泡为最终消息内容。
 * 不再把 executionEvents 合并进回复气泡（已独立到 PROGRESS 气泡）。
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
        turnId: "",
        role: isFailed ? "SYSTEM_ERROR" : "ASSISTANT",
        content: event.payload || event.failureReason || "",
        contentFormat: isFailed ? "PLAIN_TEXT" : "RESTRICTED_MARKDOWN",
        sequenceNo: Date.now(),
        createTime: "",
        providerName: "",
        modelCode: "",
        executionEvents: [],
        thinkingContent: "",
        thinkingContentFormat: "PLAIN_TEXT",
        bubbleType: "NORMAL"
    };
    const withoutStreaming = messages.filter(item => item.id !== "streaming-assistant");
    return [...withoutStreaming, message];
}

/**
 * 构建流式临时回复消息（兼容旧路径调用）。
 *
 * @param event token 事件
 * @returns 临时 AI 消息
 */
function eventToStreamingMessage(event: AgentChatProgressEventDto): AgentChatMessageDto {
    return {
        id: "streaming-assistant",
        taskId: event.taskId,
        turnId: "",
        role: "ASSISTANT",
        content: event.payload || "",
        contentFormat: "RESTRICTED_MARKDOWN",
        sequenceNo: Date.now(),
        createTime: "",
        providerName: "",
        modelCode: "",
        executionEvents: [],
        thinkingContent: "",
        thinkingContentFormat: "PLAIN_TEXT",
        bubbleType: "NORMAL"
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

/**
 * 将进度事件列表转换为执行事件列表（保留用于简版历史回放 + 回复气泡锚点）。
 * AI_THINKING_TOKEN 被过滤，不进入 execution 数组。
 *
 * @param progressEvents 进度事件列表
 * @param taskId 任务ID
 * @returns 执行事件列表
 */
export function progressEventsToExecutionEvents(
    progressEvents: AgentChatProgressEventDto[],
    taskId: string
): AgentChatExecutionEventDto[] {
    return progressEvents
        .filter(event => event.eventType !== "AI_THINKING_TOKEN")
        .map((event, idx) => ({
            id: `${taskId}-${idx}`,
            eventType: event.eventType,
            stepName: event.stepName || event.message || event.eventType,
            commandName: "",
            responseContent: event.payload || "",
            failureReason: event.failureReason || "",
            sequenceNo: idx + 1,
            startedAt: "",
            finishedAt: "",
            providerName: "",
            modelCode: ""
        }));
}