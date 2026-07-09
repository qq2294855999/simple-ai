package com.simple.ai.websocket.command;

import com.simple.ai.common.dto.command.CommandDispatchProgressEvent;
import com.simple.ai.common.dto.command.CommandDispatchRequest;
import com.simple.ai.common.service.command.CommandDispatchService;
import com.simple.common.core.utils.JsonUtils;
import com.simple.common.websocket.common.annotation.WebSocketListening;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
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
     * @param request 命令调度请求
     * @param context WebSocket 通道上下文
     */
    @WebSocketListening(type = "agent-command", cliKey = "default")
    public void handle(CommandDispatchRequest request, ChannelHandlerContext context) {

        // 调用流式命令调度服务，逐步向当前 WebSocket 通道写回进度事件
        commandDispatchService.dispatchStream(request, event -> writeProgressEvent(context, event));
    }

    /**
     * 写回 WebSocket 调度进度事件。
     *
     * @param context WebSocket 通道上下文
     * @param event 调度进度事件
     */
    private void writeProgressEvent(ChannelHandlerContext context, CommandDispatchProgressEvent event) {

        // 将调度进度事件序列化后写回当前通道
        String responseJson = JsonUtils.toJsonStr(event);
        TextWebSocketFrame frame = new TextWebSocketFrame(responseJson);
        context.channel().writeAndFlush(frame);
    }
}
