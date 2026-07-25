package com.simple.ai.common.service.memory;

import com.simple.ai.common.dto.command.CommandDispatchProgressEvent;
import com.simple.ai.common.dto.command.CommandDispatchRequest;
import com.simple.ai.common.entity.task.Task;
import lombok.Data;
import lombok.experimental.Accessors;

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
     * @return 执行结果（含成功标志和详情文本）
     */
    MemoryExecutionResult execute(Task task, CommandDispatchRequest request, String memoryId, Consumer<CommandDispatchProgressEvent> progressConsumer);

    /**
     * 记忆执行结果。
     * <p>结构化返回执行是否全部成功及详情文本，
     * 避免调用方通过字符串匹配判断成功/失败。</p>
     */
    @Data
    @Accessors(chain = true)
    class MemoryExecutionResult {

        /**
         * 是否全部步骤执行成功
         */
        private boolean success;

        /**
         * 执行详情文本（各步骤名称+结果的拼接）
         */
        private String detail;
    }
}