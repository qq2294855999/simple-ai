import { http, getAccessToken } from "./http";
import type { CommandDispatchProgressEventDto, CommandDispatchRequestDto, CommandDispatchResponseDto } from "../dto/command/CommandDispatchDto";

const dispatchStreamUrl = "/api/sys/agent-command/dispatch-stream";

/**
 * 命令调度 API 封装。
 *
 * @author qty
 */
export const CommandDispatchApi = {
  /** 非流式调度 */
  dispatch: (data: CommandDispatchRequestDto) =>
    http.post<CommandDispatchResponseDto>("/sys/agent-command/dispatch", data),

  /** POST SSE 流式调度 */
  dispatchStream: async (data: CommandDispatchRequestDto, onProgress: (event: CommandDispatchProgressEventDto) => void) => {
    // 构建请求头，附加 Bearer token 以通过后端认证
    const headers: Record<string, string> = {
      Accept: "text/event-stream",
      "Content-Type": "application/json"
    };
    const token = getAccessToken();
    if (token) {
      headers["Authorization"] = `Bearer ${token}`;
    }

    const response = await fetch(dispatchStreamUrl, {
      method: "POST",
      headers,
      body: JSON.stringify(data)
    });

    // 非成功响应时读取服务端文本，确保页面可展示真实失败原因
    if (!response.ok) {
      const message = await response.text();
      throw new Error(message || "流式调度请求失败");
    }

    // SSE 响应必须包含可读取的数据流
    if (!response.body) {
      throw new Error("流式调度未返回事件数据");
    }

    const reader = response.body.getReader();
    const decoder = new TextDecoder("utf-8");
    let buffer = "";

    // 持续读取 SSE 数据块，直到服务端完成任务并关闭连接
    while (true) {
      const result = await reader.read();
      if (result.done) {
        break;
      }
      buffer += decoder.decode(result.value, { stream: true });
      buffer = consumeSseEvents(buffer, onProgress);
    }

    // 处理连接关闭前尚未带空行分隔的最后一个事件
    consumeSseEvents(buffer, onProgress, true);
  }
};

/**
 * 消费已接收的 SSE 事件帧。
 *
 * @param buffer 未处理数据
 * @param onProgress 进度事件处理器
 * @param flush 是否处理最后一个不完整分隔帧
 * @return 剩余未处理数据
 */
function consumeSseEvents(buffer: string, onProgress: (event: CommandDispatchProgressEventDto) => void, flush = false) {
  const normalizedBuffer = buffer.replace(/\r\n/g, "\n");
  const frames = normalizedBuffer.split("\n\n");
  const remainingBuffer = flush ? "" : frames.pop() ?? "";

  // 逐帧解析服务端命名事件和 JSON 数据
  for (const frame of frames) {
    const dataLine = frame.split("\n").find(line => line.startsWith("data:"));
    if (!dataLine) {
      continue;
    }
    const content = dataLine.slice(5).trim();
    if (!content) {
      continue;
    }
    onProgress(JSON.parse(content) as CommandDispatchProgressEventDto);
  }
  return remainingBuffer;
}
