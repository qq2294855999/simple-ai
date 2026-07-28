import {describe, expect, it} from "vitest";
import type {AgentChatProgressEventDto} from "../dto/agentChat/AgentChatDto";
import {
    appendAssistantToken,
    appendProgressEvent,
    consumeAgentChatSseEvents,
    createProgressBubble,
    replaceFinalMessage,
    stripProtocolJson
} from "./agentChatStreamUtil";

const replyEvent: AgentChatProgressEventDto = {
  taskId: "task-local",
  sessionId: "session-local",
    eventType: "REPLY",
  stepId: "",
  stepName: "",
  execStatus: "RUNNING",
    message: "AI 回复内容",
  payload: "第一段",
    data: "",
  completed: false,
    failureReason: "",
    errorReason: "",
    messageId: "",
    turnId: "",
    thinkingSummary: ""
};

/**
 * 智能体聊天流工具测试。
 *
 * @author qty
 */
describe("agentChatStreamUtil", () => {
  it("应解析多行 data 帧并保留未完成帧", () => {
    const events: AgentChatProgressEventDto[] = [];
      const frame = "event: REPLY\r\n"
      + "data: {\"taskId\":\"task-local\",\r\n"
          + "data: \"sessionId\":\"session-local\",\"eventType\":\"REPLY\",\"stepId\":\"\",\"stepName\":\"\",\"execStatus\":\"RUNNING\",\"message\":\"AI 回复内容\",\"payload\":\"第一段\",\"data\":\"\",\"completed\":false,\"failureReason\":\"\",\"errorReason\":\"\",\"messageId\":\"\",\"turnId\":\"\",\"thinkingSummary\":\"\"}\r\n\r\npartial";

    const remaining = consumeAgentChatSseEvents(frame, event => events.push(event));

      expect(events).toEqual([replyEvent]);
    expect(remaining).toBe("partial");
  });

  it("应隔离异常 JSON 且继续解析后续有效事件", () => {
    const events: AgentChatProgressEventDto[] = [];
      const valid = JSON.stringify({...replyEvent, eventType: "PROGRESS"});
    const frame = `data: {invalid}\n\ndata: ${valid}\n\n`;

    consumeAgentChatSseEvents(frame, event => events.push(event));

    expect(events).toHaveLength(1);
      expect(events[0].eventType).toBe("PROGRESS");
  });

    it("应将 REPLY 事件追加到回复气泡，FINAL 事件完成消息", () => {
        const messages = appendAssistantToken([], replyEvent);
        const appended = appendAssistantToken(messages, {...replyEvent, payload: "第二段"});
        const finalMessages = replaceFinalMessage(appended, {...replyEvent, eventType: "FINAL", payload: "最终回复", completed: true});

    expect(appended[0].content).toBe("第一段第二段");
    expect(finalMessages).toHaveLength(1);
    expect(finalMessages[0].role).toBe("ASSISTANT");
    expect(finalMessages[0].content).toBe("最终回复");
  });

    it("应将稳定 PROGRESS 事件的 data 映射为可展示步骤文案", () => {
        const progressBubble = createProgressBubble("task-local", "session-local");
        const messages = appendProgressEvent([progressBubble], {
            ...replyEvent,
            type: "PROGRESS",
            eventType: "",
            message: "",
            stepName: "",
            data: "正在执行：打开微信"
        });

        expect(messages[0].executionEvents).toHaveLength(1);
        expect(messages[0].executionEvents[0].eventType).toBe("PROGRESS");
        expect(messages[0].executionEvents[0].stepName).toBe("正在执行：打开微信");
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