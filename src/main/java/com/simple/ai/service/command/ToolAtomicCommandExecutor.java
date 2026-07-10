package com.simple.ai.service.command;

import com.simple.ai.common.dto.command.AtomicCommandInvokeRequest;
import com.simple.ai.common.dto.command.AtomicCommandInvokeResponse;
import com.simple.ai.common.service.command.AtomicCommandExecutor;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.core.utils.JsonUtils;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 工具类原子命令执行器。
 *
 * @author qty
 */
@Component
public class ToolAtomicCommandExecutor implements AtomicCommandExecutor {

    /**
     * 工具角色标识
     */
    private static final String TOOL_ROLE = "TOOL";

    /**
     * 调用角色标识
     */
    private static final String CALL_ROLE = "CALL";

    /**
     * 执行角色标识
     */
    private static final String EXECUTE_ROLE = "EXECUTE";

    /**
     * 工具中文标识
     */
    private static final String TOOL_NAME = "工具";

    @Override
    public boolean supports(AtomicCommandInvokeRequest request) {

        // 命令作用命中工具类时使用当前执行器
        if (isToolText(request.getAtomicCommandRole())) {
            return true;
        }
        return isToolText(request.getCommandContent());
    }

    @Override
    public AtomicCommandInvokeResponse execute(AtomicCommandInvokeRequest request) {

        // 参数校验：任务ID不能为空
        AssertUtils.notEmpty(request.getTaskId(), "任务ID不能为空");

        // 参数校验：命令内容不能为空
        AssertUtils.notEmpty(request.getCommandContent(), "命令内容不能为空");

        // 构建工具类阻断响应，避免未授权工具命令被误判为执行成功
        return buildBlockedToolResponse(request);
    }

    /**
     * 判断文本是否为工具类命令。
     *
     * @param text 命令文本
     * @return 是否工具类命令
     */
    private boolean isToolText(String text) {

        // 文本为空时不匹配工具类执行器
        if (text == null || text.isBlank()) {
            return false;
        }
        String upperText = text.toUpperCase(Locale.ROOT);
        return upperText.contains(TOOL_ROLE)
                || upperText.contains(CALL_ROLE)
                || upperText.contains(EXECUTE_ROLE)
                || text.contains(TOOL_NAME);
    }

    /**
     * 构建工具类命令阻断响应。
     *
     * @param request 原子命令调用请求
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse buildBlockedToolResponse(AtomicCommandInvokeRequest request) {
        AtomicCommandInvokeResponse response = new AtomicCommandInvokeResponse();
        response.setSuccess(Boolean.FALSE);
        response.setResponseContent(JsonUtils.toJsonStr(request));
        response.setFailureReason("工具类命令已识别，但缺少白名单专用工具能力，已按安全策略阻断执行");
        return response;
    }

}
