package com.simple.ai.common.dto.command;

import lombok.Data;

/**
 * 业务执行客户端回传的原子命令执行结果。
 *
 * @author qty
 */
@Data
public class AgentExecutorResponse {

    /**
     * 任务ID，用于关联等待中的调度流程
     */
    private String taskId;

    /**
     * 是否执行成功
     */
    private Boolean success;

    /**
     * 执行响应内容
     */
    private String responseContent;

    /**
     * 失败原因
     */
    private String failureReason;
}
