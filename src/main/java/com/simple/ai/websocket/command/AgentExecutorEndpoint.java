package com.simple.ai.websocket.command;

import com.simple.ai.common.dto.command.AgentExecutorResponse;
import com.simple.ai.service.command.AtomicCommandResponseWaiter;
import com.simple.common.core.utils.JsonUtils;
import com.simple.common.websocket.common.annotation.WebSocketListening;
import com.simple.common.websocket.common.entity.WebSocketRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 业务执行客户端 WebSocket 入口。
 *
 * <p>执行客户端通过此端点连接并接收原子命令，执行完成后回传结果。</p>
 *
 * @author qty
 */
@Component
public class AgentExecutorEndpoint {

    /**
     * 原子命令响应等待组件
     */
    @Autowired
    private AtomicCommandResponseWaiter responseWaiter;

    /**
     * 接收执行客户端回传的原子命令执行结果。
     *
     * @param request 执行结果请求（框架自动注入 type/cliKey）
     */
    @WebSocketListening(type = "agent-executor", cliKey = "default")
    public void handle(WebSocketRequest<AgentExecutorResponse> request) {

        // 解析执行客户端回传的结果
        AgentExecutorResponse response = request.getData();
        if (response == null) {
            return;
        }

        // 通过 taskId 完成等待中的调度流程
        responseWaiter.complete(response.getTaskId(), response);
    }
}
