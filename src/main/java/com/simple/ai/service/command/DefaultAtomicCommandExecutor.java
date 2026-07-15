package com.simple.ai.service.command;

import com.simple.ai.common.dto.command.AgentExecutorResponse;
import com.simple.ai.common.dto.command.AtomicCommandInvokeRequest;
import com.simple.ai.common.dto.command.AtomicCommandInvokeResponse;
import com.simple.ai.common.service.command.AtomicCommandExecutor;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.core.utils.JsonUtils;
import com.simple.common.websocket.utils.WebSocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 默认原子命令执行器。
 *
 * <p>优先通过 WebSocket 向已连接的业务执行客户端下发原子命令并等待结果；
 * 无可用客户端或超时时回退到安全阻断响应。</p>
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
     * WebSocket 响应等待超时秒数
     */
    private static final long RESPONSE_TIMEOUT_SECONDS = 60;

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

        // 通过 WebSocket 下发给业务执行客户端
        WebSocketUtils.sendMsg(EXECUTOR_CHANNEL_TYPE, JsonUtils.toJsonStr(request));

        // 注册异步等待，超时或异常时回退到阻断响应
        return waitForExecutorResponse(request);
    }

    /**
     * 等待业务执行客户端回传结果。
     *
     * @param request 原子命令调用请求
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse waitForExecutorResponse(AtomicCommandInvokeRequest request) {
        CompletableFuture<AgentExecutorResponse> future = responseWaiter.register(request.getTaskId());
        try {

            // 阻塞等待执行客户端回传结果，超时则抛异常
            AgentExecutorResponse executorResponse = future.get(RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return buildExecutorResponse(request, executorResponse);
        } catch (TimeoutException e) {
            log.warn("等待业务执行客户端响应超时 [taskId={}]", request.getTaskId());
            return buildBlockedResponse(request, "等待业务执行客户端响应超时");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("等待业务执行客户端响应被中断 [taskId={}]", request.getTaskId());
            return buildBlockedResponse(request, "等待业务执行客户端响应被中断");
        } catch (ExecutionException e) {
            log.error("等待业务执行客户端响应异常 [taskId={}]", request.getTaskId(), e);
            return buildBlockedResponse(request, "等待业务执行客户端响应异常");
        }
    }

    /**
     * 将执行客户端回传结果转换为标准原子命令响应。
     *
     * @param request 原子命令调用请求
     * @param executorResponse 执行客户端回传结果
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse buildExecutorResponse(AtomicCommandInvokeRequest request,
                                                               AgentExecutorResponse executorResponse) {
        AtomicCommandInvokeResponse response = new AtomicCommandInvokeResponse();
        response.setSuccess(executorResponse.getSuccess());
        response.setResponseContent(executorResponse.getResponseContent());
        response.setFailureReason(executorResponse.getFailureReason());
        return response;
    }

    /**
     * 构建安全阻断响应。
     *
     * @param request 原子命令调用请求
     * @param reason 阻断原因
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
