package com.simple.ai.service.agentChat;

import com.simple.ai.common.dto.agentChat.ChatSseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.function.Consumer;

/**
 * 智能体聊天统一事件发送器。
 * <p>所有业务服务均只能调用该发送器，不得再自行构造和向浏览器暴露内部事件。</p>
 * <p>对外 SSE 类型：PROGRESS、THINKING、REPLY、FINAL、ERROR。</p>
 *
 * @author qty
 */
public class ChatEventSender {

    private static final Logger log = LoggerFactory.getLogger(ChatEventSender.class);

    /**
     * SSE 事件消费者（由 AgentChatController 注入）
     */
    private final Consumer<ChatSseEvent> eventConsumer;

    /**
     * 创建聊天事件发送器。
     *
     * @param eventConsumer SSE 事件消费者
     */
    public ChatEventSender(Consumer<ChatSseEvent> eventConsumer) {
        this.eventConsumer = eventConsumer;
    }

    /**
     * 发送进度事件。
     *
     * @param message 面向用户的当前动作描述
     */
    public void sendProgress(String message) {
        send(ChatSseEvent.builder().type(ChatSseEvent.Types.PROGRESS).data(message).completed(false).build());
    }

    /**
     * 发送思考增量事件。
     *
     * @param chunk reasoning 增量 token
     */
    public void sendThinking(String chunk) {
        send(ChatSseEvent.builder().type(ChatSseEvent.Types.THINKING).data(chunk).completed(false).build());
    }

    /**
     * 发送回复增量事件。
     *
     * @param chunk content 增量 token
     */
    public void sendReply(String chunk) {
        send(ChatSseEvent.builder().type(ChatSseEvent.Types.REPLY).data(chunk).completed(false).build());
    }

    /**
     * 发送最终事件。
     *
     * @param messageId       消息主键
     * @param turnId          轮次主键
     * @param thinkingSummary 思考内容摘要
     */
    public void sendFinal(String messageId, String turnId, String thinkingSummary) {
        send(ChatSseEvent.builder().type(ChatSseEvent.Types.FINAL).messageId(messageId).turnId(turnId).thinkingSummary(thinkingSummary).completed(true).build());
    }

    /**
     * 发送错误事件。
     *
     * @param errorReason 可展示的失败原因
     */
    public void sendError(String errorReason) {
        send(ChatSseEvent.builder().type(ChatSseEvent.Types.ERROR).errorReason(errorReason).completed(true).build());
    }

    /**
     * 发送 SSE 事件到浏览器。
     *
     * @param event 聊天 SSE 事件
     */
    private void send(ChatSseEvent event) {
        if (eventConsumer == null) {
            return;
        }
        try {
            eventConsumer.accept(event);
        } catch (RuntimeException e) {

            // 客户端断开只终止事件投递，不影响主流程
            log.warn("发送聊天 SSE 事件失败，type={}", event.getType(), e);
        }
    }

    /**
     * 将 ChatSseEvent 转换为 SseEmitter 可发送的格式。
     * <p>此方法由 AgentChatController 调用，将统一事件写入 SSE 通道。</p>
     *
     * @param emitter SSE 输出器
     * @param event   聊天 SSE 事件
     */
    public static void writeToEmitter(SseEmitter emitter, ChatSseEvent event) {
        try {
            emitter.send(SseEmitter.event().name(event.getType()).data(event));
        } catch (IOException e) {
            throw new IllegalStateException("发送智能体聊天 SSE 事件失败", e);
        }
    }
}