package com.simple.ai.common.service.memory;

import com.simple.ai.common.dto.command.CommandDispatchProgressEvent;
import com.simple.ai.common.dto.command.CommandDispatchRequest;
import com.simple.ai.common.entity.task.Task;

import java.util.function.Consumer;

/**
 * 记忆执行器。
 * <p>按记忆步骤直接创建任务并下发客户端执行，
 * 无需 AI 探索。步骤中的 {param} 占位符由用户输入参数替换。</p>
 *
 * @author qty
 */
public interface MemoryExecutor {

    /**
     * 按记忆直接执行任务。
     * <p>加载记忆及其步骤序列，替换参数占位符，
     * 依次创建 task_detail 并下发原子命令到客户端。</p>
     *
     * @param task             任务主记录
     * @param request          命令调度请求
     * @param memoryId         记忆ID
     * @param progressConsumer 进度事件消费者
     * @return 执行结果内容
     */
    String execute(Task task, CommandDispatchRequest request, String memoryId, Consumer<CommandDispatchProgressEvent> progressConsumer);
}