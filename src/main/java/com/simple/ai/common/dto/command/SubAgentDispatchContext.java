package com.simple.ai.common.dto.command;

import com.simple.ai.common.entity.subAgentRelation.SubAgentRelation;
import com.simple.ai.common.entity.task.Task;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.function.Consumer;

/**
 * 子智能体调度上下文。
 *
 * @author qty
 */
@Data
@Schema(title = "子智能体调度上下文")
public class SubAgentDispatchContext {

    /**
     * 父任务主记录
     */
    @Schema(description = "父任务主记录")
    private Task parentTask;

    /**
     * 父命令调度请求
     */
    @Schema(description = "父命令调度请求")
    private CommandDispatchRequest parentRequest;

    /**
     * 子智能体关系
     */
    @Schema(description = "子智能体关系")
    private SubAgentRelation relation;

    /**
     * 进度事件消费者
     */
    @Schema(description = "进度事件消费者")
    private Consumer<CommandDispatchProgressEvent> progressConsumer;

    /**
     * 当前递归深度
     */
    @Schema(description = "当前递归深度")
    private Integer recursionDepth;
}