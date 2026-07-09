package com.simple.ai.service.command;

import com.simple.ai.common.dto.command.AtomicCommandInvokeRequest;
import com.simple.ai.common.service.command.AtomicCommandExecutor;
import com.simple.common.core.utils.AssertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 原子命令执行器注册表。
 *
 * @author qty
 */
@Component
public class AtomicCommandExecutorRegistry {

    /**
     * 原子命令执行器列表
     */
    @Autowired
    private List<AtomicCommandExecutor> executors;

    /**
     * 默认原子命令执行器
     */
    @Autowired
    private DefaultAtomicCommandExecutor defaultAtomicCommandExecutor;

    /**
     * 查询支持当前请求的原子命令执行器。
     *
     * @param request 原子命令调用请求
     * @return 原子命令执行器
     */
    public AtomicCommandExecutor findExecutor(AtomicCommandInvokeRequest request) {

        // 优先匹配专用执行器，避免默认执行器提前兜底
        for (AtomicCommandExecutor executor : executors) {
            if (isDedicatedExecutorMatched(executor, request)) {
                return executor;
            }
        }

        // 专用执行器均不支持时使用默认安全执行器
        if (defaultAtomicCommandExecutor.supports(request)) {
            return defaultAtomicCommandExecutor;
        }
        AssertUtils.error("原子命令执行器不存在", "未找到支持当前原子命令的执行器");
        return null;
    }

    /**
     * 判断专用执行器是否匹配当前请求。
     *
     * @param executor 原子命令执行器
     * @param request 原子命令调用请求
     * @return 是否匹配
     */
    private boolean isDedicatedExecutorMatched(AtomicCommandExecutor executor, AtomicCommandInvokeRequest request) {

        // 默认执行器由兜底阶段统一处理
        if (executor == defaultAtomicCommandExecutor) {
            return false;
        }
        return executor.supports(request);
    }

}
