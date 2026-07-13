import { describe, expect, it } from "vitest";
import type { AgentChatProgressEventDto } from "../dto/agentChat/AgentChatDto";
import {
  appendAssistantToken,
  consumeAgentChatSseEvents,
  isAgentChatMessageEvent,
  replaceFinalMessage
} from "./agentChatStreamUtil";

const tokenEvent: AgentChatProgressEventDto = {
  taskId: "task-local",
  sessionId: "session-local",
  eventType: "AI_TOKEN",
  stepId: "",
  stepName: "",
  execStatus: "RUNNING",
  message: "AI 内容片段",
  payload: "第一段",
  completed: false,
  failureReason: ""
};

/**
 * 智能体聊天流工具测试。
 *
 * @author qty
 */
describe("agentChatStreamUtil", () => {
  it("应解析多行 data 帧并保留未完成帧", () => {
    const events: AgentChatProgressEventDto[] = [];
    const frame = "event: AI_TOKEN\r\n"
      + "data: {\"taskId\":\"task-local\",\r\n"
      + "data: \"sessionId\":\"session-local\",\"eventType\":\"AI_TOKEN\",\"stepId\":\"\",\"stepName\":\"\",\"execStatus\":\"RUNNING\",\"message\":\"AI 内容片段\",\"payload\":\"第一段\",\"completed\":false,\"failureReason\":\"\"}\r\n\r\npartial";

    const remaining = consumeAgentChatSseEvents(frame, event => events.push(event));

    expect(events).toEqual([tokenEvent]);
    expect(remaining).toBe("partial");
  });

  it("应隔离异常 JSON 且继续解析后续有效事件", () => {
    const events: AgentChatProgressEventDto[] = [];
    const valid = JSON.stringify({ ...tokenEvent, eventType: "TASK_CREATED" });
    const frame = `data: {invalid}\n\ndata: ${valid}\n\n`;

    consumeAgentChatSseEvents(frame, event => events.push(event));

    expect(events).toHaveLength(1);
    expect(events[0].eventType).toBe("TASK_CREATED");
  });

  it("应将 AI_TOKEN 与最终消息留在消息流，将调度事件分流到时间线", () => {
    const messages = appendAssistantToken([], tokenEvent);
    const appended = appendAssistantToken(messages, { ...tokenEvent, payload: "第二段" });
    const finalMessages = replaceFinalMessage(appended, { ...tokenEvent, eventType: "MESSAGE_COMPLETED", payload: "最终回复", completed: true });

    expect(isAgentChatMessageEvent("AI_TOKEN")).toBe(true);
    expect(isAgentChatMessageEvent("MESSAGE_COMPLETED")).toBe(true);
    expect(isAgentChatMessageEvent("TASK_CREATED")).toBe(false);
    expect(appended[0].content).toBe("第一段第二段");
    expect(finalMessages).toHaveLength(1);
    expect(finalMessages[0].role).toBe("ASSISTANT");
    expect(finalMessages[0].content).toBe("最终回复");
  });

  it("应将 CHAT_FAILED 转换为纯文本系统消息", () => {
    const messages = replaceFinalMessage([], { ...tokenEvent, eventType: "CHAT_FAILED", payload: "", failureReason: "本地失败", completed: true });

    expect(messages[0].role).toBe("SYSTEM_ERROR");
    expect(messages[0].contentFormat).toBe("PLAIN_TEXT");
    expect(messages[0].content).toBe("本地失败");
  });
});
