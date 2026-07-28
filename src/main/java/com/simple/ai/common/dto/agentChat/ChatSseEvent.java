package com.simple.ai.common.dto.agentChat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 智能体聊天 SSE 稳定事件 DTO。
 * <p>对外只暴露五类事件类型：PROGRESS、THINKING、REPLY、FINAL、ERROR。</p>
 * <p>所有内部事件（CONTEXT_LOADING、AI_STARTED、TOOL_CALLING 等）均映射为 PROGRESS 后输出。</p>
 *
 * @author qty
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(title = "智能体聊天 SSE 稳定事件")
public class ChatSseEvent {

    /**
     * 事件类型：PROGRESS / THINKING / REPLY / FINAL / ERROR
     */
    @Schema(description = "事件类型：PROGRESS/THINKING/REPLY/FINAL/ERROR")
    private String type;

    /**
     * 事件数据（内容增量或完整消息）
     */
    @Schema(description = "事件数据")
    private String data;

    /**
     * 消息 ID（FINAL 事件时填充）
     */
    @Schema(description = "消息主键")
    private String messageId;

    /**
     * 轮次 ID
     */
    @Schema(description = "轮次主键")
    private String turnId;

    /**
     * 失败原因（ERROR 事件时填充）
     */
    @Schema(description = "失败原因")
    private String errorReason;

    /**
     * 思考内容摘要（FINAL 事件时填充）
     */
    @Schema(description = "思考内容摘要")
    private String thinkingSummary;

    /**
     * 任务主键（THINKING/REPLY/PROGRESS 事件时填充，供前端按任务分组匹配气泡）
     */
    @Schema(description = "任务主键")
    private String taskId;

    /**
     * 是否完成
     */
    @Schema(description = "是否完成")
    private Boolean completed;

    /**
     * SSE 事件类型常量。
     */
    public static final class Types {

        /**
         * 进度事件：面向用户的当前动作（上下文恢复、记忆匹配、模型开始、工具调用、保存结果等）
         */
        public static final String PROGRESS = "PROGRESS";

        /**
         * 思考事件：reasoning 增量 token
         */
        public static final String THINKING = "THINKING";

        /**
         * 回复事件：content 增量 token
         */
        public static final String REPLY = "REPLY";

        /**
         * 终态事件：最终持久化记录
         */
        public static final String FINAL = "FINAL";

        /**
         * 错误事件：可展示的失败原因
         */
        public static final String ERROR = "ERROR";

        private Types() {
        }
    }
}