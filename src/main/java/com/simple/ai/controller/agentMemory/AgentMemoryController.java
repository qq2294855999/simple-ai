package com.simple.ai.controller.agentMemory;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.agentChat.ChatSseEvent;
import com.simple.ai.common.dto.agentMemory.*;
import com.simple.ai.common.exception.ClientDisconnectedException;
import com.simple.ai.common.properties.SimpleAiProperties;
import com.simple.ai.common.service.agentMemory.AgentMemoryService;
import com.simple.ai.common.service.memory.MemoryReExploreService;
import com.simple.ai.service.agentChat.ChatEventSender;
import com.simple.common.auth.client.common.annotation.HasAuthority;
import com.simple.common.core.response.R;
import com.simple.common.core.utils.AssertUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * 智能体记忆(agent_memory)控制层
 *
 * @author qty
 */
@Slf4j
@Tag(name = "智能体记忆")
@RequestMapping("sys/agent-memory")
@RestController
public class AgentMemoryController {

    @Autowired
    private AgentMemoryService agentMemoryService;

    /**
     * 记忆重新探索服务
     */
    @Autowired
    private MemoryReExploreService memoryReExploreService;

    /**
     * 全局配置属性
     */
    @Autowired
    private SimpleAiProperties simpleAiProperties;

    /**
     * 异步任务执行器
     */
    @Autowired
    private TaskExecutor taskExecutor;

    /**
     * 分页查询智能体记忆
     *
     * @param request 分页请求
     * @return 分页数据
     */
    @GetMapping("list")
    @Operation(summary = "分页查询智能体记忆")
    @HasAuthority("sys:agent-memory:list")
    public R<IPage<PageAgentMemoryResponse>> list(@ParameterObject PageAgentMemoryRequest request) {
        return R.ok(agentMemoryService.findAll(request));
    }

    /**
     * 查询单个智能体记忆
     *
     * @param id 主键
     * @return 记忆详情
     */
    @GetMapping("find/{id}")
    @Operation(summary = "查询单个智能体记忆")
    @HasAuthority("sys:agent-memory:find")
    public R<InfoAgentMemoryResponse> findOne(@PathVariable String id) {
        AssertUtils.notEmpty(id, "主键不能为空");
        return R.ok(agentMemoryService.findById(id));
    }

    /**
     * 创建智能体记忆
     *
     * @param createRequest 创建请求
     * @return 新增记录主键
     */
    @PostMapping("create")
    @Operation(summary = "创建智能体记忆")
    @HasAuthority("sys:agent-memory:create")
    public R<String> create(@RequestBody @Validated CreateAgentMemoryRequest createRequest) {
        return R.ok(agentMemoryService.save(createRequest));
    }

    /**
     * 更新智能体记忆
     *
     * @param id            主键
     * @param updateRequest 修改请求
     * @return 修改记录主键
     */
    @PutMapping("update/{id}")
    @Operation(summary = "更新智能体记忆")
    @HasAuthority("sys:agent-memory:update")
    public R<String> update(@PathVariable String id, @RequestBody @Validated UpdateAgentMemoryRequest updateRequest) {
        AssertUtils.isTrue(updateRequest.getId().equals(id), "请求内容的ID与路径ID不同");
        return R.ok(agentMemoryService.updateById(updateRequest));
    }

    /**
     * 删除智能体记忆
     *
     * @param ids 主键列表
     * @return 空响应
     */
    @DeleteMapping("deletes")
    @Transactional
    @Operation(summary = "删除智能体记忆")
    @HasAuthority("sys:agent-memory:deletes")
    public R<Object> deleteByIds(@RequestBody List<String> ids) {
        AssertUtils.notEmpty(ids, "主键不能为空");
        agentMemoryService.deleteByIds(ids);
        return R.ok();
    }

    /**
     * 获取记忆的参数定义。
     * <p>返回参数定义JSON和步骤列表，用于前端动态生成执行表单。</p>
     *
     * @param id 记忆ID
     * @return 参数定义响应
     */
    @GetMapping("{id}/params-definition")
    @Operation(summary = "获取记忆参数定义")
    @HasAuthority("sys:agent-memory:find")
    public R<ParamsDefinitionResponse> getParamsDefinition(@PathVariable String id) {
        AssertUtils.notEmpty(id, "主键不能为空");
        return R.ok(agentMemoryService.getParamsDefinition(id));
    }

    /**
     * 执行记忆。
     * <p>根据用户传入的参数替换步骤模板中的占位符，创建任务并逐步骤执行。</p>
     *
     * @param id      记忆ID
     * @param request 执行请求（含参数和客户端ID）
     * @return 执行响应
     */
    @PostMapping("{id}/execute")
    @Operation(summary = "执行记忆")
    @HasAuthority("sys:agent-memory:execute")
    public R<ExecuteMemoryResponse> execute(@PathVariable String id, @RequestBody @Validated ExecuteMemoryRequest request) {
        AssertUtils.notEmpty(id, "主键不能为空");
        return R.ok(agentMemoryService.execute(id, request));
    }

    /**
     * 重新探索失败记忆。
     * <p>当记忆执行失败后，通过 SSE 流式透传 AI 重新探索进度和结果。
     * AI 产出新的命令序列后通过 reviseMemory 工具覆盖原记忆。</p>
     *
     * @param taskId   失败任务主键
     * @param response HTTP 响应
     * @return SSE 事件输出器
     */
    @PostMapping(value = "task/{taskId}/re-explore", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "重新探索失败记忆")
    @HasAuthority("sys:agent-memory:re-explore")
    public SseEmitter reExplore(@PathVariable String taskId, HttpServletResponse response) {
        AssertUtils.notEmpty(taskId, "任务主键不能为空");

        // 明确声明 SSE 响应并禁止代理转换或聚合响应内容，确保每个事件帧立即到达浏览器
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache, no-transform");
        response.setHeader("X-Accel-Buffering", "no");
        response.setHeader("Connection", "keep-alive");
        SseEmitter emitter = new SseEmitter(simpleAiProperties.getChat().getStreamTimeoutMillis());

        // SSE 超时必须以错误终态关闭，避免浏览器无限等待 loading
        emitter.onTimeout(() -> emitter.completeWithError(new TimeoutException("重新探索响应超时")));

        // 客户端主动断开连接时中断后台任务
        emitter.onCompletion(() -> log.debug("SSE 连接已关闭，客户端可能点击了停止"));
        emitter.onError(ex -> log.warn("SSE 连接异常: {}", ex.getMessage()));

        try {
            // 建立通道后立即写出首帧，使浏览器、Vite 与反向代理立刻提交流式响应
            sendEvent(emitter, ChatSseEvent.builder().type(ChatSseEvent.Types.PROGRESS).taskId(taskId).data("已建立实时对话通道").completed(false).build());

            // 在异步线程执行重新探索，保持请求线程可立即返回 SSE 通道
            taskExecutor.execute(() -> runReExplore(taskId, emitter));
        } catch (RuntimeException e) {

            // 执行器拒绝任务时立即关闭 SSE，前端将显示失败消息并结束 loading
            emitter.completeWithError(e);
        }
        return emitter;
    }

    /**
     * 执行重新探索并写出事件。
     *
     * @param taskId  失败任务主键
     * @param emitter SSE 输出器
     */
    private void runReExplore(String taskId, SseEmitter emitter) {
        try {
            memoryReExploreService.reExplore(taskId, event -> sendEvent(emitter, event));
            emitter.complete();
        } catch (Throwable e) {

            // 客户端断开连接属于正常行为，不需要标记为错误
            if (isClientDisconnected(e)) {
                log.debug("客户端已断开，终止重新探索流式响应");
                emitter.complete();
                return;
            }

            // 数据库或 AI 调用异常必须关闭 SSE，使客户端进入失败处理而非保持 loading
            emitter.completeWithError(e);
        }
    }

    /**
     * 判断异常是否由客户端断开连接引起。
     *
     * @param e 异常
     * @return 是否为客户端断开异常
     */
    private boolean isClientDisconnected(Throwable e) {
        if (e instanceof ClientDisconnectedException) {
            return true;
        }
        String message = e.getMessage();
        if (message != null && (message.contains("Broken pipe") || message.contains("ClientAbortException") || message.contains("Connection reset"))) {
            return true;
        }
        Throwable cause = e.getCause();
        return cause != null && isClientDisconnected(cause);
    }

    /**
     * 写出单条 SSE 事件。
     * <p>统一使用 ChatEventSender 将稳定事件写入 SSE 通道。</p>
     *
     * @param emitter SSE 输出器
     * @param event   聊天 SSE 事件
     */
    private void sendEvent(SseEmitter emitter, ChatSseEvent event) {
        ChatEventSender.writeToEmitter(emitter, event);
    }
}