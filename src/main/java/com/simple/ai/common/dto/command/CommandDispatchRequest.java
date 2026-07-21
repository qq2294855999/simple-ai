package com.simple.ai.common.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Map;

/**
 * 智能体命令调度请求参数。
 *
 * @author qty
 */
@Data
@Schema(title = "智能体命令调度请求参数")
public class CommandDispatchRequest {

    /**
     * 智能体ID
     */
    @Schema(description = "智能体ID")
    @NotEmpty(message = "智能体ID不能为空")
    private String agentId;

    /**
     * 命令名称
     */
    @Schema(description = "命令名称")
    @NotEmpty(message = "命令名称不能为空")
    private String commandName;

    /**
     * 命令内容
     */
    @Schema(description = "命令内容")
    @NotEmpty(message = "命令内容不能为空")
    private String commandContent;

    /**
     * 用户ID（用于按用户过滤资产）
     */
    @Schema(description = "用户ID")
    private String userId;

    /**
     * 客户端ID（用于点对点下发命令）
     */
    @Schema(description = "客户端ID")
    private String clientId;

    /**
     * 会话ID
     */
    @Schema(description = "会话ID")
    private String sessionId;

    /**
     * 显式模型主键。
     */
    @Schema(description = "显式模型主键")
    private String modelId;

    /**
     * 请求参数
     */
    @Schema(description = "请求参数")
    private Map<String, Object> requestParams;

}
