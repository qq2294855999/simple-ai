package com.simple.ai.common.service.command;

import com.simple.ai.common.dto.command.AtomicCommandInvokeRequest;
import com.simple.ai.common.dto.command.AtomicCommandInvokeResponse;

/**
 * 原子命令执行器。
 *
 * @author qty
 */
public interface AtomicCommandExecutor {

    /**
     * 判断是否支持当前原子命令调用请求。
     *
     * @param request 原子命令调用请求
     * @return 是否支持
     */
    boolean supports(AtomicCommandInvokeRequest request);

    /**
     * 执行原子命令调用请求。
     *
     * @param request 原子命令调用请求
     * @return 原子命令调用响应
     */
    AtomicCommandInvokeResponse execute(AtomicCommandInvokeRequest request);

}
