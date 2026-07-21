package com.simple.ai.common.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Simple Executor Protocol v1.0 WebSocket 外层消息。
 * <p>所有执行器方向的业务消息均使用 messageType 与 payload 作为统一外层结构。</p>
 *
 * @param <T> 消息负载类型
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "SEP v1.0 WebSocket 外层消息")
public class SepMessage<T> {

    /**
     * 协议消息类型。
     */
    @Schema(description = "协议消息类型，例如 COMMAND_BATCH、COMMAND_RESULT、HEARTBEAT、HEARTBEAT_ACK")
    private String messageType;

    /**
     * 协议消息负载。
     */
    @Schema(description = "协议消息负载")
    private T payload;
}
