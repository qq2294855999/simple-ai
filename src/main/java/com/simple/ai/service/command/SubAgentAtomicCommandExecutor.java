package com.simple.ai.service.command;

import com.simple.ai.common.dto.command.AtomicCommandInvokeRequest;
import com.simple.ai.common.dto.command.AtomicCommandInvokeResponse;
import com.simple.ai.common.service.command.AtomicCommandExecutor;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.core.utils.JsonUtils;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 子智能体原子命令执行器。
 *
 * @author qty
 */
@Component
public class SubAgentAtomicCommandExecutor implements AtomicCommandExecutor {

    /**
     * 子智能体角色标识
     */
    private static final String SUB_AGENT_ROLE = "SUB_AGENT";

    /**
     * 子智能体中文标识
     */
    private static final String SUB_AGENT_NAME = "子智能体";

    @Override
    public boolean supports(AtomicCommandInvokeRequest request) {

        // 命令作用命中子智能体时使用当前执行器
        if (isSubAgentText(request.getAtomicCommandRole())) {
            return true;
        }
        return isSubAgentText(request.getCommandContent());
    }

    @Override
    public AtomicCommandInvokeResponse execute(AtomicCommandInvokeRequest request) {

        // 参数校验：任务ID不能为空
        AssertUtils.notEmpty(request.getTaskId(), "任务ID不能为空");

        // 参数校验：命令内容不能为空
        AssertUtils.notEmpty(request.getCommandContent(), "命令内容不能为空");

        // 构建子智能体识别响应，实际递归调度由核心服务携带父任务上下文处理
        return buildSubAgentResponse(request);
    }

    /**
     * 判断文本是否为子智能体命令。
     *
     * @param text 命令文本
     * @return 是否子智能体命令
     */
    private boolean isSubAgentText(String text) {

        // 文本为空时不匹配子智能体执行器
        if (text == null || text.isBlank()) {
            return false;
        }
        String upperText = text.toUpperCase(Locale.ROOT);
        return upperText.contains(SUB_AGENT_ROLE) || text.contains(SUB_AGENT_NAME);
    }

    /**
     * 构建子智能体识别响应。
     *
     * @param request 原子命令调用请求
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse buildSubAgentResponse(AtomicCommandInvokeRequest request) {
        AtomicCommandInvokeResponse response = new AtomicCommandInvokeResponse();
        response.setSuccess(Boolean.TRUE);
        response.setResponseContent(JsonUtils.toJsonStr(request));
        response.setFailureReason("");
        return response;
    }
}
