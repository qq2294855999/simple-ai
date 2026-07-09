package com.simple.ai.common.dto.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 智能体 AI 调用响应参数。
 *
 * @author qty
 */
@Data
@Schema(title = "智能体 AI 调用响应参数")
public class AgentAiResponse {

    /**
     * 是否调用成功
     */
    @Schema(description = "是否调用成功")
    private Boolean success;

    /**
     * 响应内容
     */
    @Schema(description = "响应内容")
    private String responseContent;

    /**
     * 失败原因
     */
    @Schema(description = "失败原因")
    private String failureReason;

}
