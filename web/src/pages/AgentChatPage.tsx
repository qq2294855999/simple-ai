import { Button, Card, Checkbox, Empty, Input, List, Popconfirm, Select, Space, Spin, Tag, Timeline, Tooltip, Typography } from "antd";
import { CloseOutlined, DeleteOutlined, LoadingOutlined, PlusOutlined, RobotOutlined, SendOutlined, UserOutlined } from "@ant-design/icons";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { AgentChatApi } from "../api/agentChatApi";
import { AgentDefinitionApi } from "../api/agentDefinitionApi";
import { AiModelApi } from "../api/aiModelApi";
import { RestrictedMarkdownComponent } from "../components/agentChat/RestrictedMarkdownComponent";
import type { AgentChatMessageDto, AgentChatProgressEventDto, AgentChatSessionDto, AgentChatTrajectoryDto, SendAgentChatMessageRequestDto } from "../dto/agentChat/AgentChatDto";
import type { AgentDefinitionPageDto } from "../dto/agentDefinition/AgentDefinitionDto";
import type { AiModelResponseDto } from "../dto/aiModel/AiModelDto";
import { usePreventDoubleClickHook } from "../hooks/usePreventDoubleClickHook";
import { ToastUtil } from "../utils/ToastUtil";
import { appendAssistantToken, isAgentChatMessageEvent, replaceFinalMessage } from "../utils/agentChatStreamUtil";

const maxTimelineEventCount = 300;
const maxSessionNameLength = 14;
const messagePageSize = 50;

/** 截断过长的会话标题，超过 maxLen 字符用 ... 折叠。 */
function truncateSessionName(name: string, maxLen: number = maxSessionNameLength): string {
  if (name.length <= maxLen) {
    return name;
  }
  return name.substring(0, maxLen) + "...";
}

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
  const [trajectories, setTrajectories] = useState<AgentChatTrajectoryDto[]>([]);
  const [input, setInput] = useState("");
  const [selectedSessionIds, setSelectedSessionIds] = useState<string[]>([]);
  const [showBatchSelect, setShowBatchSelect] = useState(false);
  const [aiThinking, setAiThinking] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const streamAbortControllerRef = useRef<AbortController | undefined>(undefined);
  const messageListRef = useRef<HTMLDivElement>(null);
  const timelineBodyRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLTextAreaElement>(null);
  const prevScrollHeightRef = useRef(0);

  /** 消息列表自动滚动到底部。 */
  const scrollMessagesToBottom = useCallback(() => {
    requestAnimationFrame(() => {
      if (messageListRef.current) {
        messageListRef.current.scrollTop = messageListRef.current.scrollHeight;
      }
    });
  }, []);

  /** 执行轨迹自动滚动到最新。 */
  const scrollTimelineToEnd = useCallback(() => {
    requestAnimationFrame(() => {
      if (timelineBodyRef.current) {
        timelineBodyRef.current.scrollTop = timelineBodyRef.current.scrollHeight;
      }
    });
  }, []);

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
    const result = await AgentChatApi.findMessagesPage(sessionId, messagePageSize, Number.MAX_SAFE_INTEGER);
    setMessages(result);
    setHasMore(result.length >= messagePageSize);
  }, []);

  /** 上滑加载更早的消息。 */
  const loadMoreMessages = useCallback(async () => {
    if (!selectedSessionId || !hasMore || loadingMore) {
      return;
    }

    // 取当前最早消息的序号作为分页游标
    const firstSeq = messages.length > 0 ? messages[0].sequenceNo : Number.MAX_SAFE_INTEGER;
    setLoadingMore(true);

    // 记录当前滚动高度用于加载后保持位置
    prevScrollHeightRef.current = messageListRef.current?.scrollHeight || 0;
    try {
      const olderMessages = await AgentChatApi.findMessagesPage(selectedSessionId, messagePageSize, firstSeq);
      setHasMore(olderMessages.length >= messagePageSize);

      // 将更早的消息插入数组头部
      setMessages(previousMessages => [...olderMessages, ...previousMessages]);
    } catch {
      ToastUtil.error("加载历史消息失败");
    } finally {
      setLoadingMore(false);
    }
  }, [selectedSessionId, hasMore, loadingMore, messages]);

  // 加载更多后恢复滚动位置，避免页面跳动
  useEffect(() => {
    if (loadingMore || !messageListRef.current || prevScrollHeightRef.current === 0) {
      return;
    }
    const newScrollHeight = messageListRef.current.scrollHeight;
    messageListRef.current.scrollTop = newScrollHeight - prevScrollHeightRef.current;
    prevScrollHeightRef.current = 0;
  }, [messages, loadingMore]);

  /** 加载会话的历史执行轨迹。 */
  const loadTrajectories = useCallback(async (sessionId: string) => {
    try {
      const result = await AgentChatApi.findTrajectory(sessionId);
      setTrajectories(result);
    } catch {
      setTrajectories([]);
    }
  }, []);

  useEffect(() => {
    void loadAgents().catch(() => setAgents([]));
  }, [loadAgents]);

  useEffect(() => {
    setSessions([]);
    setMessages([]);
    setEvents([]);
    setTrajectories([]);
    setSelectedSessionId(undefined);
    setSelectedSessionIds([]);
    setModels([]);
    setSelectedModelId(undefined);

    // 切换智能体后加载其持久化会话历史和可用模型
    if (selectedAgentId) {
      void loadSessions(selectedAgentId).catch(() => setSessions([]));
      void loadModels(selectedAgentId).catch(() => setModels([]));
    }
  }, [loadSessions, loadModels, selectedAgentId]);

  // 消息或思考状态变化时自动滚动到底部
  useEffect(() => {
    scrollMessagesToBottom();
  }, [messages, aiThinking, scrollMessagesToBottom]);

  // 执行轨迹事件变化时自动滚动到最新
  useEffect(() => {
    scrollTimelineToEnd();
  }, [events, trajectories, scrollTimelineToEnd]);

  const handleSelectSession = useCallback((sessionId: string) => {
    setSelectedSessionId(sessionId);
    setEvents([]);
    setTrajectories([]);
    setHasMore(true);
    setLoadingMore(false);
    void loadMessages(sessionId).catch(() => setMessages([]));

    // 切换会话时加载历史执行轨迹
    void loadTrajectories(sessionId);
  }, [loadMessages, loadTrajectories]);

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
    setTrajectories([]);
  });

  /** 删除单个会话。 */
  const { onClick: handleDeleteSession, loading: deletingSession } = usePreventDoubleClickHook(async (sessionId: string) => {
    await AgentChatApi.deleteSession(sessionId);
    setSessions(previousSessions => previousSessions.filter(s => s.id !== sessionId));

    // 删除的是当前选中会话时清空消息和轨迹
    if (sessionId === selectedSessionId) {
      setSelectedSessionId(undefined);
      setMessages([]);
      setEvents([]);
      setTrajectories([]);
    }
    ToastUtil.success("删除成功");
  });

  /** 批量删除会话。 */
  const { onClick: handleBatchDelete, loading: batchDeleting } = usePreventDoubleClickHook(async () => {
    // 批量删除前过滤已选中的会话ID
    await AgentChatApi.deleteSessions(selectedSessionIds);
    setSessions(previousSessions => previousSessions.filter(s => !selectedSessionIds.includes(s.id)));

    // 删除的包含当前选中会话时清空消息和轨迹
    if (selectedSessionId && selectedSessionIds.includes(selectedSessionId)) {
      setSelectedSessionId(undefined);
      setMessages([]);
      setEvents([]);
      setTrajectories([]);
    }
    setSelectedSessionIds([]);
    ToastUtil.success("批量删除成功");
  });

  /** 全选/取消全选会话。 */
  const handleSelectAllSessions = useCallback((checked: boolean) => {
    if (checked) {
      setSelectedSessionIds(sessions.map(s => s.id));
    } else {
      setSelectedSessionIds([]);
    }
  }, [sessions]);

  /** 单个会话勾选切换。 */
  const handleToggleSessionSelect = useCallback((sessionId: string, checked: boolean) => {
    if (checked) {
      setSelectedSessionIds(previousIds => [...previousIds, sessionId]);
    } else {
      setSelectedSessionIds(previousIds => previousIds.filter(id => id !== sessionId));
    }
  }, []);

  const handleProgress = useCallback((event: AgentChatProgressEventDto) => {
    if (!isAgentChatMessageEvent(event.eventType)) {
      setEvents(previousEvents => [...previousEvents, event].slice(-maxTimelineEventCount));
    }

    // token 仅进入对话消息流，绝不显示为执行轨迹文本
    if (event.eventType === "AI_TOKEN") {
      // 首 token 到达时结束思考动画
      setAiThinking(false);
      setMessages(previousMessages => appendAssistantToken(previousMessages, event));
      return;
    }
    if (event.eventType === "MESSAGE_COMPLETED" || event.eventType === "CHAT_FAILED") {
      setAiThinking(false);
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
    setTrajectories([]);
    setAiThinking(true);
    setMessages(previousMessages => [...previousMessages, buildOptimisticUserMessage(content)]);
    scrollMessagesToBottom();
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
        setAiThinking(false);
        setMessages(previousMessages => replaceFinalMessage(previousMessages, buildChatFailureEvent(message)));
        ToastUtil.error(message);
      }
    } finally {
      setAiThinking(false);
      if (streamAbortControllerRef.current === abortController) {
        streamAbortControllerRef.current = undefined;
      }
      await loadMessages(selectedSessionId);
      if (selectedAgentId) {
        await loadSessions(selectedAgentId);
      }

      // 发送完成后刷新轨迹，结束后聚焦输入框
      await loadTrajectories(selectedSessionId);
      inputRef.current?.focus();
    }
  });

  const handleCancelStream = useCallback(() => {
    streamAbortControllerRef.current?.abort();
  }, []);

  /** 获取消息角色前缀文本。用户显示"我"，AI 显示智能体名称。 */
  const getMessageRolePrefix = useCallback((role: string) => {
    if (role === "USER") {
      return "我";
    }
    if (role === "ASSISTANT") {
      return selectedSession?.agentName || "AI";
    }
    return "系统";
  }, [selectedSession]);

  /** 判断是否全部会话已选中。 */
  const allSessionsSelected = useMemo(
    () => sessions.length > 0 && selectedSessionIds.length === sessions.length,
    [sessions, selectedSessionIds]
  );

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
        <Card
          title="历史会话"
          className="agent-chat-sessions"
          extra={
            <Space>
              {!showBatchSelect ? (
                <Button size="small" disabled={sessions.length === 0} onClick={() => { setShowBatchSelect(true); }}>批量管理</Button>
              ) : (
                <>
                  <Button size="small" onClick={() => { setShowBatchSelect(false); setSelectedSessionIds([]); }}>取消选择</Button>
                  {selectedSessionIds.length > 0 && (
                    <Popconfirm title="确认批量删除所选会话？" onConfirm={() => { void handleBatchDelete(); }}>
                      <Button size="small" danger icon={<DeleteOutlined />} loading={batchDeleting}>删除所选 ({selectedSessionIds.length})</Button>
                    </Popconfirm>
                  )}
                </>
              )}
            </Space>
          }
        >
          {showBatchSelect && sessions.length > 0 && (
            <div style={{ padding: "0 0 8px 0" }}>
              <Checkbox
                checked={allSessionsSelected}
                indeterminate={selectedSessionIds.length > 0 && !allSessionsSelected}
                onChange={event => handleSelectAllSessions(event.target.checked)}
              >
                全选
              </Checkbox>
            </div>
          )}
          <List
            locale={{ emptyText: <Empty description="请选择智能体后新建对话" image={Empty.PRESENTED_IMAGE_SIMPLE} /> }}
            dataSource={sessions}
            renderItem={session => (
              <List.Item
                className={session.id === selectedSessionId ? "agent-chat-session-active" : ""}
                actions={[
                  <Popconfirm
                    key="delete"
                    title="确认删除此会话？"
                    onConfirm={() => { void handleDeleteSession(session.id); }}
                  >
                    <Button size="small" danger icon={<DeleteOutlined />} loading={deletingSession} />
                  </Popconfirm>
                ]}
              >
                {showBatchSelect && (
                  <Checkbox
                    checked={selectedSessionIds.includes(session.id)}
                    style={{ marginRight: 8 }}
                    onChange={event => handleToggleSessionSelect(session.id, event.target.checked)}
                  />
                )}
                        <div style={{ cursor: "pointer", flex: 1, minWidth: 0 }} onClick={() => handleSelectSession(session.id)}>
                          <Tooltip title={session.sessionName}>
                            <List.Item.Meta
                              title={truncateSessionName(session.sessionName)}
                              description={session.agentName}
                            />
                          </Tooltip>
                        </div>
              </List.Item>
            )}
          />
        </Card>
        <Card title={selectedSession ? `对话：${truncateSessionName(selectedSession.sessionName)}` : "对话消息"} className="agent-chat-messages">
          <div
            className="agent-chat-message-list"
            ref={messageListRef}
            onScroll={event => {
              // 滚动到顶部附近时加载更早的消息
              const target = event.currentTarget;
              if (target.scrollTop < 50 && hasMore && !loadingMore && selectedSessionId) {
                void loadMoreMessages();
              }
            }}
          >
            {loadingMore && (
              <div style={{ textAlign: "center", padding: "8px 0" }}>
                <Spin indicator={<LoadingOutlined style={{ fontSize: 18 }} spin />} />
                <Typography.Text type="secondary" style={{ marginLeft: 8 }}>加载历史消息…</Typography.Text>
              </div>
            )}
            {messages.length === 0 && !aiThinking ? <Empty description="选择或创建会话后开始对话" /> : messages.map(message => (
              <div key={`${message.id}-${message.sequenceNo}`} className={`agent-chat-message agent-chat-message-${message.role.toLowerCase()}`}>
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 8 }}>
                  <Space>
                    {message.role === "USER" ? <UserOutlined style={{ color: "#1677ff" }} /> : <RobotOutlined style={{ color: "#52c41a" }} />}
                    <Typography.Text strong>{getMessageRolePrefix(message.role)}</Typography.Text>
                  </Space>
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
            {aiThinking && (
              <div className="agent-chat-message agent-chat-message-assistant" style={{ display: "flex", alignItems: "center", gap: 10 }}>
                <Spin indicator={<LoadingOutlined style={{ fontSize: 20, color: "#52c41a" }} spin />} />
                <Typography.Text type="secondary">AI 正在思考中…</Typography.Text>
              </div>
            )}
          </div>
          <Input.TextArea
            ref={inputRef as React.Ref<any>}
            value={input}
            disabled={!selectedSessionId}
            onChange={event => setInput(event.target.value)}
            onKeyDown={event => {
              // 回车发送，Shift+回车换行
              if (event.key === "Enter" && !event.shiftKey) {
                event.preventDefault();
                if (!sending && input.trim()) { void handleSend(); }
              }
            }}
            placeholder="输入问题，Enter 发送，Shift+Enter 换行"
            autoSize={{ minRows: 3, maxRows: 6 }}
          />
          <div className="agent-chat-send-bar">
            <Typography.Text type="secondary">{sending ? "AI 正在思考中…" : "Enter 发送 · Shift+Enter 换行"}</Typography.Text>
            <Space>
              {sending && <Button icon={<CloseOutlined />} onClick={handleCancelStream}>停止等待</Button>}
              <Button type="primary" icon={<SendOutlined />} loading={sending} disabled={!selectedSessionId || !input.trim()} onClick={handleSend}>发送</Button>
            </Space>
          </div>
        </Card>
        <Card title="执行轨迹" className="agent-chat-timeline" bodyStyle={{ padding: "12px 16px" }}>
          <div className="agent-chat-timeline-body" ref={timelineBodyRef}>
          <Timeline
            items={[
              // 历史轨迹（持久化任务详情）
              ...trajectories.map(trajectory => ({
                key: `traj-${trajectory.id}`,
                color: trajectory.execStatus === "FAILED" ? ("red" as const) : ("blue" as const),
                children: (
                  <div>
                    <Tag>{trajectory.stepType}</Tag>
                    <Typography.Text>{trajectory.taskName}</Typography.Text>
                    {trajectory.providerName && (
                      <Typography.Paragraph type="secondary">{trajectory.providerName} · {trajectory.modelCode}</Typography.Paragraph>
                    )}
                  </div>
                )
              })),
              // 当前实时事件
              ...events.map((event, index) => ({
                key: `${event.taskId}-${event.eventType}-${index}`,
                color: event.eventType.includes("FAILED") ? ("red" as const) : ("blue" as const),
                children: (
                  <div>
                    <Tag>{event.eventType}</Tag>
                    <Typography.Text>{event.message}</Typography.Text>
                    {event.stepName && <Typography.Paragraph type="secondary">{event.stepName}</Typography.Paragraph>}
                    {event.failureReason && <Typography.Paragraph type="danger">{event.failureReason}</Typography.Paragraph>}
                  </div>
                )
              }))
            ]}
          />
          {trajectories.length === 0 && events.length === 0 && <Empty description="调度事件将以结构化时间线展示" image={Empty.PRESENTED_IMAGE_SIMPLE} />}
          </div>
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
