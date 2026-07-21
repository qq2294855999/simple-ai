package com.simple.ai.service.command;

import com.simple.ai.common.dto.command.ExecutorCommandResultResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * 原子命令异步响应等待组件。
 *
 * <p>调度服务通过 WebSocket 下发原子命令后，通过此组件按 commandId 注册等待任务。
 * 执行客户端回传 COMMAND_RESULT 时，WebSocket 端点调用 {@link #complete(String, ExecutorCommandResultResponse)} 完成等待。</p>
 *
 * <p>支持超时自动清理和断连批量清理。</p>
 *
 * @author qty
 */
@Component
public class AtomicCommandResponseWaiter {

    private static final Logger log = LoggerFactory.getLogger(AtomicCommandResponseWaiter.class);

    /**
     * 最大等待秒数
     */
    private static final long DEFAULT_TIMEOUT_SECONDS = 60;

    /**
     * 等待中的命令集合，key 为 commandId
     */
    private final Map<String, CompletableFuture<ExecutorCommandResultResponse>> waitingCommands = new ConcurrentHashMap<>();

    /**
     * 等待中的命令超时记录，key 为 commandId
     */
    private final Map<String, CommandTimeoutEntry> timeoutEntries = new ConcurrentHashMap<>();

    /**
     * 客户端与等待命令映射，key 为 clientId，value 为该客户端下所有等待 commandId
     */
    private final Map<String, java.util.Set<String>> clientCommandMap = new ConcurrentHashMap<>();

    /**
     * 注册等待命令。
     *
     * @param commandId 命令主键
     * @param clientId  客户端ID
     * @return 异步等待结果
     */
    public CompletableFuture<ExecutorCommandResultResponse> register(String commandId, String clientId) {
        CompletableFuture<ExecutorCommandResultResponse> future = new CompletableFuture<>();

        // 将异步结果注册到等待集合中
        waitingCommands.put(commandId, future);

        // 记录客户端与命令的映射关系，用于断连批量清理
        clientCommandMap.computeIfAbsent(clientId, k -> java.util.concurrent.ConcurrentHashMap.newKeySet()).add(commandId);

        // 注册超时自动清理任务
        CommandTimeoutEntry timeoutEntry = new CommandTimeoutEntry(commandId, System.currentTimeMillis() + DEFAULT_TIMEOUT_SECONDS * 1000);
        timeoutEntries.put(commandId, timeoutEntry);

        return future.orTimeout(DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS).whenComplete((result, throwable) -> {
            // 完成或超时时清理资源
            cleanupWaitResources(commandId, clientId);
        });
    }

    /**
     * 完成等待命令。
     *
     * @param commandId 命令主键
     * @param result    执行结果
     */
    public void complete(String commandId, ExecutorCommandResultResponse result) {

        // 从等待集合中取出对应命令
        CompletableFuture<ExecutorCommandResultResponse> future = waitingCommands.remove(commandId);
        if (future != null) {
            timeoutEntries.remove(commandId);
            future.complete(result);
        }
    }

    /**
     * 清理指定客户端的全部等待命令（客户端断连时调用）。
     *
     * @param clientId 客户端ID
     */
    public void clearByClientId(String clientId) {
        java.util.Set<String> commandIds = clientCommandMap.remove(clientId);
        if (commandIds == null || commandIds.isEmpty()) {
            return;
        }
        log.warn("客户端[{}]断连，清理 {} 个等待命令", clientId, commandIds.size());
        for (String commandId : commandIds) {
            CompletableFuture<ExecutorCommandResultResponse> future = waitingCommands.remove(commandId);
            if (future != null && !future.isDone()) {
                timeoutEntries.remove(commandId);
                future.completeExceptionally(new RuntimeException("客户端[" + clientId + "]已断连"));
            }
        }
    }

    /**
     * 清理等待资源。
     *
     * @param commandId 命令主键
     * @param clientId  客户端ID
     */
    private void cleanupWaitResources(String commandId, String clientId) {
        waitingCommands.remove(commandId);
        timeoutEntries.remove(commandId);
        java.util.Set<String> commandIds = clientCommandMap.get(clientId);
        if (commandIds != null) {
            commandIds.remove(commandId);
        }
    }

    /**
     * 命令超时记录。
     */
    private static class CommandTimeoutEntry implements Delayed {
        private final String commandId;

        private final long expireAtMs;

        CommandTimeoutEntry(String commandId, long expireAtMs) {
            this.commandId = commandId;
            this.expireAtMs = expireAtMs;
        }

        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(expireAtMs - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }

        @Override
        public int compareTo(Delayed other) {
            return Long.compare(this.expireAtMs, ((CommandTimeoutEntry) other).expireAtMs);
        }
    }
}
