package com.simple.ai.websocket.command;

import com.simple.ai.common.dto.command.CommandDispatchProgressEvent;
import com.simple.ai.common.dto.command.CommandDispatchRequest;
import com.simple.ai.common.service.command.CommandDispatchService;
import com.simple.common.core.utils.JsonUtils;
import com.simple.common.websocket.common.annotation.WebSocketListening;
import com.simple.common.websocket.common.entity.WebSocketRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 智能体命令 WebSocket 入口。
 *
 * @author qty
 */
@Component
public class CommandWebSocketEndpoint {

    /**
     * 智能体命令调度服务
     */
    @Autowired
    private CommandDispatchService commandDispatchService;

    /**
     * 处理 WebSocket 智能体命令。
     *
     * @param request 命令调度请求（框架自动注入 type/cliKey）
     */
    @WebSocketListening(type = "agent-command")
    public void handle(WebSocketRequest<CommandDispatchRequest> request) {

        // 调用流式命令调度服务，通过 request.reply() 向当前客户端逐条写回进度事件
        commandDispatchService.dispatchStream(request.getData(), event -> writeProgressEvent(request, event));
    }

    /**
     * 通过框架内置 reply 方法写回调度进度事件。
     *
     * @param request WebSocket 请求（含 type/cliKey）
     * @param event 调度进度事件
     */
    private void writeProgressEvent(WebSocketRequest<CommandDispatchRequest> request, CommandDispatchProgressEvent event) {

        // 将调度进度事件序列化后通过 request.reply() 点对点发送给当前客户端
        String responseJson = JsonUtils.toJsonStr(event);
        request.reply(responseJson);
    }
}
