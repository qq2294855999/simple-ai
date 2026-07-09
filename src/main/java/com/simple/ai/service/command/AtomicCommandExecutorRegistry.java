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
     * 查询支持当前请求的原子命令执行器。
     *
     * @param request 原子命令调用请求
     * @return 原子命令执行器
     */
    public AtomicCommandExecutor findExecutor(AtomicCommandInvokeRequest request) {
        
        // 遍历执行器列表，查找首个支持当前请求的执行器
        for (AtomicCommandExecutor executor : executors) {
            if (executor.supports(request)) {
                return executor;
            }
        }
        AssertUtils.error("原子命令执行器不存在", "未找到支持当前原子命令的执行器");
        return null;
    }

}
