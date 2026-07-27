package com.simple.ai.service.agent;

import com.simple.ai.common.dto.command.CommandDispatchProgressEvent;

import java.util.function.Consumer;

/**
 * 进度事件消费者 ThreadLocal 持有器。
 * <p>用于在跨层调用链（DispatchService → Assembler → AiClient → ToolCallback）中
 * 传递进度事件消费者，避免逐层修改方法签名。</p>
 * <p>使用模式对齐 {@link AgentSessionContext} ThreadLocal 风格：
 * 在顶层设置 → 子层通过 get() 获取并发布进度 → 顶层清理。</p>
 *
 * @author qty
 */
public class ProgressConsumerHolder {

    /**
     * 进度事件消费者 ThreadLocal，每个请求线程独立持有
     */
    private static final ThreadLocal<Consumer<CommandDispatchProgressEvent>> CONSUMER_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前线程的进度消费者。
     *
     * @param consumer 进度事件消费者，可为 null 表示无消费者
     */
    public static void set(Consumer<CommandDispatchProgressEvent> consumer) {
        CONSUMER_HOLDER.set(consumer);
    }

    /**
     * 获取当前线程的进度消费者。
     *
     * @return 进度事件消费者，未设置时返回 null
     */
    public static Consumer<CommandDispatchProgressEvent> get() {
        return CONSUMER_HOLDER.get();
    }

    /**
     * 清理当前线程的进度消费者，防止内存泄漏。
     */
    public static void clear() {
        CONSUMER_HOLDER.remove();
    }
}
