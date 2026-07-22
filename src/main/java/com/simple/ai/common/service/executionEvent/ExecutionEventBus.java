package com.simple.ai.common.service.executionEvent;

import com.simple.ai.common.dto.command.CommandDispatchProgressEvent;

/**
 * 执行事件总线接口，负责将调度进度事件转换为 ExecutionEvent 记录并持久化。
 * <p>在聊天轮次内由 DefaultAgentChatService 调用，每个调度阶段事件对应一条执行事件。</p>
 *
 * @author qty
 */
public interface ExecutionEventBus {

    /**
     * 记录一条执行事件。
     * <p>根据进度事件的 eventType 映射为 ExecutionEvent 的事件类型，并持久化到数据库。</p>
     *
     * @param turnId 对话轮次主键
     * @param taskId 调度任务主键
     * @param event  调度进度事件
     */
    void recordEvent(String turnId, String taskId, CommandDispatchProgressEvent event);
}
