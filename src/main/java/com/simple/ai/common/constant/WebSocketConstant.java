package com.simple.ai.common.constant;

/**
 * WebSocket 通道常量。
 *
 * @author qty
 */
public final class WebSocketConstant {

    /**
     * 执行客户端 WebSocket 通道类型标识。
     * <p>用于 {@link com.simple.common.websocket.utils.WebSocketUtils} 的 type 参数，
     * 标识 agent-executor 类型的 WebSocket 连接。</p>
     */
    public static final String AGENT_EXECUTOR_TYPE = "agent-executor";

    /**
     * 创建 WebSocket 通道常量。
     */
    private WebSocketConstant() {
    }

}
