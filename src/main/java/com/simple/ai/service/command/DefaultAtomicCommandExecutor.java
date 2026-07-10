package com.simple.ai.service.command;

import com.simple.ai.common.dto.command.AtomicCommandInvokeRequest;
import com.simple.ai.common.dto.command.AtomicCommandInvokeResponse;
import com.simple.ai.common.service.command.AtomicCommandExecutor;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.core.utils.JsonUtils;
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

        // 默认执行器不直接执行未知命令，避免未匹配原子命令被误判为成功
        return buildBlockedResponse(request);
    }

    /**
     * 构建默认安全阻断响应。
     *
     * @param request 原子命令调用请求
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse buildBlockedResponse(AtomicCommandInvokeRequest request) {
        AtomicCommandInvokeResponse response = new AtomicCommandInvokeResponse();
        response.setSuccess(Boolean.FALSE);
        response.setResponseContent(JsonUtils.toJsonStr(request));
        response.setFailureReason("未匹配到可执行的专用原子命令，默认安全执行器已阻断执行");
        return response;
    }

}
