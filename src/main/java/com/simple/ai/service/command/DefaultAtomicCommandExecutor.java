package com.simple.ai.service.command;

import com.simple.ai.common.dto.command.*;
import com.simple.ai.common.service.command.AtomicCommandExecutor;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.core.utils.JsonUtils;
import com.simple.common.websocket.utils.WebSocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 默认原子命令执行器。
 *
 * <p>优先通过 WebSocket 向指定业务执行客户端点对点下发原子命令并等待结果；
 * 无可用客户端或超时时回退到安全阻断响应。</p>
 *
 * <p>支持 system.capability 内置命令特殊处理。</p>
 *
 * @author qty
 */
@Component
public class DefaultAtomicCommandExecutor implements AtomicCommandExecutor {

    private static final Logger log = LoggerFactory.getLogger(DefaultAtomicCommandExecutor.class);

    /**
     * 执行客户端 WebSocket 通道类型
     */
    private static final String EXECUTOR_CHANNEL_TYPE = "agent-executor";

    /**
     * SEP 批量命令消息类型
     */
    private static final String COMMAND_BATCH_MESSAGE_TYPE = "COMMAND_BATCH";

    /**
     * WebSocket 响应等待超时秒数
     */
    private static final long RESPONSE_TIMEOUT_SECONDS = 60;

    /**
     * 内置系统命令前缀
     */
    private static final String SYSTEM_COMMAND_PREFIX = "system.";

    /**
     * 原子命令响应等待组件
     */
    @Autowired
    private AtomicCommandResponseWaiter responseWaiter;

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

        // system.capability 特殊处理：组装单条能力查询命令并等待结果
        if (isSystemCommand(request.getCommandContent())) {
            return executeSystemCommand(request);
        }

        // 编译批量命令请求，支持单条命令以批量协议下发
        ExecutorCommandBatchRequest batchRequest = buildBatchRequest(request);
        String commandId = resolveCommandId(batchRequest);
        String clientId = resolveClientId(request);

        // 先注册等待器（按 commandId），再点对点发送，修复竞态 BUG
        CompletableFuture<ExecutorCommandResultResponse> future = responseWaiter.register(commandId, clientId);
        sendBatchCommand(clientId, batchRequest);

        // 等待执行客户端回传结果
        return waitForExecutorResponse(request, future, commandId);
    }

    /**
     * 判断是否为内置系统命令。
     *
     * @param commandContent 命令内容
     * @return 是否为系统命令
     */
    private boolean isSystemCommand(String commandContent) {
        return commandContent != null && commandContent.startsWith(SYSTEM_COMMAND_PREFIX);
    }

    /**
     * 执行内置系统命令。
     *
     * @param request 原子命令调用请求
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse executeSystemCommand(AtomicCommandInvokeRequest request) {
        String commandId = UUID.randomUUID().toString();
        request.setCommandId(commandId);
        String clientId = resolveClientId(request);

        // 编译 system.capability 单条命令
        ExecutorCommandItem item = new ExecutorCommandItem().setCommandId(commandId)
                                                            .setSequenceNo(1)
                                                            .setAtomicCommandCode(request.getCommandContent())
                                                            .setArgs(request.getRequestParams())
                                                            .setTimeoutMs((int) TimeUnit.SECONDS.toMillis(RESPONSE_TIMEOUT_SECONDS));
        ExecutorCommandBatchRequest batchRequest = new ExecutorCommandBatchRequest().setDispatchId(UUID.randomUUID().toString())
                                                                                    .setTaskId(request.getTaskId())
                                                                                    .setClientId(clientId)
                                                                                    .setStopOnFailure(Boolean.TRUE)
                                                                                    .setCommands(Collections.singletonList(item));

        // 先注册等待器，再点对点发送
        CompletableFuture<ExecutorCommandResultResponse> future = responseWaiter.register(commandId, clientId);
        sendBatchCommand(clientId, batchRequest);

        return waitForExecutorResponse(request, future, commandId);
    }

    /**
     * 编译批量命令请求。
     *
     * @param request 原子命令调用请求
     * @return 批量命令请求
     */
    private ExecutorCommandBatchRequest buildBatchRequest(AtomicCommandInvokeRequest request) {
        String commandId = request.getCommandId() != null ? request.getCommandId() : UUID.randomUUID().toString();
        request.setCommandId(commandId);

        ExecutorCommandItem item = new ExecutorCommandItem().setCommandId(commandId)
                                                            .setSequenceNo(1)
                                                            .setAtomicCommandCode(request.getCommandContent())
                                                            .setArgs(request.getRequestParams())
                                                            .setTimeoutMs((int) TimeUnit.SECONDS.toMillis(RESPONSE_TIMEOUT_SECONDS));

        ExecutorCommandBatchRequest batchRequest = new ExecutorCommandBatchRequest().setDispatchId(UUID.randomUUID().toString())
                                                                                    .setTaskId(request.getTaskId())
                                                                                    .setClientId(resolveClientId(request))
                                                                                    .setStopOnFailure(Boolean.TRUE)
                                                                                    .setCommands(Collections.singletonList(item));
        return batchRequest;
    }

    /**
     * 点对点发送批量命令。
     *
     * @param clientId     客户端ID（即 cliKey）
     * @param batchRequest 批量命令请求
     */
    private void sendBatchCommand(String clientId, ExecutorCommandBatchRequest batchRequest) {
        AssertUtils.notEmpty(clientId, "客户端ID不能为空");

        // 使用 SEP 外层消息封装批量命令，保证执行器按 messageType 分发协议负载。
        SepMessage<ExecutorCommandBatchRequest> message = new SepMessage<>();
        message.setMessageType(COMMAND_BATCH_MESSAGE_TYPE);
        message.setPayload(batchRequest);
        WebSocketUtils.sendMsg(EXECUTOR_CHANNEL_TYPE, clientId, JsonUtils.toJsonStr(message));
    }

    /**
     * 解析客户端ID。
     *
     * @param request 原子命令调用请求
     * @return 客户端ID
     */
    private String resolveClientId(AtomicCommandInvokeRequest request) {
        AssertUtils.notEmpty(request.getClientId(), "原子命令缺少客户端ID");
        return request.getClientId();
    }

    /**
     * 解析命令ID。
     *
     * @param batchRequest 批量命令请求
     * @return 命令ID
     */
    private String resolveCommandId(ExecutorCommandBatchRequest batchRequest) {
        List<ExecutorCommandItem> commands = batchRequest.getCommands();
        if (commands != null && !commands.isEmpty()) {
            return commands.get(0).getCommandId();
        }
        return UUID.randomUUID().toString();
    }

    /**
     * 等待业务执行客户端回传结果。
     *
     * @param request 原子命令调用请求
     * @param future  异步等待结果
     * @param commandId 命令ID
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse waitForExecutorResponse(AtomicCommandInvokeRequest request, CompletableFuture<ExecutorCommandResultResponse> future, String commandId) {
        try {

            // 阻塞等待执行客户端回传结果，超时则抛异常
            ExecutorCommandResultResponse executorResult = future.get(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return buildExecutorResponse(request, executorResult);
        } catch (TimeoutException e) {
            log.warn("等待执行客户端响应超时 [commandId={}]", commandId);
            return buildBlockedResponse(request, "等待执行客户端响应超时");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("等待执行客户端响应被中断 [commandId={}]", commandId);
            return buildBlockedResponse(request, "等待执行客户端响应被中断");
        } catch (ExecutionException e) {
            log.error("等待执行客户端响应异常 [commandId={}]", commandId, e);
            return buildBlockedResponse(request, "等待执行客户端响应异常: " + e.getMessage());
        }
    }

    /**
     * 将执行客户端回传结果转换为标准原子命令响应。
     *
     * @param request        原子命令调用请求
     * @param executorResult 执行客户端回传结果
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse buildExecutorResponse(AtomicCommandInvokeRequest request, ExecutorCommandResultResponse executorResult) {
        AtomicCommandInvokeResponse response = new AtomicCommandInvokeResponse();
        response.setSuccess(executorResult.getSuccess());
        response.setResponseContent(executorResult.getMessage() != null ? executorResult.getMessage() : "");
        response.setFailureReason(executorResult.getError() != null ? executorResult.getError().getDetail() : "");
        return response;
    }

    /**
     * 构建安全阻断响应。
     *
     * @param request 原子命令调用请求
     * @param reason  阻断原因
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse buildBlockedResponse(AtomicCommandInvokeRequest request, String reason) {
        AtomicCommandInvokeResponse response = new AtomicCommandInvokeResponse();
        response.setSuccess(Boolean.FALSE);
        response.setResponseContent(JsonUtils.toJsonStr(request));
        response.setFailureReason(reason);
        return response;
    }
}
