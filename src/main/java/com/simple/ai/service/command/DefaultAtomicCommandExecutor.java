package com.simple.ai.service.command;

import com.simple.ai.common.dto.command.AtomicCommandInvokeRequest;
import com.simple.ai.common.dto.command.AtomicCommandInvokeResponse;
import com.simple.ai.common.service.command.AtomicCommandExecutor;
import com.simple.common.core.utils.AssertUtils;
import org.springframework.stereotype.Component;

/**
 * 默认原子命令执行器。
 *
 * @author qty
 */
@Component
public class DefaultAtomicCommandExecutor implements AtomicCommandExecutor {

    @Override
    public boolean supports(AtomicCommandInvokeRequest request) {
        return true;
    }

    @Override
    public AtomicCommandInvokeResponse execute(AtomicCommandInvokeRequest request) {
        
        // 参数校验：任务ID不能为空
        AssertUtils.notEmpty(request.getTaskId(), "任务ID不能为空");

        // 参数校验：命令内容不能为空
        AssertUtils.notEmpty(request.getCommandContent(), "命令内容不能为空");

        // 默认执行器不直接执行系统命令，只返回待人工处理结果
        return buildManualResponse(request);
    }

    /**
     * 构建待人工处理响应。
     *
     * @param request 原子命令调用请求
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse buildManualResponse(AtomicCommandInvokeRequest request) {
        AtomicCommandInvokeResponse response = new AtomicCommandInvokeResponse();
        response.setSuccess(Boolean.TRUE);
        response.setResponseContent("默认安全执行器已接收命令，命令内容需由人工或专用执行器处理：" + request.getCommandContent());
        return response;
    }

}
