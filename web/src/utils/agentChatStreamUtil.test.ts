import {describe, expect, it} from "vitest";
import type {AgentChatProgressEventDto} from "../dto/agentChat/AgentChatDto";
import {appendAssistantToken, consumeAgentChatSseEvents, isAgentChatMessageEvent, replaceFinalMessage, stripProtocolJson} from "./agentChatStreamUtil";

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

/**
 * stripProtocolJson 过滤函数测试。
 *
 * @author qty
 */
describe("stripProtocolJson", () => {
    it("应原样保留不含协议关键字的普通文本", () => {
        const input = "你好，这是一条普通消息。";
        expect(stripProtocolJson(input)).toBe(input);
    });

    it("应移除 Markdown JSON 代码块中的协议数据", () => {
        const input = "分析结果如下：\n```json\n{\"event\":\"schedule\",\"action\":\"call_win_rpa\"}\n```\n以上是执行计划。";
        const output = stripProtocolJson(input);
        expect(output).toBe("分析结果如下：\n以上是执行计划。");
    });

    it("应移除无语言标记的 Markdown 代码块中的协议数据", () => {
        const input = "```\n{\"event\":\"schedule\",\"step\":\"2\"}\n```\n后续处理完成。";
        const output = stripProtocolJson(input);
        expect(output).toBe("后续处理完成。");
    });

    it("应保留不含协议关键字的 JSON 代码块", () => {
        const input = "配置如下：\n```json\n{\"name\":\"test\",\"version\":\"1.0\"}\n```";
        expect(stripProtocolJson(input)).toBe(input);
    });

    it("应移除独立行的单行协议 JSON 对象", () => {
        const input = "开始处理\n{\"event\":\"schedule\",\"action\":\"call_win_rpa\",\"result\":\"success\"}\n处理完成";
        const output = stripProtocolJson(input);
        expect(output).toBe("开始处理\n处理完成");
    });

    it("应移除跨多行的协议 JSON 对象", () => {
        const input = "执行中...\n{\n  \"event\": \"schedule\",\n  \"action\": \"call_win_rpa\"\n}\n执行完毕";
        const output = stripProtocolJson(input);
        expect(output).toBe("执行中...\n执行完毕");
    });

    it("应清理连续 4 个及以上空行为 3 个换行", () => {
        const input = "第一段\n\n\n\n\n第二段";
        const output = stripProtocolJson(input);
        // 4+ 个连续换行 → 3 个换行（即 2 个空行间隔）
        expect(output).toBe("第一段\n\n\n第二段");
    });

    it("空字符串应原样返回", () => {
        expect(stripProtocolJson("")).toBe("");
    });

    it("纯协议 JSON 内容应返回空字符串", () => {
        const input = "{\"event\":\"schedule\",\"action\":\"call_win_rpa\",\"params\":{},\"result\":\"success\"}";
        expect(stripProtocolJson(input)).toBe("");
    });
});
