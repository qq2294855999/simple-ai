package com.simple.ai.controller.command;

import com.simple.ai.common.dto.command.CommandDispatchProgressEvent;
import com.simple.ai.common.dto.command.CommandDispatchRequest;
import com.simple.ai.common.dto.command.CommandDispatchResponse;
import com.simple.ai.common.service.command.CommandDispatchService;
import com.simple.common.auth.client.common.annotation.HasAuthority;
import com.simple.common.core.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

/**
 * 智能体命令调度控制层。
 *
 * @author qty
 */
@Tag(name = "智能体命令调度")
@RequestMapping("sys/agent-command")
@RestController
public class CommandDispatchController {

    /**
     * 智能体命令调度服务
     */
    @Autowired
    private CommandDispatchService commandDispatchService;

    /**
     * 任务执行器
     */
    @Autowired
    private TaskExecutor taskExecutor;

    /**
     * 调度智能体命令。
     *
     * @param request 命令调度请求
     * @return 命令调度响应
     */
    @PostMapping("dispatch")
    @Operation(summary = "调度智能体命令")
    @HasAuthority("sys:agent-command:dispatch")
    public R<CommandDispatchResponse> dispatch(@RequestBody @Valid CommandDispatchRequest request) {

        // 委托核心服务执行命令调度
        CommandDispatchResponse response = commandDispatchService.dispatch(request);
        return R.ok(response);
    }

    /**
     * 流式调度智能体命令。
     *
     * @param request 命令调度请求
     * @return SSE 进度事件输出器
     */
    @PostMapping("dispatch-stream")
    @Operation(summary = "流式调度智能体命令")
    @HasAuthority("sys:agent-command:dispatch-stream")
    public SseEmitter dispatchStream(@RequestBody @Valid CommandDispatchRequest request) {
        SseEmitter emitter = new SseEmitter(0L);

        // 使用 Spring 任务执行器处理长任务，避免请求线程被调度过程占用
        taskExecutor.execute(() -> runDispatchStream(request, emitter));
        return emitter;
    }

    /**
     * 执行流式调度并写出 SSE 事件。
     *
     * @param request 命令调度请求
     * @param emitter SSE 输出器
     */
    private void runDispatchStream(CommandDispatchRequest request, SseEmitter emitter) {
        try {
            commandDispatchService.dispatchStream(request, event -> sendProgressEvent(emitter, event));
            emitter.complete();
        } catch (Exception e) {
            emitter.completeWithError(e);
        }
    }

    /**
     * 发送流式进度事件。
     *
     * @param emitter SSE 输出器
     * @param event 进度事件
     */
    private void sendProgressEvent(SseEmitter emitter, CommandDispatchProgressEvent event) {
        try {
            emitter.send(SseEmitter.event()
                    .name(event.getEventType())
                    .data(event));
        } catch (IOException e) {
            throw new IllegalStateException("发送智能体命令调度进度事件失败", e);
        }
    }
}
