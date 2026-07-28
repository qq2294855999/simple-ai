package com.simple.ai.common.service.command;

import com.simple.ai.common.dto.command.CommandDispatchProgressEvent;
import com.simple.ai.common.dto.command.CommandDispatchRequest;
import com.simple.ai.common.dto.command.CommandDispatchResponse;
import com.simple.ai.service.agentChat.AgentChatRuntimeContext;

import java.util.function.Consumer;

/**
 * 智能体命令调度服务。
 *
 * @author qty
 */
public interface CommandDispatchService {

    /**
     * 调度智能体命令。
     *
     * @param request 命令调度请求
     * @return 命令调度响应
     */
    CommandDispatchResponse dispatch(CommandDispatchRequest request);

    /**
     * 流式调度智能体命令。
     *
     * @param request 命令调度请求
     * @param progressConsumer 进度事件消费者
     * @return 命令调度最终响应
     */
    CommandDispatchResponse dispatchStream(CommandDispatchRequest request, Consumer<CommandDispatchProgressEvent> progressConsumer);

    /**
     * 流式调度智能体命令（显式传递运行时上下文）。
     *
     * @param request          命令调度请求
     * @param runtimeContext   聊天运行时上下文
     * @param progressConsumer 进度事件消费者
     * @return 命令调度最终响应
     */
    default CommandDispatchResponse dispatchStream(CommandDispatchRequest request, AgentChatRuntimeContext runtimeContext, Consumer<CommandDispatchProgressEvent> progressConsumer) {
        return dispatchStream(request, progressConsumer);
    }
}