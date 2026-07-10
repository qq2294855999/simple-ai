package com.simple.ai.service.command;

import com.simple.ai.common.dto.command.AtomicCommandInvokeRequest;
import com.simple.ai.common.dto.command.AtomicCommandInvokeResponse;
import com.simple.ai.common.service.command.AtomicCommandExecutor;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.core.utils.JsonUtils;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 写入类原子命令执行器。
 *
 * @author qty
 */
@Component
public class WriteAtomicCommandExecutor implements AtomicCommandExecutor {

    /**
     * 写入角色标识
     */
    private static final String WRITE_ROLE = "WRITE";

    /**
     * 保存角色标识
     */
    private static final String SAVE_ROLE = "SAVE";

    /**
     * 更新角色标识
     */
    private static final String UPDATE_ROLE = "UPDATE";

    /**
     * 删除角色标识
     */
    private static final String DELETE_ROLE = "DELETE";

    /**
     * 写入中文标识
     */
    private static final String WRITE_NAME = "写入";

    @Override
    public boolean supports(AtomicCommandInvokeRequest request) {

        // 命令作用命中写入类时使用当前执行器
        if (isWriteText(request.getAtomicCommandRole())) {
            return true;
        }
        return isWriteText(request.getCommandContent());
    }

    @Override
    public AtomicCommandInvokeResponse execute(AtomicCommandInvokeRequest request) {

        // 参数校验：任务ID不能为空
        AssertUtils.notEmpty(request.getTaskId(), "任务ID不能为空");

        // 参数校验：命令内容不能为空
        AssertUtils.notEmpty(request.getCommandContent(), "命令内容不能为空");

        // 构建写入类阻断响应，避免未授权写入命令被误判为执行成功
        return buildBlockedWriteResponse(request);
    }

    /**
     * 判断文本是否为写入类命令。
     *
     * @param text 命令文本
     * @return 是否写入类命令
     */
    private boolean isWriteText(String text) {

        // 文本为空时不匹配写入类执行器
        if (text == null || text.isBlank()) {
            return false;
        }
        String upperText = text.toUpperCase(Locale.ROOT);
        return upperText.contains(WRITE_ROLE)
                || upperText.contains(SAVE_ROLE)
                || upperText.contains(UPDATE_ROLE)
                || upperText.contains(DELETE_ROLE)
                || text.contains(WRITE_NAME);
    }

    /**
     * 构建写入类命令阻断响应。
     *
     * @param request 原子命令调用请求
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse buildBlockedWriteResponse(AtomicCommandInvokeRequest request) {
        AtomicCommandInvokeResponse response = new AtomicCommandInvokeResponse();
        response.setSuccess(Boolean.FALSE);
        response.setResponseContent(JsonUtils.toJsonStr(request));
        response.setFailureReason("写入类命令已识别，但缺少白名单专用写入能力，已按安全策略阻断执行");
        return response;
    }
}
