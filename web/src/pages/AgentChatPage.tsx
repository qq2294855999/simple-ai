import { Button, Card, Empty, Input, List, Select, Space, Tag, Timeline, Tooltip, Typography } from "antd";
import { CloseOutlined, PlusOutlined, SendOutlined } from "@ant-design/icons";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { AgentChatApi } from "../api/agentChatApi";
import { AgentDefinitionApi } from "../api/agentDefinitionApi";
import { AiModelApi } from "../api/aiModelApi";
import { RestrictedMarkdownComponent } from "../components/agentChat/RestrictedMarkdownComponent";
import type { AgentChatMessageDto, AgentChatProgressEventDto, AgentChatSessionDto, SendAgentChatMessageRequestDto } from "../dto/agentChat/AgentChatDto";
import type { AgentDefinitionPageDto } from "../dto/agentDefinition/AgentDefinitionDto";
import type { AiModelResponseDto } from "../dto/aiModel/AiModelDto";
import { usePreventDoubleClickHook } from "../hooks/usePreventDoubleClickHook";
import { ToastUtil } from "../utils/ToastUtil";
import { appendAssistantToken, isAgentChatMessageEvent, replaceFinalMessage } from "../utils/agentChatStreamUtil";

const maxTimelineEventCount = 300;

/**
 * 智能体人机对话页面。
 *
 * @author qty
 */
export function AgentChatPage() {
  const [agents, setAgents] = useState<AgentDefinitionPageDto[]>([]);
  const [selectedAgentId, setSelectedAgentId] = useState<string>();
  const [models, setModels] = useState<AiModelResponseDto[]>([]);
  const [selectedModelId, setSelectedModelId] = useState<string>();
  const [sessions, setSessions] = useState<AgentChatSessionDto[]>([]);
  const [selectedSessionId, setSelectedSessionId] = useState<string>();
  const [messages, setMessages] = useState<AgentChatMessageDto[]>([]);
  const [events, setEvents] = useState<AgentChatProgressEventDto[]>([]);
  const [input, setInput] = useState("");
  const streamAbortControllerRef = useRef<AbortController | undefined>(undefined);

  const selectedSession = useMemo(
    () => sessions.find(session => session.id === selectedSessionId),
    [selectedSessionId, sessions]
  );

  const loadAgents = useCallback(async () => {
    const page = await AgentDefinitionApi.listAll();
    setAgents(page.records);
  }, []);

  const loadModels = useCallback(async (agentId: string) => {
    const result = await AiModelApi.available(agentId);
    setModels(result);
  }, []);

  const loadSessions = useCallback(async (agentId: string) => {
    const result = await AgentChatApi.findSessions(agentId);
    setSessions(result);
  }, []);

  const loadMessages = useCallback(async (sessionId: string) => {
    const result = await AgentChatApi.findMessages(sessionId);
    setMessages(result);
  }, []);

  useEffect(() => {
    void loadAgents().catch(() => setAgents([]));
  }, [loadAgents]);

  useEffect(() => {
    setSessions([]);
    setMessages([]);
    setEvents([]);
    setSelectedSessionId(undefined);
    setModels([]);
    setSelectedModelId(undefined);

    // 切换智能体后加载其持久化会话历史和可用模型
    if (selectedAgentId) {
      void loadSessions(selectedAgentId).catch(() => setSessions([]));
      void loadModels(selectedAgentId).catch(() => setModels([]));
    }
  }, [loadSessions, loadModels, selectedAgentId]);

  const handleSelectSession = useCallback((sessionId: string) => {
    setSelectedSessionId(sessionId);
    setEvents([]);
    void loadMessages(sessionId).catch(() => setMessages([]));
  }, [loadMessages]);

  const { onClick: handleCreateSession, loading: creating } = usePreventDoubleClickHook(async () => {
    if (!selectedAgentId) {
      ToastUtil.error("请先选择智能体");
      return;
    }
    const session = await AgentChatApi.createSession({ agentId: selectedAgentId });
    setSessions(previousSessions => [session, ...previousSessions]);
    setSelectedSessionId(session.id);
    setMessages([]);
    setEvents([]);
  });

  const handleProgress = useCallback((event: AgentChatProgressEventDto) => {
    if (!isAgentChatMessageEvent(event.eventType)) {
      setEvents(previousEvents => [...previousEvents, event].slice(-maxTimelineEventCount));
    }

    // token 仅进入对话消息流，绝不显示为执行轨迹文本
    if (event.eventType === "AI_TOKEN") {
      setMessages(previousMessages => appendAssistantToken(previousMessages, event));
      return;
    }
    if (event.eventType === "MESSAGE_COMPLETED" || event.eventType === "CHAT_FAILED") {
      setMessages(previousMessages => replaceFinalMessage(previousMessages, event));
    }
  }, []);

  const { onClick: handleSend, loading: sending } = usePreventDoubleClickHook(async () => {
    if (!selectedSessionId || !input.trim()) {
      ToastUtil.error("请选择会话并输入消息");
      return;
    }
    const content = input.trim();
    const abortController = new AbortController();
    streamAbortControllerRef.current = abortController;
    setInput("");
    setEvents([]);
    setMessages(previousMessages => [...previousMessages, buildOptimisticUserMessage(content)]);
    const request: SendAgentChatMessageRequestDto = { sessionId: selectedSessionId, content };

    // 用户显式选择模型时传递 modelId
    if (selectedModelId) {
      request.modelId = selectedModelId;
    }
    try {
      await AgentChatApi.sendStream(request, handleProgress, abortController.signal);
    } catch (error) {
      if (abortController.signal.aborted) {
        ToastUtil.error("已停止等待聊天响应");
      } else {
        const message = error instanceof Error ? error.message : "流式聊天请求失败";
        setMessages(previousMessages => replaceFinalMessage(previousMessages, buildChatFailureEvent(message)));
        ToastUtil.error(message);
      }
    } finally {
      if (streamAbortControllerRef.current === abortController) {
        streamAbortControllerRef.current = undefined;
      }
      await loadMessages(selectedSessionId);
      if (selectedAgentId) {
        await loadSessions(selectedAgentId);
      }
    }
  });

  const handleCancelStream = useCallback(() => {
    streamAbortControllerRef.current?.abort();
  }, []);

  return (
    <div>
      <Typography.Title level={3}>人机对话与调度轨迹</Typography.Title>
      <div className="simple-search-panel">
        <Space wrap>
          <Select
            placeholder="选择智能体"
            value={selectedAgentId}
            style={{ width: 240, height: 36 }}
            onChange={setSelectedAgentId}
            options={agents.map(agent => ({ label: agent.name, value: agent.id }))}
          />
          <Select
            placeholder="选择模型（可选）"
            value={selectedModelId}
            allowClear
            style={{ width: 260, height: 36 }}
            onChange={setSelectedModelId}
            disabled={!selectedAgentId || models.length === 0}
            options={models.map(model => ({
              label: `${model.providerName} · ${model.modelName}`,
              value: model.id
            }))}
          />
          <Button type="primary" icon={<PlusOutlined />} loading={creating} disabled={!selectedAgentId} onClick={handleCreateSession}>新建对话</Button>
        </Space>
      </div>
      <div className="agent-chat-layout">
        <Card title="历史会话" className="agent-chat-sessions">
          <List
            locale={{ emptyText: <Empty description="请选择智能体后新建对话" image={Empty.PRESENTED_IMAGE_SIMPLE} /> }}
            dataSource={sessions}
            renderItem={session => (
              <List.Item className={session.id === selectedSessionId ? "agent-chat-session-active" : ""} onClick={() => handleSelectSession(session.id)}>
                <List.Item.Meta title={session.sessionName} description={session.agentName} />
              </List.Item>
            )}
          />
        </Card>
        <Card title={selectedSession ? `对话：${selectedSession.sessionName}` : "对话消息"} className="agent-chat-messages">
          <div className="agent-chat-message-list">
            {messages.length === 0 ? <Empty description="选择或创建会话后开始对话" /> : messages.map(message => (
              <div key={`${message.id}-${message.sequenceNo}`} className={`agent-chat-message agent-chat-message-${message.role.toLowerCase()}`}>
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                  <Typography.Text strong>{message.role === "USER" ? "你" : message.role === "ASSISTANT" ? "AI" : "系统"}</Typography.Text>
                  {message.role === "ASSISTANT" && message.providerName && (
                    <Tooltip title="本次调用实际使用的供应商·模型">
                      <Tag color="geekblue">{message.providerName} · {message.modelCode}</Tag>
                    </Tooltip>
                  )}
                </div>
                {message.contentFormat === "RESTRICTED_MARKDOWN"
                  ? <RestrictedMarkdownComponent content={message.content} />
                  : <Typography.Paragraph style={{ whiteSpace: "pre-wrap", marginBottom: 0 }}>{message.content}</Typography.Paragraph>}
              </div>
            ))}
          </div>
          <Input.TextArea value={input} disabled={!selectedSessionId} onChange={event => setInput(event.target.value)} placeholder="输入问题，AI 的真实调度过程会在右侧时间线展示" autoSize={{ minRows: 3, maxRows: 6 }} />
          <div className="agent-chat-send-bar">
            <Typography.Text type="secondary">最终回复使用受限 Markdown 安全渲染</Typography.Text>
            <Space>
              {sending && <Button icon={<CloseOutlined />} onClick={handleCancelStream}>停止等待</Button>}
              <Button type="primary" icon={<SendOutlined />} loading={sending} disabled={!selectedSessionId} onClick={handleSend}>发送</Button>
            </Space>
          </div>
        </Card>
        <Card title="执行轨迹" className="agent-chat-timeline">
          <Timeline
            items={events.map((event, index) => ({
              key: `${event.taskId}-${event.eventType}-${index}`,
              color: event.eventType.includes("FAILED") ? "red" : "blue",
              children: <div><Tag>{event.eventType}</Tag><Typography.Text>{event.message}</Typography.Text>{event.stepName && <Typography.Paragraph type="secondary">{event.stepName}</Typography.Paragraph>}{event.failureReason && <Typography.Paragraph type="danger">{event.failureReason}</Typography.Paragraph>}</div>
            }))}
          />
          {events.length === 0 && <Empty description="调度事件将以结构化时间线展示" image={Empty.PRESENTED_IMAGE_SIMPLE} />}
        </Card>
      </div>
    </div>
  );
}

function buildOptimisticUserMessage(content: string): AgentChatMessageDto {
  return { id: `local-user-${Date.now()}`, taskId: "", role: "USER", content, contentFormat: "PLAIN_TEXT", sequenceNo: Date.now(), createTime: "", providerName: "", modelCode: "" };
}

function buildChatFailureEvent(message: string): AgentChatProgressEventDto {
  return {
    taskId: "",
    sessionId: "",
    eventType: "CHAT_FAILED",
    stepId: "",
    stepName: "",
    execStatus: "FAILED",
    message: "聊天请求失败",
    payload: "",
    completed: true,
    failureReason: message
  };
}
