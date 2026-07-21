package com.simple.ai.websocket.command;

import com.simple.ai.common.dto.command.ExecutorCommandResultResponse;
import com.simple.ai.common.dto.command.SepMessage;
import com.simple.ai.service.command.AtomicCommandResponseWaiter;
import com.simple.common.websocket.common.annotation.WebSocketListening;
import com.simple.common.websocket.common.entity.WebSocketRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 业务执行客户端 WebSocket 入口。
 *
 * <p>执行客户端通过此端点连接并接收原子命令，执行完成后回传 COMMAND_RESULT。</p>
 *
 * @author qty
 */
@Component
public class AgentExecutorEndpoint {

    private static final Logger log = LoggerFactory.getLogger(AgentExecutorEndpoint.class);

    /**
     * SEP 命令结果消息类型。
     */
    private static final String COMMAND_RESULT_MESSAGE_TYPE = "COMMAND_RESULT";

    /**
     * 原子命令响应等待组件
     */
    @Autowired
    private AtomicCommandResponseWaiter responseWaiter;

    /**
     * 接收执行客户端回传的 COMMAND_RESULT。
     *
     * <p>不限制 cliKey，动态接收所有 agent-executor 类型消息。
     * 按 commandId 完成对应等待器，支持按 clientId 批量清理。</p>
     *
     * @param request SEP 外层消息请求（框架自动注入 type/cliKey）
     */
    @WebSocketListening(type = "agent-executor")
    public void handle(WebSocketRequest<SepMessage<ExecutorCommandResultResponse>> request) {

        // 读取 SEP 外层消息，拒绝缺少协议类型的无效数据。
        SepMessage<ExecutorCommandResultResponse> message = request.getData();
        if (message == null || message.getMessageType() == null) {
            return;
        }

        // 心跳确认不进入命令等待器，避免把非业务结果写入执行链路。
        if (!COMMAND_RESULT_MESSAGE_TYPE.equals(message.getMessageType())) {
            return;
        }

        // 解析执行客户端回传的 COMMAND_RESULT 负载。
        ExecutorCommandResultResponse result = message.getPayload();
        if (result == null || result.getCommandId() == null) {
            return;
        }

        // 通过 commandId 完成等待中的调度流程。
        responseWaiter.complete(result.getCommandId(), result);
    }
}
