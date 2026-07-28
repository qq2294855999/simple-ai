package com.simple.ai.service.command;

import com.simple.ai.common.dto.command.*;
import com.simple.ai.common.service.command.AtomicCommandExecutor;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.core.utils.JsonUtils;
import com.simple.common.websocket.utils.WebSocketUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 默认原子命令执行器
 *
 * <p>通过框架级 WebSocketUtils.sendSyncMsg() 向指定业务执行客户端点对点下发原子命令并同步等待结果。
 * 无可用客户端或超时时由框架抛出 RuntimeException，由上层统一处理。</p>
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

        // 构建 SEP 消息并通过框架级 sendSyncMsg 同步等待回执
        SepMessage<ExecutorCommandBatchRequest> message = buildSepMessage(batchRequest);

        // 打印 WebSocket 发送消息（debug 级别）
        log.debug("WebSocket 下发原子命令 [clientId={}, commandId={}]: {}", clientId, commandId, JsonUtils.toJsonStr(message));

        Object result = WebSocketUtils.sendSyncMsg(EXECUTOR_CHANNEL_TYPE, clientId, message, RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        // 打印 WebSocket 接收消息（debug 级别）
        log.debug("WebSocket 收到原子命令回执 [clientId={}, commandId={}]: {}", clientId, commandId, result != null ? JsonUtils.toJsonStr(result) : "null");

        // 从返回值提取执行器结果并构建响应
        return extractAndBuildResponse(request, result, commandId);
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

        // 构建 SEP 消息并通过框架级 sendSyncMsg 同步等待回执
        SepMessage<ExecutorCommandBatchRequest> message = buildSepMessage(batchRequest);

        // 打印 WebSocket 发送系统命令消息（debug 级别）
        log.debug("WebSocket 下发系统命令 [clientId={}, commandId={}]: {}", clientId, commandId, JsonUtils.toJsonStr(message));

        Object result = WebSocketUtils.sendSyncMsg(EXECUTOR_CHANNEL_TYPE, clientId, message, RESPONSE_TIMEOUT_SECONDS, TimeUnit.SECONDS);

        // 打印 WebSocket 接收系统命令回执（debug 级别）
        log.debug("WebSocket 收到系统命令回执 [clientId={}, commandId={}]: {}", clientId, commandId, result != null ? JsonUtils.toJsonStr(result) : "null");

        return extractAndBuildResponse(request, result, commandId);
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
     * 构建 SEP 外层消息封装批量命令。
     *
     * @param batchRequest 批量命令请求
     * @return SEP 消息
     */
    private SepMessage<ExecutorCommandBatchRequest> buildSepMessage(ExecutorCommandBatchRequest batchRequest) {
        SepMessage<ExecutorCommandBatchRequest> message = new SepMessage<>();
        message.setMessageType(COMMAND_BATCH_MESSAGE_TYPE);
        message.setPayload(batchRequest);
        return message;
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
     * 从 sendSyncMsg 返回值中提取执行器结果并构建标准响应。
     *
     * @param request   原子命令调用请求
     * @param result    sendSyncMsg 返回值
     * @param commandId 命令ID
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse extractAndBuildResponse(AtomicCommandInvokeRequest request, Object result, String commandId) {
        try {

            // 从返回值提取 ExecutorCommandResultResponse
            ExecutorCommandResultResponse executorResult = extractExecutorResult(result);
            return buildExecutorResponse(request, executorResult);
        } catch (Exception e) {
            log.warn("解析执行器响应失败 [commandId={}]", commandId, e);
            return buildBlockedResponse(request, "解析执行器响应失败: " + e.getMessage());
        }
    }

    /**
     * 从 sendSyncMsg 返回值中提取 ExecutorCommandResultResponse
     * <p>sendSyncMsg 返回的是回执中 data 字段的值，即 SepMessage JSON 对象。
     * 需要将其反序列化为 SepMessage 后提取 payload 再转为 ExecutorCommandResultResponse。</p>
     *
     * @param result sendSyncMsg 返回值（JSONObject/Map）
     * @return 执行器命令结果响应
     */
    private ExecutorCommandResultResponse extractExecutorResult(Object result) {
        AssertUtils.notEmpty(result, "执行器返回数据为空");
        try {
            String resultJson = JsonUtils.toJsonStr(result);
            SepMessage<?> replyMessage = JsonUtils.toJsonObj(resultJson, SepMessage.class);
            Object payload = replyMessage.getPayload();
            AssertUtils.notEmpty(payload, "执行器返回 payload 为空");
            String payloadJson = JsonUtils.toJsonStr(payload);
            return JsonUtils.toJsonObj(payloadJson, ExecutorCommandResultResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("解析执行器返回数据失败", e);
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

        // 透传执行器返回的原始数据（如 system.capability 的命令列表），供上层解析
        response.setData(executorResult.getData());
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
