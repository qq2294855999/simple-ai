package com.simple.ai.service.command;

import com.simple.ai.common.dto.command.CommandDispatchProgressEvent;
import com.simple.ai.common.dto.command.CommandDispatchRequest;
import com.simple.ai.common.dto.command.CommandDispatchResponse;

import java.util.function.Consumer;

/**
 * 内部命令调度执行器。
 *
 * @author qty
 */
interface InternalCommandDispatchExecutor {

    /**
     * 执行内部命令调度。
     *
     * @param request 命令调度请求
     * @param progressConsumer 进度事件消费者
     * @param parentTaskId 父任务ID
     * @param recursionDepth 当前递归深度
     * @return 命令调度响应
     */
    CommandDispatchResponse dispatchInternal(CommandDispatchRequest request,
                                             Consumer<CommandDispatchProgressEvent> progressConsumer,
                                             String parentTaskId,
                                             int recursionDepth);
}
