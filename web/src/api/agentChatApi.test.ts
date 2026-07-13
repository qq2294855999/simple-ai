import { afterEach, describe, expect, it, vi } from "vitest";
import { AgentChatApi } from "./agentChatApi";

const localFetch = vi.fn();

/**
 * 智能体聊天 SSE API 鉴权契约测试。
 *
 * @author qty
 */
describe("AgentChatApi.sendStream", () => {
  afterEach(() => {
    window.localStorage.clear();
    vi.unstubAllGlobals();
    localFetch.mockReset();
  });

  it("应在存在业务令牌时使用与 REST 相同的 Bearer Authorization 头并消费 SSE 事件", async () => {
    window.localStorage.setItem("accessToken", "local-access-token");
    localFetch.mockResolvedValue(buildSseResponse("data: {\"eventType\":\"MESSAGE_ACCEPTED\",\"taskId\":\"\",\"sessionId\":\"session-local\",\"payload\":\"\",\"completed\":false}\n\n"));
    vi.stubGlobal("fetch", localFetch);
    const events: string[] = [];

    await AgentChatApi.sendStream(
      { sessionId: "session-local", content: "本地 SSE 测试消息" },
      event => events.push(event.eventType)
    );

    expect(localFetch).toHaveBeenCalledWith(
      "/api/sys/agent-chat/send-stream",
      expect.objectContaining({
        method: "POST",
        headers: expect.objectContaining({
          Accept: "text/event-stream",
          "Content-Type": "application/json",
          Authorization: "Bearer local-access-token"
        })
      })
    );
    expect(events).toEqual(["MESSAGE_ACCEPTED"]);
  });

  it("应在未启用登录令牌时不伪造 Authorization 头", async () => {
    localFetch.mockResolvedValue(buildSseResponse(""));
    vi.stubGlobal("fetch", localFetch);

    await AgentChatApi.sendStream(
      { sessionId: "session-local", content: "无令牌本地测试消息" },
      () => undefined
    );

    const options = localFetch.mock.calls[0][1] as RequestInit;
    const headers = options.headers as Record<string, string>;
    expect(headers.Authorization).toBeUndefined();
  });

  it("应使用统一 API 根路径并透传调用方取消信号", async () => {
    localFetch.mockResolvedValue(buildSseResponse(""));
    vi.stubGlobal("fetch", localFetch);
    const abortController = new AbortController();

    await AgentChatApi.sendStream(
      { sessionId: "session-local", content: "取消信号本地测试消息" },
      () => undefined,
      abortController.signal
    );

    expect(localFetch).toHaveBeenCalledWith(
      "/api/sys/agent-chat/send-stream",
      expect.objectContaining({ signal: abortController.signal })
    );
  });
});

/**
 * 构建仅包含本地 SSE 文本的响应替身。
 *
 * @param content SSE 响应文本
 * @returns 可读取的 fetch 响应替身
 */
function buildSseResponse(content: string): Response {
  const encoder = new TextEncoder();
  const stream = new ReadableStream<Uint8Array>({
    start(controller) {

      // 仅推送测试构造的本地帧，不连接任何远端服务
      if (content) {
        controller.enqueue(encoder.encode(content));
      }
      controller.close();
    }
  });
  return new Response(stream, { status: 200 });
}
