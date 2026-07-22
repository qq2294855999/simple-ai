package com.simple.ai.common.dto.agentChat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 智能体聊天执行事件 DTO，用于前端内嵌折叠轨迹展示。
 * <p>从 ExecutionEvent 实体中提取前端所需的关键字段，精简传输。</p>
 *
 * @author qty
 */
@Data
@Schema(title = "智能体聊天执行事件")
public class AgentChatExecutionEventDto {

    /**
     * 事件主键
     */
    private String id;

    /**
     * 事件类型: ATOMIC_COMMAND_START / ATOMIC_COMMAND_COMPLETE / AI_STARTED / AI_COMPLETED 等
     */
    private String eventType;

    /**
     * 步骤名称（展示用）
     */
    private String stepName;

    /**
     * 原子命令名称
     */
    private String commandName;

    /**
     * 响应内容摘要（截断版本）
     */
    private String responseContent;

    /**
     * 失败原因
     */
    private String failureReason;

    /**
     * 轮次内事件序号
     */
    private Integer sequenceNo;

    /**
     * 开始时间
     */
    private Date startedAt;

    /**
     * 结束时间
     */
    private Date finishedAt;

    /**
     * 运行供应商名称快照
     */
    private String providerName;

    /**
     * 运行模型编码快照
     */
    private String modelCode;
}
