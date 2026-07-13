package com.simple.ai.common.dto.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 智能体 AI 调用请求参数。
 *
 * @author qty
 */
@Data
@Schema(title = "智能体 AI 调用请求参数")
public class AgentAiRequest {

    /**
     * 智能体主键。
     */
    @Schema(description = "智能体主键")
    private String agentId;

    /**
     * 显式模型主键。
     */
    @Schema(description = "显式模型主键")
    private String modelId;

    /**
     * 提示词内容
     */
    @Schema(description = "提示词内容")
    @NotEmpty(message = "提示词内容不能为空")
    private String promptContent;

    /**
     * 用户命令内容
     */
    @Schema(description = "用户命令内容")
    @NotEmpty(message = "用户命令内容不能为空")
    private String commandContent;

    /**
     * 会话摘要
     */
    @Schema(description = "会话摘要")
    private String sessionSummary;

}
