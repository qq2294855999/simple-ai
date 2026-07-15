package com.simple.ai.service.command;

import com.simple.ai.common.dto.command.AgentExecutorResponse;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 原子命令异步响应等待组件。
 *
 * <p>调度服务通过 WebSocket 下发原子命令后，通过此组件注册等待任务。
 * 执行客户端回传结果时，WebSocket 端点调用 {@link #complete(String, AgentExecutorResponse)} 完成等待。</p>
 *
 * @author qty
 */
@Component
public class AtomicCommandResponseWaiter {

    /**
     * 最大等待秒数
     */
    private static final long DEFAULT_TIMEOUT_SECONDS = 60;

    /**
     * 等待中的任务集合，key 为 taskId
     */
    private final Map<String, CompletableFuture<AgentExecutorResponse>> waitingTasks = new ConcurrentHashMap<>();

    /**
     * 注册等待任务。
     *
     * @param taskId 任务主键
     * @return 异步等待结果
     */
    public CompletableFuture<AgentExecutorResponse> register(String taskId) {
        CompletableFuture<AgentExecutorResponse> future = new CompletableFuture<>();

        // 将异步结果注册到等待集合中
        waitingTasks.put(taskId, future);
        return future.orTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 完成等待任务。
     *
     * @param taskId 任务主键
     * @param response 执行结果
     */
    public void complete(String taskId, AgentExecutorResponse response) {

        // 从等待集合中取出对应任务
        CompletableFuture<AgentExecutorResponse> future = waitingTasks.remove(taskId);
        if (future != null) {
            future.complete(response);
        }
    }
}
