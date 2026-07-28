package com.simple.ai.service.agent;

import com.simple.ai.common.dto.agent.AgentAiRequest;
import com.simple.ai.common.dto.agent.AgentAiResponse;
import com.simple.ai.common.dto.aiModel.AiModelRuntimeConfig;
import com.simple.ai.common.entity.agentChatMessage.AgentChatMessage;
import com.simple.ai.common.entity.agentChatRawLog.AgentChatRawLog;
import com.simple.ai.common.properties.SimpleAiProperties;
import com.simple.ai.common.service.agent.AgentAiClient;
import com.simple.ai.common.view.agentChatMessage.AgentChatMessageView;
import com.simple.ai.common.view.agentChatRawLog.AgentChatRawLogView;
import com.simple.ai.service.agentChat.AgentChatRuntimeContext;
import com.simple.ai.service.aiModel.AiModelChatClientFactory;
import com.simple.ai.service.aiModel.AiModelRoutingService;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.core.utils.JsonUtils;
import com.simple.common.mp.common.enums.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

/**
 * Spring AI 智能体 AI 调用客户端实现。
 * 从 ChatResponse / AssistantMessage.metadata 分别抽取 content 与 reasoning（thinkingContent），
 * 流式时分别推送给 tokenConsumer 与 thinkingTokenConsumer。
 *
 * @author qty
 */
@Service
class SpringAiAgentAiClient implements AgentAiClient {

    private static final Logger log = LoggerFactory.getLogger(SpringAiAgentAiClient.class);

    @Autowired
    private AiModelRoutingService aiModelRoutingService;

    @Autowired
    private AiModelChatClientFactory aiModelChatClientFactory;

    @Autowired
    private List<ToolCallback> toolCallbacks;

    /**
     * 聊天消息视图，用于加载会话历史消息
     */
    @Autowired
    private AgentChatMessageView agentChatMessageView;

    /**
     * 原始消息日志视图，用于保存 AI 调用原始请求/响应
     */
    @Autowired
    private AgentChatRawLogView agentChatRawLogView;

    /**
     * 全局配置属性。
     */
    @Autowired
    private SimpleAiProperties simpleAiProperties;

    /**
     * AssistantMessage / ChatResponse metadata 中推理内容 key 白名单（兼容 OpenAI 兼容推理模型）。
     */
    private static final List<String> REASONING_METADATA_KEYS = List.of("reasoning_content", "reasoning", "thinking_content", "thinking", "reasoningContent", "thinkingContent");

    @Override
    public AgentAiResponse chat(AgentAiRequest request) {

        // 校验 AI 调用请求中的必填业务内容
        assertAiRequest(request);

        // 按运行时路由解析模型并创建本次调用客户端
        AiModelRuntimeConfig config = resolveRuntimeConfig(request);
        ChatClient chatClient = aiModelChatClientFactory.create(config);

        // 调用 Spring AI 获取完整 ChatResponse
        ChatResponse chatResponse = callSpringAiChatResponse(chatClient, request);

        // 抽取 content 与 reasoning
        String content = extractAssistantContent(chatResponse);
        String thinking = extractAssistantReasoning(chatResponse);

        // 构建成功响应对象
        return buildSuccessResponse(content, thinking, config);
    }

    @Override
    public AgentAiResponse chatStream(AgentAiRequest request, Consumer<String> tokenConsumer, Consumer<String> thinkingTokenConsumer) {

        // 校验 AI 调用请求中的必填业务内容
        assertAiRequest(request);

        // 按运行时路由解析模型并创建本次调用客户端
        AiModelRuntimeConfig config = resolveRuntimeConfig(request);
        ChatClient chatClient = aiModelChatClientFactory.create(config);

        // 调用 Spring AI 流式接口，按 token 分类推送（content / reasoning）
        String[] aggregated = callSpringAiChatResponseStream(chatClient, request, tokenConsumer, thinkingTokenConsumer);

        // 构建成功响应对象
        return buildSuccessResponse(aggregated[0], aggregated[1], config);
    }

    @Override
    public AgentAiResponse chatStream(AgentAiRequest request, AgentChatRuntimeContext runtimeContext, Consumer<String> tokenConsumer, Consumer<String> thinkingTokenConsumer) {

        // 校验 AI 调用请求中的必填业务内容
        assertAiRequest(request);

        // 按运行时路由解析模型并创建本次调用客户端
        AiModelRuntimeConfig config = resolveRuntimeConfig(request);
        ChatClient chatClient = aiModelChatClientFactory.create(config);

        // 调用 Spring AI 流式接口，使用运行时上下文替代 ThreadLocal
        String[] aggregated = callSpringAiChatResponseStreamWithContext(chatClient, request, runtimeContext, tokenConsumer, thinkingTokenConsumer);

        // 构建成功响应对象
        return buildSuccessResponse(aggregated[0], aggregated[1], config);
    }

    /**
     * 校验 AI 调用请求。
     *
     * @param request AI 调用请求
     */
    private void assertAiRequest(AgentAiRequest request) {
        AssertUtils.notEmpty(request, "AI调用请求不能为空");
        AssertUtils.notEmpty(request.getAgentId(), "智能体主键不能为空");
        AssertUtils.notEmpty(request.getPromptContent(), "提示词内容不能为空");
        AssertUtils.notEmpty(request.getCommandContent(), "用户命令内容不能为空");
    }

    /**
     * 解析当前调用的运行时模型配置。
     *
     * @param request AI 调用请求
     * @return 运行时模型配置
     */
    private AiModelRuntimeConfig resolveRuntimeConfig(AgentAiRequest request) {
        return aiModelRoutingService.resolve(request.getModelId(), request.getAgentId());
    }

    /**
     * 调用 Spring AI 获取完整同步 ChatResponse。
     * <p>使用角色分离模式：system 提示词 + 对话历史 + 当前用户命令。</p>
     *
     * @param chatClient 动态聊天客户端
     * @param request AI 调用请求
     * @return Spring AI ChatResponse
     */
    private ChatResponse callSpringAiChatResponse(ChatClient chatClient, AgentAiRequest request) {
        String systemPrompt = request.getPromptContent();
        List<Message> historyMessages = loadHistoryMessages(request.getSessionId());

        // 保存原始请求日志
        saveRequestRawLog(request, systemPrompt, historyMessages);

        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt().system(systemPrompt);
        ChatClient.ChatClientRequestSpec historySpec = requestSpec.messages(historyMessages);

        // 注册工具回调，让 AI 在对话中自主调用工具完成数据操作
        ChatClient.ChatClientRequestSpec userSpec = historySpec.user(request.getCommandContent());
        ChatClient.ChatClientRequestSpec toolSpec = userSpec.toolCallbacks(toolCallbacks);
        ChatResponse chatResponse = toolSpec.call().chatResponse();

        // 保存原始响应日志
        saveResponseRawLog(request, chatResponse);
        return chatResponse;
    }

    /**
     * 调用 Spring AI 获取流式 ChatResponse Flux，分别把 content token 与 reasoning token 推送出去。
     * 返回 [aggregatedContent, aggregatedThinking] 用于落库。
     * <p>使用角色分离模式：system 提示词 + 对话历史 + 当前用户命令。</p>
     *
     * @param chatClient 动态聊天客户端
     * @param request AI 调用请求
     * @param tokenConsumer content token 消费者
     * @param thinkingTokenConsumer reasoning token 消费者（可为 null）
     * @return [完整content, 完整thinking] 聚合字符串
     */
    private String[] callSpringAiChatResponseStream(ChatClient chatClient, AgentAiRequest request, Consumer<String> tokenConsumer, Consumer<String> thinkingTokenConsumer) {
        String systemPrompt = request.getPromptContent();
        List<Message> historyMessages = loadHistoryMessages(request.getSessionId());
        StringBuilder contentBuilder = new StringBuilder();
        StringBuilder thinkingBuilder = new StringBuilder();

        // 保存原始请求日志（流式请求结构与非流式一致）
        saveRequestRawLog(request, systemPrompt, historyMessages);

        // 构建工具上下文，传递 sessionId 供工具回调获取用户上下文
        // 过滤 null 和空字符串值，避免 Spring AI 抛出 "toolContext values cannot contain null elements"
        Map<String, Object> toolContext = new java.util.HashMap<>();
        if (request.getSessionId() != null && !request.getSessionId().isBlank()) {
            putIfNotBlank(toolContext, "sessionId", request.getSessionId());
            putIfNotBlank(toolContext, "userId", request.getUserId());
            putIfNotBlank(toolContext, "agentId", request.getAgentId());
        }

        // 构建 Spring AI 流式请求，使用角色分离：system 提示词 + 对话历史 + 当前用户命令
        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt().system(systemPrompt);
        ChatClient.ChatClientRequestSpec historySpec = requestSpec.messages(historyMessages);

        // 注册工具回调，让 AI 在流式对话中自主调用工具完成数据操作
        ChatClient.ChatClientRequestSpec userSpec = historySpec.user(request.getCommandContent());
        ChatClient.ChatClientRequestSpec toolSpec = userSpec.toolCallbacks(toolCallbacks).toolContext(toolContext);
        ChatClient.StreamResponseSpec streamSpec = toolSpec.stream();
        Flux<ChatResponse> chatResponseFlux = streamSpec.chatResponse();

        // 消费每一片 ChatResponse，分别提取 content token / reasoning token
        chatResponseFlux.doOnNext(chatResponse -> acceptStreamChatResponse(tokenConsumer, thinkingTokenConsumer, contentBuilder, thinkingBuilder, chatResponse)).blockLast();

        String[] result = new String[] { contentBuilder.toString(), thinkingBuilder.toString() };

        // 保存原始响应日志（流式聚合后的完整内容）
        saveResponseRawLog(request, result[0], result[1]);
        return result;
    }

    /**
     * 调用 Spring AI 获取流式 ChatResponse Flux（显式传递运行时上下文）。
     * <p>使用运行时上下文替代 ThreadLocal，工具回调直接从上下文获取 userId/clientId。</p>
     *
     * @param chatClient            动态聊天客户端
     * @param request               AI 调用请求
     * @param runtimeContext        聊天运行时上下文
     * @param tokenConsumer         content token 消费者
     * @param thinkingTokenConsumer reasoning token 消费者（可为 null）
     * @return [完整content, 完整thinking] 聚合字符串
     */
    private String[] callSpringAiChatResponseStreamWithContext(ChatClient chatClient, AgentAiRequest request, AgentChatRuntimeContext runtimeContext, Consumer<String> tokenConsumer,
                                                               Consumer<String> thinkingTokenConsumer) {
        String systemPrompt = request.getPromptContent();
        List<Message> historyMessages = loadHistoryMessages(request.getSessionId());
        StringBuilder contentBuilder = new StringBuilder();
        StringBuilder thinkingBuilder = new StringBuilder();

        // 保存原始请求日志（流式请求结构与非流式一致）
        saveRequestRawLog(request, systemPrompt, historyMessages);

        // 构建工具上下文，传递运行时上下文供工具回调获取用户上下文
        // 过滤 null 和空字符串值，避免 Spring AI 抛出 "toolContext values cannot contain null elements"
        Map<String, Object> toolContext = new java.util.HashMap<>();
        if (runtimeContext != null) {
            putIfNotBlank(toolContext, "sessionId", runtimeContext.getSessionId());
            putIfNotBlank(toolContext, "userId", runtimeContext.getUserId());
            putIfNotBlank(toolContext, "clientId", runtimeContext.getClientId());
            putIfNotBlank(toolContext, "agentId", runtimeContext.getAgentId());
            putIfNotBlank(toolContext, "turnId", runtimeContext.getTurnId());
            putIfNotBlank(toolContext, "taskId", runtimeContext.getTaskId());

            // 传递事件发送器，供工具回调发送流式进度事件
            if (runtimeContext.getEventSender() != null) {
                toolContext.put("eventSender", runtimeContext.getEventSender());
            }
        }

        // 直接使用原始工具回调，不再使用 SessionAwareToolCallback 包装
        // 工具回调通过 ToolContext 获取运行时上下文
        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt().system(systemPrompt);
        ChatClient.ChatClientRequestSpec historySpec = requestSpec.messages(historyMessages);

        // 注册工具回调，让 AI 在流式对话中自主调用工具完成数据操作
        ChatClient.ChatClientRequestSpec userSpec = historySpec.user(request.getCommandContent());
        ChatClient.ChatClientRequestSpec toolSpec = userSpec.toolCallbacks(toolCallbacks).toolContext(toolContext);
        ChatClient.StreamResponseSpec streamSpec = toolSpec.stream();
        Flux<ChatResponse> chatResponseFlux = streamSpec.chatResponse();

        // 消费每一片 ChatResponse，分别提取 content token / reasoning token
        chatResponseFlux.doOnNext(chatResponse -> acceptStreamChatResponse(tokenConsumer, thinkingTokenConsumer, contentBuilder, thinkingBuilder, chatResponse)).blockLast();

        String[] result = new String[] { contentBuilder.toString(), thinkingBuilder.toString() };

        // 保存原始响应日志（流式聚合后的完整内容）
        saveResponseRawLog(request, result[0], result[1]);
        return result;
    }

    /**
     * 处理流式单帧 ChatResponse：抽取 content / reasoning 两个维度的 token，
     * 分别推送给两个消费者并聚合用于最终落库。
     *
     * @param tokenConsumer content token 消费者（可能为 null）
     * @param thinkingTokenConsumer reasoning token 消费者（可能为 null）
     * @param contentBuilder content 聚合器
     * @param thinkingBuilder reasoning 聚合器
     * @param chatResponse 当前帧
     */
    private void acceptStreamChatResponse(Consumer<String> tokenConsumer, Consumer<String> thinkingTokenConsumer, StringBuilder contentBuilder, StringBuilder thinkingBuilder,
                                          ChatResponse chatResponse) {
        if (chatResponse == null || chatResponse.getResults() == null) {
            return;
        }

        // 对 Spring AI 返回的每条 AssistantMessage 结果分别提取
        for (var generation : chatResponse.getResults()) {
            if (generation == null || generation.getOutput() == null) {
                continue;
            }
            AssistantMessage message = generation.getOutput();

            // 正常回复文本 token
            String contentPiece = message.getText();
            if (contentPiece != null && !contentPiece.isEmpty()) {
                contentBuilder.append(contentPiece);
                if (tokenConsumer != null) {
                    // 必须让异常传播出去，以便中断后台任务
                    tokenConsumer.accept(contentPiece);
                }
            }

            // 思考推理 token（来自 AssistantMessage.metadata）
            String reasoningPiece = extractReasoningFromMapMetadata(message.getMetadata());
            if (reasoningPiece != null && !reasoningPiece.isEmpty()) {
                thinkingBuilder.append(reasoningPiece);
                if (thinkingTokenConsumer != null) {
                    // 必须让异常传播出去，以便中断后台任务
                    thinkingTokenConsumer.accept(reasoningPiece);
                }
            }
        }

        // ChatResponse 顶层 metadata 兜底（少数供应商把推理放整包响应里）
        String topReasoning = extractReasoningFromChatResponseMetadata(chatResponse.getMetadata());
        if (topReasoning != null && !topReasoning.isEmpty()) {
            thinkingBuilder.append(topReasoning);
            if (thinkingTokenConsumer != null) {
                // 必须让异常传播出去，以便中断后台任务
                thinkingTokenConsumer.accept(topReasoning);
            }
        }
    }

    /**
     * 从同步 ChatResponse 中抽取主 Assistant 文本内容。
     *
     * @param chatResponse Spring AI ChatResponse
     * @return 文本内容或空串
     */
    private String extractAssistantContent(ChatResponse chatResponse) {
        if (chatResponse == null || chatResponse.getResults() == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (var generation : chatResponse.getResults()) {
            if (generation != null && generation.getOutput() != null && generation.getOutput().getText() != null) {
                sb.append(generation.getOutput().getText());
            }
        }
        return sb.toString();
    }

    /**
     * 从同步 ChatResponse 的 AssistantMessage.metadata / ChatResponse.metadata 中抽取 reasoning。
     *
     * @param chatResponse Spring AI ChatResponse
     * @return reasoning 内容或空串
     */
    private String extractAssistantReasoning(ChatResponse chatResponse) {
        if (chatResponse == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();

        if (chatResponse.getResults() != null) {
            for (var generation : chatResponse.getResults()) {
                if (generation != null && generation.getOutput() != null) {
                    String piece = extractReasoningFromMapMetadata(generation.getOutput().getMetadata());
                    if (piece != null) {
                        sb.append(piece);
                    }
                }
            }
        }

        String top = extractReasoningFromChatResponseMetadata(chatResponse.getMetadata());
        if (top != null) {
            sb.append(top);
        }
        return sb.toString();
    }

    /**
     * 从 AssistantMessage 的 Map<String, Object> metadata 中按白名单顺序取第一个非空 reasoning 片段。
     * 支持嵌套 map 递归查找。
     *
     * @param metadata Map 形式 metadata
     * @return reasoning 文本片段或 null
     */
    private String extractReasoningFromMapMetadata(Map<String, Object> metadata) {
        if (metadata == null || metadata.isEmpty()) {
            return null;
        }
        for (String key : REASONING_METADATA_KEYS) {
            Object value = metadata.get(key);
            if (value == null) {
                continue;
            }
            if (value instanceof String s && !s.isEmpty()) {
                return s;
            }
            if (value instanceof Map<?, ?> nestedMap) {
                // 兼容嵌套结构，递归搜索一次
                @SuppressWarnings("unchecked") Map<String, Object> casted = (Map<String, Object>) nestedMap;
                String nested = extractReasoningFromMapMetadata(casted);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    /**
     * 从 ChatResponseMetadata 中按白名单顺序取第一个非空 reasoning 片段。
     * Spring AI 1.0.0 ChatResponseMetadata 不是 Map，
     * 这里通过反射调用其 get(Object key) 方法（Spring 元数据接口通用约定）兜底取值，
     * 失败时再回退使用 keySet() + 反射 getMethod 取值。
     *
     * @param metadata ChatResponse 顶层元数据
     * @return reasoning 文本片段或 null
     */
    private String extractReasoningFromChatResponseMetadata(ChatResponseMetadata metadata) {
        if (metadata == null) {
            return null;
        }
        Method getMethod;
        try {
            getMethod = metadata.getClass().getMethod("get", Object.class);
        } catch (NoSuchMethodException ignored) {
            // 没有通用 get(Object) 方法时，回退为通过 keySet 找并通过同名 getter 反射取
            return extractReasoningFromMetadataByKeyset(metadata);
        }

        for (String key : REASONING_METADATA_KEYS) {
            Object value;
            try {
                value = getMethod.invoke(metadata, key);
            } catch (Exception ignored) {
                continue;
            }
            if (value == null) {
                continue;
            }
            if (value instanceof String s && !s.isEmpty()) {
                return s;
            }
            if (value instanceof Map<?, ?> nestedMap) {
                @SuppressWarnings("unchecked") Map<String, Object> casted = (Map<String, Object>) nestedMap;
                String nested = extractReasoningFromMapMetadata(casted);
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    /**
     * ChatResponseMetadata.get(Object) 不存在时的兜底：通过 keySet() 或通过反射遍历 REASONING_METADATA_KEYS
     * 调用同名 getter（getReasoningContent / getReasoning / ...）。
     *
     * @param metadata ChatResponseMetadata
     * @return reasoning 文本片段或 null
     */
    private String extractReasoningFromMetadataByKeyset(ChatResponseMetadata metadata) {
        if (metadata == null) {
            return null;
        }
        Set<String> availableKeys = new HashSet<>();

        // 获取 keySet（如果有的话）
        try {
            Method keySetMethod = metadata.getClass().getMethod("keySet");
            Object keySetResult = keySetMethod.invoke(metadata);
            if (keySetResult instanceof Set<?> set) {
                for (Object o : set) {
                    if (o instanceof String s) {
                        availableKeys.add(s);
                    }
                }
            }
        } catch (Exception ignored) {
            // keySet 取不到时用 REASONING_METADATA_KEYS 全集尝试
        }

        for (String key : REASONING_METADATA_KEYS) {
            if (!availableKeys.isEmpty() && !availableKeys.contains(key)) {
                continue;
            }
            // 优先 get(String) 或 get(Object)
            String direct = invokeStringGetter(metadata, "get", new Class<?>[] { Object.class }, key);
            if (direct != null) {
                return direct;
            }
            direct = invokeStringGetter(metadata, "get", new Class<?>[] { String.class }, key);
            if (direct != null) {
                return direct;
            }
            // 再尝试 Bean getter：reasoningContent -> getReasoningContent
            String beanGetter = "get" + Character.toUpperCase(key.charAt(0)) + key.substring(1);
            direct = invokeStringGetter(metadata, beanGetter, new Class<?>[0]);
            if (direct != null) {
                return direct;
            }
        }
        return null;
    }

    /**
     * 反射调用指定名称方法并将返回值在非空 String 或 Map 情况下抽取。
     *
     * @param target     目标对象
     * @param methodName 方法名
     * @param paramTypes 参数类型
     * @param args       实参
     * @return 字符串 reasoning 或 null
     */
    private String invokeStringGetter(Object target, String methodName, Class<?>[] paramTypes, Object... args) {
        try {
            Method m = target.getClass().getMethod(methodName, paramTypes);
            Object value = m.invoke(target, args);
            if (value == null) {
                return null;
            }
            if (value instanceof String s && !s.isEmpty()) {
                return s;
            }
            if (value instanceof Map<?, ?> nestedMap) {
                @SuppressWarnings("unchecked") Map<String, Object> casted = (Map<String, Object>) nestedMap;
                return extractReasoningFromMapMetadata(casted);
            }
            return null;
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 从数据库加载会话的最近 N 轮对话历史，转换为 Spring AI Message 列表。
     *
     * <p>每轮包含一条 USER 消息和一条 ASSISTANT 消息。
     * 按 sequence_no 升序排列，确保对话时序正确。</p>
     *
     * <p>排除最近一条 USER 消息：因为当前消息已通过 saveUserMessage 先落库，
     * 再通过 .user(currentCommand) 单独发送，历史中不应重复包含。</p>
     *
     * @param sessionId 会话ID，为空时返回空列表
     * @return 对话历史消息列表
     */
    private List<Message> loadHistoryMessages(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return new ArrayList<>();
        }

        // 查询会话全部消息，按序号升序排列
        List<AgentChatMessage> allMessages = agentChatMessageView.findAllBySessionId(sessionId);
        if (allMessages.isEmpty()) {
            return new ArrayList<>();
        }

        // 从最新消息向前取最近 N 轮，转换为 Spring AI Message
        List<Message> historyMessages = new ArrayList<>();
        int turnCount = 0;

        // 标记是否已跳过当前轮次的 USER 消息（即最近一条 user 消息，它会在 .user() 中单独发送）
        boolean skippedCurrentUser = false;

        // 从最新到最旧遍历消息，按轮数限制裁剪
        for (int i = allMessages.size() - 1; i >= 0; i--) {
            AgentChatMessage msg = allMessages.get(i);

            // 只取 USER 和 ASSISTANT 角色的消息，SYSTEM_ERROR 不参与上下文
            if (!"USER".equals(msg.getRole()) && !"ASSISTANT".equals(msg.getRole())) {
                continue;
            }

            // 跳过最近一条 USER 消息（即当前消息，已通过 .user() 单独发送），避免重复
            if ("USER".equals(msg.getRole()) && !skippedCurrentUser) {
                skippedCurrentUser = true;
                continue;
            }

            // 每遇到一条 USER 消息计数为一轮
            if ("USER".equals(msg.getRole())) {
                turnCount++;
                if (turnCount > simpleAiProperties.getChat().getMaxHistoryTurns()) {
                    break;
                }
            }

            // 插入到列表头部以保持时间顺序
            historyMessages.add(0, toSpringAiMessage(msg));
        }

        return historyMessages;
    }

    /**
     * 将 AgentChatMessage 实体转换为 Spring AI Message 对象。
     *
     * @param msg 聊天消息实体
     * @return Spring AI Message 对象
     */
    private Message toSpringAiMessage(AgentChatMessage msg) {
        if ("ASSISTANT".equals(msg.getRole())) {
            return new AssistantMessage(msg.getContent());
        }
        return new UserMessage(msg.getContent());
    }

    /**
     * 保存原始请求日志：将发送给大模型的 system 提示词、对话历史、用户命令序列化为 JSON 并落库。
     *
     * @param request AI 调用请求
     * @param systemPrompt 系统提示词
     * @param historyMessages 对话历史消息列表
     */
    private void saveRequestRawLog(AgentAiRequest request, String systemPrompt, List<Message> historyMessages) {
        try {
            // 构建请求原始内容 JSON
            Map<String, Object> requestContent = new HashMap<>();
            requestContent.put("system", systemPrompt);

            // 将历史消息转换为 role:content 格式
            List<Map<String, String>> messages = new ArrayList<>();
            for (Message msg : historyMessages) {
                Map<String, String> entry = new HashMap<>();
                entry.put("role", (msg instanceof AssistantMessage) ? "assistant" : "user");
                entry.put("content", msg.getText());
                messages.add(entry);
            }
            requestContent.put("messages", messages);
            requestContent.put("user", request.getCommandContent());

            // 构建实体并保存
            AgentChatRawLog rawLog = new AgentChatRawLog();
            rawLog.setSessionId(request.getSessionId());
            rawLog.setDirection("REQUEST");
            rawLog.setRawContent(JsonUtils.toJsonStr(requestContent));
            rawLog.setModelCode("");
            rawLog.setProviderId("");
            rawLog.setStatus(Status.ON);
            rawLog.setRemark("");

            agentChatRawLogView.save(rawLog);
        } catch (RuntimeException e) {

            // 原始日志保存失败不影响主流程
            log.warn("原始请求日志保存失败，sessionId={}", request.getSessionId(), e);
        }
    }

    /**
     * 保存原始响应日志：将 ChatResponse 中的 content 和 reasoning 序列化为 JSON 并落库。
     *
     * @param request      AI 调用请求
     * @param chatResponse Spring AI ChatResponse 对象
     */
    private void saveResponseRawLog(AgentAiRequest request, ChatResponse chatResponse) {
        try {
            String content = extractAssistantContent(chatResponse);
            String thinking = extractAssistantReasoning(chatResponse);

            // 构建响应原始内容 JSON
            Map<String, Object> responseContent = new HashMap<>();
            responseContent.put("content", content);
            responseContent.put("thinking", thinking);

            // 构建实体并保存
            AgentChatRawLog rawLog = new AgentChatRawLog();
            rawLog.setSessionId(request.getSessionId());
            rawLog.setDirection("RESPONSE");
            rawLog.setRawContent(JsonUtils.toJsonStr(responseContent));
            rawLog.setModelCode("");
            rawLog.setProviderId("");
            rawLog.setStatus(Status.ON);
            rawLog.setRemark("");

            agentChatRawLogView.save(rawLog);
        } catch (RuntimeException e) {

            // 原始日志保存失败不影响主流程
            log.warn("原始响应日志保存失败，sessionId={}", request.getSessionId(), e);
        }
    }

    /**
     * 保存原始响应日志（流式聚合版）：将聚合后的 content 和 thinking 序列化为 JSON 并落库。
     *
     * @param request  AI 调用请求
     * @param content  聚合后的内容文本
     * @param thinking 聚合后的思考文本
     */
    private void saveResponseRawLog(AgentAiRequest request, String content, String thinking) {
        try {
            // 构建响应原始内容 JSON
            Map<String, Object> responseContent = new HashMap<>();
            responseContent.put("content", content);
            responseContent.put("thinking", thinking);

            // 构建实体并保存
            AgentChatRawLog rawLog = new AgentChatRawLog();
            rawLog.setSessionId(request.getSessionId());
            rawLog.setDirection("RESPONSE");
            rawLog.setRawContent(JsonUtils.toJsonStr(responseContent));
            rawLog.setModelCode("");
            rawLog.setProviderId("");
            rawLog.setStatus(Status.ON);
            rawLog.setRemark("");

            agentChatRawLogView.save(rawLog);
        } catch (RuntimeException e) {

            // 原始日志保存失败不影响主流程
            log.warn("原始响应日志保存失败，sessionId={}", request.getSessionId(), e);
        }
    }

    /**
     * 构建成功响应对象。
     *
     * @param content 响应内容
     * @param thinkingContent 思考推理过程完整文本
     * @param config 实际运行模型配置
     * @return AI 调用响应
     */
    private AgentAiResponse buildSuccessResponse(String content, String thinkingContent, AiModelRuntimeConfig config) {
        AgentAiResponse response = new AgentAiResponse();
        response.setSuccess(Boolean.TRUE);
        response.setResponseContent(content);
        response.setThinkingContent(thinkingContent == null ? "" : thinkingContent);
        response.setFailureReason("");
        response.setProviderId(config.getProviderId());
        response.setProviderName(config.getProviderName());
        response.setModelId(config.getModelId());
        response.setModelCode(config.getModelCode());
        return response;
    }

    /**
     * 向工具上下文中放入非空且非空字符串的值。
     * <p>HashMap 本身允许 null value，但 Spring AI ToolContext 会校验拒绝 null 元素，
     * 因此需要在放入前过滤。</p>
     *
     * @param map   目标 Map
     * @param key   键
     * @param value 值（可能为 null 或空字符串）
     */
    private void putIfNotBlank(Map<String, Object> map, String key, String value) {
        if (value != null && !value.isBlank()) {
            map.put(key, value);
        }
    }
}