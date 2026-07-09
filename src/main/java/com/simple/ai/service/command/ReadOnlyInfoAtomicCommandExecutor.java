package com.simple.ai.service.command;

import com.simple.ai.common.dto.command.AtomicCommandInvokeRequest;
import com.simple.ai.common.dto.command.AtomicCommandInvokeResponse;
import com.simple.ai.common.service.command.AtomicCommandExecutor;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.core.utils.JsonUtils;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 只读信息原子命令执行器。
 *
 * @author qty
 */
@Component
public class ReadOnlyInfoAtomicCommandExecutor implements AtomicCommandExecutor {

    /**
     * 只读角色标识
     */
    private static final String READ_ROLE = "READ";

    /**
     * 查询角色标识
     */
    private static final String QUERY_ROLE = "QUERY";

    /**
     * 信息角色标识
     */
    private static final String INFO_ROLE = "INFO";

    @Override
    public boolean supports(AtomicCommandInvokeRequest request) {

        // 命令作用命中只读信息类时使用当前执行器
        if (isReadOnlyRole(request.getAtomicCommandRole())) {
            return true;
        }
        return isReadOnlyCommand(request.getCommandContent());
    }

    @Override
    public AtomicCommandInvokeResponse execute(AtomicCommandInvokeRequest request) {

        // 参数校验：任务ID不能为空
        AssertUtils.notEmpty(request.getTaskId(), "任务ID不能为空");

        // 参数校验：命令内容不能为空
        AssertUtils.notEmpty(request.getCommandContent(), "命令内容不能为空");

        // 构建只读信息类执行结果
        return buildReadOnlyResponse(request);
    }

    /**
     * 判断命令作用是否为只读信息类。
     *
     * @param role 命令作用
     * @return 是否只读信息类
     */
    private boolean isReadOnlyRole(String role) {

        // 命令作用为空时交由命令内容继续判断
        if (role == null || role.isBlank()) {
            return false;
        }
        String upperRole = role.toUpperCase(Locale.ROOT);
        return upperRole.contains(READ_ROLE) || upperRole.contains(QUERY_ROLE) || upperRole.contains(INFO_ROLE);
    }

    /**
     * 判断命令内容是否为只读信息类。
     *
     * @param commandContent 命令内容
     * @return 是否只读信息类
     */
    private boolean isReadOnlyCommand(String commandContent) {

        // 命令内容为空时不匹配专用执行器
        if (commandContent == null || commandContent.isBlank()) {
            return false;
        }
        String upperCommandContent = commandContent.toUpperCase(Locale.ROOT);
        return upperCommandContent.contains(READ_ROLE) || upperCommandContent.contains(QUERY_ROLE) || upperCommandContent.contains(INFO_ROLE);
    }

    /**
     * 构建只读信息类响应。
     *
     * @param request 原子命令调用请求
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse buildReadOnlyResponse(AtomicCommandInvokeRequest request) {
        AtomicCommandInvokeResponse response = new AtomicCommandInvokeResponse();
        response.setSuccess(Boolean.TRUE);
        response.setResponseContent(JsonUtils.toJsonStr(request));
        response.setFailureReason("");
        return response;
    }

}
