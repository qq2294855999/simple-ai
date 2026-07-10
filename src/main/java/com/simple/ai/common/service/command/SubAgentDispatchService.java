package com.simple.ai.common.service.command;

import com.simple.ai.common.dto.command.CommandDispatchResponse;
import com.simple.ai.common.dto.command.SubAgentDispatchContext;

/**
 * 子智能体调度服务。
 *
 * @author qty
 */
public interface SubAgentDispatchService {

    /**
     * 调度子智能体命令。
     *
     * @param context 子智能体调度上下文
     * @return 子智能体命令调度响应
     */
    CommandDispatchResponse dispatch(SubAgentDispatchContext context);
}
