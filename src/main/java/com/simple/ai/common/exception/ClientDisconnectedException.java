package com.simple.ai.common.exception;

/**
 * 客户端断开连接异常。
 * <p>当 SSE 客户端断开连接时抛出，用于中断后台任务执行。</p>
 *
 * @author qty
 */
public class ClientDisconnectedException extends RuntimeException {

    public ClientDisconnectedException(String message, Throwable cause) {
        super(message, cause);
    }
}
