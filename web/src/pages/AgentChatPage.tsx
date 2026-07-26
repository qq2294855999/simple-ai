import {Button, Card, Checkbox, Collapse, Empty, Input, List, Popconfirm, Select, Space, Spin, Tag, Tooltip, Typography} from "antd";
import {
    BulbOutlined,
    CloseOutlined,
    DeleteOutlined,
    LoadingOutlined,
    PlusOutlined,
    RightOutlined,
    RobotOutlined,
    SendOutlined,
    ThunderboltOutlined,
    UserOutlined
} from "@ant-design/icons";
import {useCallback, useEffect, useMemo, useRef, useState} from "react";
import {useScrollSelect} from "../hooks/useScrollSelect";
import {AgentChatApi} from "../api/agentChatApi";
import {AgentClientApi} from "../api/agentClientApi";
import {AgentDefinitionApi} from "../api/agentDefinitionApi";
import {AiModelApi} from "../api/aiModelApi";
import {RestrictedMarkdownComponent} from "../components/agentChat/RestrictedMarkdownComponent";
import type {
    AgentChatExecutionEventDto,
    AgentChatMessageDto,
    AgentChatProgressEventDto,
    AgentChatSessionDto,
    SendAgentChatMessageRequestDto
} from "../dto/agentChat/AgentChatDto";
import type {AgentDefinitionPageDto} from "../dto/agentDefinition/AgentDefinitionDto";
import type {AiModelResponseDto} from "../dto/aiModel/AiModelDto";
import {usePreventDoubleClickHook} from "../hooks/usePreventDoubleClickHook";
import {ToastUtil} from "../utils/ToastUtil";
import {
    appendAssistantToken,
    appendProgressEvent,
    appendThinkingToken,
    createProgressBubble,
    createReplyBubble,
    createThinkingBubble,
    finalizeProgressBubble,
    finalizeThinkingBubble,
    replaceFinalMessage,
    stripProtocolJson
} from "../utils/agentChatStreamUtil";

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
    // 智能体下拉：滚动分页加载
    const {
        options: agents,
        loading: agentsLoading,
        onPopupScroll: onAgentsPopupScroll
    } = useScrollSelect<AgentDefinitionPageDto>(
        (page, size) => AgentDefinitionApi.page({current: page, size}),
        20,
        []
    );

  const [selectedAgentId, setSelectedAgentId] = useState<string>();
  const [models, setModels] = useState<AiModelResponseDto[]>([]);
  const [selectedModelId, setSelectedModelId] = useState<string>();

    // 客户端下拉：滚动分页加载
    const {
        options: clientsRaw,
        loading: clientsLoading,
        onPopupScroll: onClientsPopupScroll
    } = useScrollSelect<{ id: string; clientName: string }>(
        (page, size) => AgentClientApi.page({current: page, size}),
        20,
        []
    );

    const clients = useMemo(
        () => clientsRaw.map(c => ({id: c.id, clientName: c.clientName})),
        [clientsRaw]
    );

    const [selectedClientId, setSelectedClientId] = useState<string>();
  const [sessions, setSessions] = useState<AgentChatSessionDto[]>([]);
  const [selectedSessionId, setSelectedSessionId] = useState<string>();
  const [messages, setMessages] = useState<AgentChatMessageDto[]>([]);
  const [input, setInput] = useState("");
  const [selectedSessionIds, setSelectedSessionIds] = useState<string[]>([]);
  const [showBatchSelect, setShowBatchSelect] = useState(false);
  const [aiThinking, setAiThinking] = useState(false);
  const [hasMore, setHasMore] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const streamAbortControllerRef = useRef<AbortController | undefined>(undefined);
  const messageListRef = useRef<HTMLDivElement>(null);
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

  const selectedSession = useMemo(
    () => sessions.find(session => session.id === selectedSessionId),
    [selectedSessionId, sessions]
  );

  const loadModels = useCallback(async (agentId: string) => {
    const result = await AiModelApi.available(agentId);
    setModels(result);
  }, []);

    const loadSessions = useCallback(async (agentId: string, modelId?: string, clientId?: string) => {
        const result = await AgentChatApi.findSessions(agentId, modelId, clientId);
    setSessions(result);
  }, []);

    /** 根据会话中的 modelId/clientId 回显 Select 值。 */
    const restoreSessionContext = useCallback((session: AgentChatSessionDto) => {
        if (session.modelId) {
            setSelectedModelId(session.modelId);
        }
        if (session.clientId) {
            setSelectedClientId(session.clientId);
        }
  }, []);

    /** 渲染会话描述信息（智能体 + 模型 + 客户端）。 */
    const renderSessionDescription = useCallback((session: AgentChatSessionDto): React.ReactNode => {
        const parts: string[] = [];
        if (session.agentName) {
            parts.push(session.agentName);
        }

        // 查找模型名称
        if (session.modelId) {
            const model = models.find(m => m.id === session.modelId);
            if (model) {
                parts.push(`${model.providerName} · ${model.modelName}`);
            } else {
                parts.push(`模型: ${session.modelId.substring(0, 8)}...`);
            }
        }

        // 查找客户端名称
        if (session.clientId) {
            const client = clients.find(c => c.id === session.clientId);
            if (client) {
                parts.push(client.clientName);
            } else {
                parts.push(`客户端: ${session.clientId.substring(0, 8)}...`);
            }
        }

        return parts.length > 0 ? parts.join(" · ") : session.agentName;
    }, [models, clients]);

  const loadMessages = useCallback(async (sessionId: string) => {
      const rawMessages = await AgentChatApi.findMessagesPage(sessionId, messagePageSize, Number.MAX_SAFE_INTEGER);

      // 为后端消息补齐默认字段（旧数据没有 bubbleType / thinkingContent）
      const normalizedMessages: AgentChatMessageDto[] = rawMessages.map(msg => ({
          ...msg,
          thinkingContent: msg.thinkingContent ?? "",
          thinkingContentFormat: msg.thinkingContentFormat ?? "PLAIN_TEXT",
          bubbleType: (msg.bubbleType ?? "NORMAL") as AgentChatMessageDto["bubbleType"]
      }));

      // 历史回放：ASSISTANT 消息如果有 executionEvents 或 thinkingContent，
      // 在 ASSISTANT 消息之前合成 PROGRESS / THINKING 独立气泡（保持时序：进度 → 思考 → 回复）
      const composed: AgentChatMessageDto[] = [];
      normalizedMessages.forEach(msg => {
          if (msg.role === "ASSISTANT" && msg.bubbleType === "NORMAL") {
              // executionEvents 已存在（旧数据）时，单独合成一个 PROGRESS 气泡放在回复之前
              if (msg.executionEvents && msg.executionEvents.length > 0) {
                  composed.push({
                      id: `composed-progress-${msg.id}`,
                      taskId: msg.taskId,
                      turnId: msg.turnId,
                      role: "ASSISTANT",
                      content: `✅ 执行详情 (${msg.executionEvents.length} 步)`,
                      contentFormat: "PLAIN_TEXT",
                      sequenceNo: msg.sequenceNo - 2,
                      createTime: msg.createTime,
                      providerName: "",
                      modelCode: "",
                      executionEvents: msg.executionEvents,
                      thinkingContent: "",
                      thinkingContentFormat: "PLAIN_TEXT",
                      bubbleType: "PROGRESS"
                  });
              }

              // thinkingContent 有值时，单独合成一个 THINKING 气泡放在回复之前
              if (msg.thinkingContent && msg.thinkingContent.trim().length > 0) {
                  const charCount = msg.thinkingContent.trim().length;
                  composed.push({
                      id: `composed-thinking-${msg.id}`,
                      taskId: msg.taskId,
                      turnId: msg.turnId,
                      role: "ASSISTANT",
                      content: `✅ 思考过程 (约 ${charCount} 字)`,
                      contentFormat: "PLAIN_TEXT",
                      sequenceNo: msg.sequenceNo - 1,
                      createTime: msg.createTime,
                      providerName: "",
                      modelCode: "",
                      executionEvents: [],
                      thinkingContent: msg.thinkingContent,
                      thinkingContentFormat: msg.thinkingContentFormat,
                      bubbleType: "THINKING"
                  });
              }

              // 回复气泡本身保持原样（executionEvents 字段保留用于锚点提示，不会再内嵌折叠）
              composed.push(msg);
          } else {
              composed.push(msg);
          }
      });

      setMessages(composed);
      setHasMore(normalizedMessages.length >= messagePageSize);
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

  useEffect(() => {
      // 智能体和客户端下拉已由 useScrollSelect 自动加载
  }, []);

    // 切换智能体时重置模型和会话，并加载新智能体的可用模型
  useEffect(() => {
    setSessions([]);
    setMessages([]);
    setSelectedSessionId(undefined);
    setSelectedSessionIds([]);
    setModels([]);
    setSelectedModelId(undefined);

    if (selectedAgentId) {
      void loadModels(selectedAgentId).catch(() => setModels([]));
    }
  }, [loadModels, selectedAgentId]);

    // 智能体、模型或客户端变化时重新加载会话列表
    useEffect(() => {
        if (selectedAgentId) {
            void loadSessions(selectedAgentId, selectedModelId, selectedClientId).catch(() => setSessions([]));
        }
    }, [loadSessions, selectedAgentId, selectedModelId, selectedClientId]);

  // 消息或思考状态变化时自动滚动到底部
  useEffect(() => {
    scrollMessagesToBottom();
  }, [messages, aiThinking, scrollMessagesToBottom]);

  const handleSelectSession = useCallback((sessionId: string) => {
    setSelectedSessionId(sessionId);
    setHasMore(true);
    setLoadingMore(false);
    void loadMessages(sessionId).catch(() => setMessages([]));

      // 回显会话的模型和客户端配置
      const session = sessions.find(s => s.id === sessionId);
      if (session) {
          restoreSessionContext(session);
      }
  }, [loadMessages, sessions, restoreSessionContext]);

  const { onClick: handleCreateSession, loading: creating } = usePreventDoubleClickHook(async () => {
    if (!selectedAgentId) {
      ToastUtil.error("请先选择智能体");
      return;
    }
      if (!selectedModelId) {
          ToastUtil.error("请选择模型");
          return;
      }
      if (!selectedClientId) {
          ToastUtil.error("请选择客户端");
          return;
      }
      const session = await AgentChatApi.createSession({
          agentId: selectedAgentId,
          modelId: selectedModelId,
          clientId: selectedClientId
      });
    setSessions(previousSessions => [session, ...previousSessions]);
    setSelectedSessionId(session.id);
    setMessages([]);
  });

  /** 删除单个会话。 */
  const { onClick: handleDeleteSession, loading: deletingSession } = usePreventDoubleClickHook(async (sessionId: string) => {
    await AgentChatApi.deleteSession(sessionId);
    setSessions(previousSessions => previousSessions.filter(s => s.id !== sessionId));

    // 删除的是当前选中会话时清空消息和轨迹
    if (sessionId === selectedSessionId) {
      setSelectedSessionId(undefined);
      setMessages([]);
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
      // 思考 token 直接写入 THINKING 气泡 thinkingContent（如果没有该气泡，先跳过等待 MESSAGE_ACCEPTED 占位推送）
      if (event.eventType === "AI_THINKING_TOKEN") {
          setAiThinking(true);
          setMessages(previousMessages => appendThinkingToken(previousMessages, event.payload || "", event.taskId));
          return;
      }

      // AI 输出 token：写入回复气泡 content，同时 aiThinking 标记关闭（说明模型已进入最终答复阶段）
    if (event.eventType === "AI_TOKEN") {
      setAiThinking(false);
      setMessages(previousMessages => appendAssistantToken(previousMessages, event));
      return;
    }

      // 消息被接受时：确保三路占位气泡已存在（进度 → 思考 → 回复）
      if (event.eventType === "MESSAGE_ACCEPTED") {
          setAiThinking(true);
          setMessages(previousMessages => {
              // 先查找是否有 taskId 为空的占位气泡（由 handleSend 预创建），将其 taskId 更新为真实值
              // 避免重复创建气泡导致旧气泡（空 taskId）永远无法被 finalize 而一直转圈
              const hasEmptyTaskIdBubble = previousMessages.some(
                  m => !m.taskId && (m.bubbleType === "PROGRESS" || m.bubbleType === "THINKING"
                      || (m.bubbleType === "NORMAL" && m.role === "ASSISTANT" && m.content === ""))
              );

              let updated = previousMessages;
              if (hasEmptyTaskIdBubble && event.taskId) {
                  // 将所有空 taskId 的占位气泡更新为真实 taskId，后续 finalize 可正确匹配
                  updated = previousMessages.map(m => {
                      if (!m.taskId && m.bubbleType === "PROGRESS") {
                          return {...m, taskId: event.taskId};
                      }
                      if (!m.taskId && m.bubbleType === "THINKING") {
                          return {...m, taskId: event.taskId};
                      }
                      if (!m.taskId && m.bubbleType === "NORMAL" && m.role === "ASSISTANT" && m.content === "") {
                          return {...m, taskId: event.taskId};
                      }
                      return m;
                  });
              }

              // 更新后再次确认是否已有匹配的占位气泡，缺失则补建
              const hasProgress = updated.some(m => m.bubbleType === "PROGRESS" && (!event.taskId || m.taskId === event.taskId));
              const hasThinking = updated.some(m => m.bubbleType === "THINKING" && (!event.taskId || m.taskId === event.taskId));
              const hasReply = updated.some(m => m.id === "streaming-assistant" || (m.bubbleType === "NORMAL" && m.role === "ASSISTANT" && m.content === ""));

              const inserts: AgentChatMessageDto[] = [];
              if (!hasProgress) inserts.push(createProgressBubble(event.taskId, event.sessionId));
              if (!hasThinking) inserts.push(createThinkingBubble(event.taskId, event.sessionId));
              if (!hasReply) inserts.push(createReplyBubble(event.taskId, event.sessionId));

              return inserts.length > 0 ? [...updated, ...inserts] : updated;
          });
          return;
      }

      // 进度事件（非消息流事件）：推入 PROGRESS 气泡
      if (event.eventType !== "MESSAGE_COMPLETED" && event.eventType !== "CHAT_FAILED"
          && event.eventType !== "TASK_COMPLETED" && event.eventType !== "TASK_FAILED") {
          setMessages(previousMessages => appendProgressEvent(previousMessages, event));
          return;
      }

      // 消息完成或任务完成/失败时：分别 finalize 三个气泡，不再把进度合并到回复气泡内部
      if (event.eventType === "MESSAGE_COMPLETED" || event.eventType === "CHAT_FAILED"
          || event.eventType === "TASK_COMPLETED" || event.eventType === "TASK_FAILED") {
      setAiThinking(false);
        setMessages(previousMessages => {
            let updated = previousMessages;
            const progressStatus = (event.eventType === "MESSAGE_COMPLETED" || event.eventType === "TASK_COMPLETED") ? "OK" : "FAILED";

            // 先完成进度气泡（状态折叠）
            updated = finalizeProgressBubble(updated, event.taskId, progressStatus);
            // 完成思考气泡（无内容则整条移除）
            updated = finalizeThinkingBubble(updated, event.taskId);
            // 完成回复气泡（替换最终消息内容）
            updated = replaceFinalMessage(updated, event);

            // 事件失败时，确保回复气泡写入失败原因（CHAT_FAILED/TASK_FAILED）
            if ((event.eventType === "CHAT_FAILED" || event.eventType === "TASK_FAILED") && updated.length > 0) {
                const last = updated[updated.length - 1];
                if (!last.content && event.failureReason) {
                    updated[updated.length - 1] = {...last, role: "SYSTEM_ERROR", content: event.failureReason, contentFormat: "PLAIN_TEXT"};
                }
            }

            return updated;
        });
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
    setAiThinking(true);

      // 发送时预先创建四路占位气泡（按真实时序：用户 → 进度 → 思考 → 回复）
      // 这样进度条从一开始就在用户气泡下方、AI 回复气泡上方，流式和完成后位置一致
      setMessages(previousMessages => [
          ...previousMessages,
          buildOptimisticUserMessage(content),
          createProgressBubble("", selectedSessionId),
          createThinkingBubble("", selectedSessionId),
          createReplyBubble("", selectedSessionId)
      ]);
    scrollMessagesToBottom();

      // 生成唯一幂等键，防止断线重连后产生重复消息
      const idempotencyKey = crypto.randomUUID();

      const request: SendAgentChatMessageRequestDto = {sessionId: selectedSessionId, content, idempotencyKey};

    try {
        // 使用带断线重连的流式发送，网络断开时自动指数退避重试
        await AgentChatApi.sendStreamWithRetry(
            request,
            handleProgress,
            abortController.signal,
            () => AgentChatApi.findMessagesPage(selectedSessionId, messagePageSize, Number.MAX_SAFE_INTEGER)
        );
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
          await loadSessions(selectedAgentId, selectedModelId, selectedClientId);
      }

        // 结束后聚焦输入框
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

    /** 渲染 PROGRESS 进度特殊气泡：⚡虚线框蓝灰色背景 + Collapse Timeline。 */
    const renderProgressBubble = useCallback((message: AgentChatMessageDto): React.ReactNode => {
        const events = message.executionEvents || [];
        const totalSteps = events.length;
        const isFinal = message.id.startsWith("final-progress") || message.id.startsWith("composed-progress");
        const isStreaming = !isFinal && message.id.startsWith("streaming-progress");
        const statusTag = message.content.includes("❌") ? "error"
            : message.content.includes("✅") ? "success" : "processing";
        const collapseTitle = message.content
            ? message.content
            : (isStreaming ? `执行中 (${totalSteps} 步)` : `执行详情 (${totalSteps} 步)`);

        return (
            <div
                key={`${message.id}-${message.sequenceNo}`}
                className="agent-chat-message agent-chat-message-assistant"
                style={{
                    background: "rgba(114,166,255,0.08)",
                    border: "1px dashed #72a6ff",
                    borderRadius: 8
                }}
            >
                <div style={{display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 0}}>
                    <Space>
                        <ThunderboltOutlined style={{color: "#72a6ff"}}/>
                        <Typography.Text strong style={{color: "#1d39c4"}}>
                            执行进度
                        </Typography.Text>
                        {isStreaming ? (
                            <Spin indicator={<LoadingOutlined style={{fontSize: 12, color: "#72a6ff"}} spin/>}/>
                        ) : (
                            <Tag color={statusTag} style={{margin: 0, padding: "0 6px", fontSize: 11}}>
                                {statusTag === "success" ? "已完成" : statusTag === "error" ? "失败" : "处理中"}
                            </Tag>
                        )}
                    </Space>
                </div>
                <Collapse
                    ghost
                    size="small"
                    style={{marginTop: 4}}
                    defaultActiveKey={isStreaming ? ["progress"] : undefined}
                    expandIcon={({isActive}) => <RightOutlined rotate={isActive ? 90 : 0} style={{color: "#72a6ff"}}/>}
                    items={[{
                        key: "progress",
                        label: <Typography.Text type="secondary" style={{fontSize: 12}}>{collapseTitle}</Typography.Text>,
                        children: renderExecutionEvents(events)
                    }]}
                />
            </div>
        );
    }, []);

    /** 渲染 THINKING 思考特殊气泡：💡紫色虚线框。无内容时返回 null 不渲染。 */
    const renderThinkingBubble = useCallback((message: AgentChatMessageDto): React.ReactNode => {
        const hasContent = (message.thinkingContent || "").trim().length > 0;
        // 流式期间 THINKING 占位气泡即使还没内容，也显示 loading 提示（给用户"AI 在思考"的视觉锚点）
        // final 状态且空内容已在 finalizeThinkingBubble 时整段 splice，此处基本不会命中空内容
        if (!hasContent && message.id.startsWith("final-thinking")) {
            return null;
        }

        const isStreaming = message.id.startsWith("streaming-thinking");
        const charCount = (message.thinkingContent || "").trim().length;
        const collapseTitle = message.content
            ? message.content
            : (hasContent ? `思考过程 (约 ${charCount} 字)` : "思考中…");

        return (
            <div
                key={`${message.id}-${message.sequenceNo}`}
                className="agent-chat-message agent-chat-message-assistant"
                style={{
                    background: "rgba(170,120,255,0.08)",
                    border: "1px dashed #aa78ff",
                    borderRadius: 8
                }}
            >
                <div style={{display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 0}}>
                    <Space>
                        <BulbOutlined style={{color: "#aa78ff"}}/>
                        <Typography.Text strong style={{color: "#531dab"}}>
                            思考过程
                        </Typography.Text>
                        {isStreaming && !hasContent ? (
                            <Spin indicator={<LoadingOutlined style={{fontSize: 12, color: "#aa78ff"}} spin/>}/>
                        ) : null}
                    </Space>
                    {aiThinking && isStreaming && !hasContent ? (
                        <Typography.Text type="secondary" style={{fontSize: 12}}>
                            AI 正在思考中…
                        </Typography.Text>
                    ) : null}
                </div>
                {/* 已有思考内容则折叠展示，避免气泡整体空白突兀 */}
                {hasContent ? (
                    <Collapse
                        ghost
                        size="small"
                        style={{marginTop: 4}}
                        defaultActiveKey={isStreaming ? ["thinking"] : undefined}
                        expandIcon={({isActive}) => <RightOutlined rotate={isActive ? 90 : 0} style={{color: "#aa78ff"}}/>}
                        items={[{
                            key: "thinking",
                            label: <Typography.Text type="secondary" style={{fontSize: 12}}>{collapseTitle}</Typography.Text>,
                            children: (
                                <div style={{paddingLeft: 4}}>
                                    {message.thinkingContentFormat === "RESTRICTED_MARKDOWN"
                                        ? <RestrictedMarkdownComponent content={message.thinkingContent || ""}/>
                                        : <Typography.Paragraph
                                            style={{whiteSpace: "pre-wrap", marginBottom: 0, fontSize: 12, color: "#595959"}}>
                                            {message.thinkingContent}
                                        </Typography.Paragraph>}
                                </div>
                            )
                        }]}
                    />
                ) : null}
            </div>
        );
    }, [aiThinking]);

    /** 渲染正常用户/AI 回复气泡（bubbleType=NORMAL）。 */
    const renderNormalBubble = useCallback((message: AgentChatMessageDto): React.ReactNode => {
        const hasExecutionAnchor = message.executionEvents && message.executionEvents.length > 0;
        return (
            <div
                key={`${message.id}-${message.sequenceNo}`}
                className={`agent-chat-message agent-chat-message-${message.role.toLowerCase()}`}
            >
                <div style={{display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 8}}>
                    <Space>
                        {message.role === "USER"
                            ? <UserOutlined style={{color: "#1677ff"}}/>
                            : <RobotOutlined style={{color: "#52c41a"}}/>}
                        <Typography.Text strong>{getMessageRolePrefix(message.role)}</Typography.Text>
                    </Space>
                    {message.role === "ASSISTANT" && message.providerName && (
                        <Tooltip title="本次调用实际使用的供应商·模型">
                            <Tag color="geekblue">{message.providerName} · {message.modelCode}</Tag>
                        </Tooltip>
                    )}
                </div>
                {message.contentFormat === "RESTRICTED_MARKDOWN"
                    ? <RestrictedMarkdownComponent content={stripProtocolJson(message.content)}/>
                    : <Typography.Paragraph style={{whiteSpace: "pre-wrap", marginBottom: 0}}>
                        {stripProtocolJson(message.content)}
                    </Typography.Paragraph>}
                {/* 回复气泡不再内嵌完整折叠轨迹，改为一行锚点提示（避免进度搬家造成割裂） */}
                {message.role === "ASSISTANT" && hasExecutionAnchor ? (
                    <Typography.Link
                        type="secondary"
                        style={{fontSize: 12, marginTop: 8, display: "inline-block"}}
                        onClick={() => {
                            // 在消息列表中滚动到本条回复之前最近的一个 PROGRESS 气泡
                            const progressEl = document.querySelector(`[class*="agent-chat-message-assistant"] [style*="rgba(114,166,255"]`);
                            progressEl?.scrollIntoView({behavior: "smooth", block: "center"});
                        }}
                    >
                        <ThunderboltOutlined style={{marginRight: 4}}/>
                        执行详情见上方 ({message.executionEvents!.length} 步)
                    </Typography.Link>
                ) : null}
            </div>
        );
    }, [getMessageRolePrefix]);

    /** 统一分派渲染入口：按 bubbleType 分发到三个专用渲染。 */
    const renderMessageBubble = useCallback((message: AgentChatMessageDto): React.ReactNode => {
        if (message.bubbleType === "PROGRESS") {
            return renderProgressBubble(message);
        }
        if (message.bubbleType === "THINKING") {
            return renderThinkingBubble(message);
        }
        return renderNormalBubble(message);
    }, [renderProgressBubble, renderThinkingBubble, renderNormalBubble]);

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
            onPopupScroll={onAgentsPopupScroll}
            notFoundContent={agentsLoading ? <Spin size="small"/> : "暂无数据"}
          />
          <Select
              placeholder="选择模型"
            value={selectedModelId}
            style={{ width: 260, height: 36 }}
            onChange={setSelectedModelId}
            disabled={!selectedAgentId || models.length === 0}
            options={models.map(model => ({
              label: `${model.providerName} · ${model.modelName}`,
              value: model.id
            }))}
          />
            <Select
                placeholder="选择客户端"
                value={selectedClientId}
                style={{width: 220, height: 36}}
                onChange={setSelectedClientId}
                options={clients.map(c => ({label: c.clientName, value: c.id}))}
                showSearch
                onPopupScroll={onClientsPopupScroll}
                notFoundContent={clientsLoading ? <Spin size="small"/> : "暂无数据"}
                filterOption={(input, option) => (option?.label as string || "").includes(input)}
            />
            <Button type="primary" icon={<PlusOutlined/>} loading={creating} disabled={!selectedAgentId || !selectedModelId || !selectedClientId}
                    onClick={handleCreateSession}>新建对话</Button>
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
                              description={renderSessionDescription(session)}
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
              {messages.length === 0 && !aiThinking ? <Empty description="选择或创建会话后开始对话"/> : messages.map(message => renderMessageBubble(message))}
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
      </div>
    </div>
  );
}

/**
 * 渲染内嵌执行轨迹，按事件序号排列，展示步骤名称和耗时。
 *
 * @param events 执行事件列表
 * @returns React 元素列表
 */
function renderExecutionEvents(events: AgentChatExecutionEventDto[]) {
    if (!events || events.length === 0) {
        return <Typography.Text type="secondary">无执行事件</Typography.Text>;
    }

    // 按序号排序展示原子命令调用链
    const sortedEvents = [...events].sort((a, b) => (a.sequenceNo || 0) - (b.sequenceNo || 0));

    // 收集原子命令事件（仅展示有意义的步骤）
    const commandEvents = sortedEvents.filter(event =>
        event.eventType === "ATOMIC_COMMAND_START" ||
        event.eventType === "ATOMIC_COMMAND_COMPLETE" ||
        event.eventType === "AI_STARTED" ||
        event.eventType === "AI_COMPLETED" ||
        event.eventType === "CONTEXT_ASSEMBLED" ||
        event.eventType === "MEMORY_MATCHED" ||
        event.eventType === "MEMORY_MISSED"
    );

    if (commandEvents.length === 0) {
        return <Typography.Text type="secondary">暂无步骤详情</Typography.Text>;
    }

    return (
        <div style={{paddingLeft: 4}}>
            {commandEvents.map(event => {
                // 失败事件用红色标记
                const isFailed = event.eventType.includes("FAILED") || event.failureReason;
                const color = isFailed ? "red" : event.eventType.includes("AI") ? "purple" : "blue";

                return (
                    <div
                        key={event.id}
                        style={{
                            display: "flex",
                            alignItems: "center",
                            padding: "4px 0",
                            fontSize: 12
                        }}
                    >
                        <Tag color={color} style={{fontSize: 11, marginRight: 8, minWidth: 120, textAlign: "center"}}>
                            {event.stepName || event.commandName || event.eventType}
                        </Tag>
                        {event.responseContent && (
                            <Tooltip title={event.responseContent}>
                                <Typography.Text
                                    type="secondary"
                                    style={{
                                        maxWidth: 200,
                                        overflow: "hidden",
                                        textOverflow: "ellipsis",
                                        whiteSpace: "nowrap",
                                        marginRight: 8
                                    }}
                                >
                                    {event.responseContent.length > 30
                                        ? event.responseContent.substring(0, 30) + "..."
                                        : event.responseContent}
                                </Typography.Text>
                            </Tooltip>
                        )}
                        {event.failureReason && (
                            <Typography.Text type="danger" style={{fontSize: 11}}>
                                {event.failureReason}
                            </Typography.Text>
                        )}
          </div>
                );
            })}
    </div>
  );
}

function buildOptimisticUserMessage(content: string): AgentChatMessageDto {
    return {
        id: `local-user-${Date.now()}`,
        taskId: "",
        turnId: "",
        role: "USER",
        content,
        contentFormat: "PLAIN_TEXT",
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