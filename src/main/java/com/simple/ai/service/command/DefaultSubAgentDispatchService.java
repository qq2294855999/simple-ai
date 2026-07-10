package com.simple.ai.service.command;

import com.simple.ai.common.dto.command.CommandDispatchRequest;
import com.simple.ai.common.dto.command.CommandDispatchResponse;
import com.simple.ai.common.dto.command.CommandDispatchProgressEvent;
import com.simple.ai.common.dto.command.SubAgentDispatchContext;
import com.simple.ai.common.entity.agentMemoryDetail.AgentMemoryDetail;
import com.simple.ai.common.entity.subAgentRelation.SubAgentRelation;
import com.simple.ai.common.entity.task.Task;
import com.simple.ai.common.service.command.SubAgentDispatchService;
import com.simple.common.core.utils.AssertUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

/**
 * 默认子智能体调度服务实现。
 *
 * @author qty
 */
@Service
class DefaultSubAgentDispatchService implements SubAgentDispatchService {

    /**
     * 内部命令调度执行器
     */
    private final InternalCommandDispatchExecutor internalCommandDispatchExecutor;

    /**
     * 默认子智能体调度服务实现。
     *
     * @param internalCommandDispatchExecutor 内部命令调度执行器
     */
    DefaultSubAgentDispatchService(@Lazy InternalCommandDispatchExecutor internalCommandDispatchExecutor) {
        this.internalCommandDispatchExecutor = internalCommandDispatchExecutor;
    }

    @Override
    public CommandDispatchResponse dispatch(SubAgentDispatchContext context) {

        // 参数校验：子智能体调度上下文不能为空
        AssertUtils.notEmpty(context, "子智能体调度上下文不能为空");

        // 构建子智能体命令请求
        CommandDispatchRequest subRequest = buildSubAgentRequest(context);

        // 调用内部命令调度执行器，保持父任务和递归深度上下文不丢失
        return dispatchSubAgentRequest(context, subRequest);
    }

    /**
     * 构建子智能体命令请求。
     *
     * @param context 子智能体调度上下文
     * @return 子智能体命令请求
     */
    private CommandDispatchRequest buildSubAgentRequest(SubAgentDispatchContext context) {
        SubAgentRelation relation = context.getRelation();
        AgentMemoryDetail detail = context.getMemoryDetail();
        CommandDispatchRequest parentRequest = context.getParentRequest();
        AssertUtils.notEmpty(relation, "子智能体关系不能为空");
        AssertUtils.notEmpty(detail, "子智能体步骤不能为空");
        AssertUtils.notEmpty(parentRequest, "父命令调度请求不能为空");
        AssertUtils.notEmpty(relation.getSubAgentId(), "子智能体ID不能为空");
        AssertUtils.notEmpty(detail.getStepName(), "子智能体步骤名称不能为空");
        AssertUtils.notEmpty(detail.getExecContent(), "子智能体执行内容不能为空");
        CommandDispatchRequest subRequest = new CommandDispatchRequest();
        subRequest.setAgentId(relation.getSubAgentId());
        subRequest.setCommandName(detail.getStepName());
        subRequest.setCommandContent(detail.getExecContent());
        subRequest.setSessionId(parentRequest.getSessionId());
        subRequest.setRequestParams(parentRequest.getRequestParams());
        return subRequest;
    }

    /**
     * 调度子智能体命令请求。
     *
     * @param context 子智能体调度上下文
     * @param subRequest 子智能体命令请求
     * @return 子智能体命令响应
     */
    private CommandDispatchResponse dispatchSubAgentRequest(SubAgentDispatchContext context, CommandDispatchRequest subRequest) {
        Task parentTask = context.getParentTask();
        Consumer<CommandDispatchProgressEvent> progressConsumer = context.getProgressConsumer();
        Integer recursionDepth = context.getRecursionDepth();
        AssertUtils.notEmpty(parentTask, "父任务不能为空");
        AssertUtils.notEmpty(parentTask.getId(), "父任务ID不能为空");
        int nextDepth = recursionDepth == null ? 1 : recursionDepth + 1;
        return internalCommandDispatchExecutor.dispatchInternal(subRequest, progressConsumer, parentTask.getId(), nextDepth);
    }
}
