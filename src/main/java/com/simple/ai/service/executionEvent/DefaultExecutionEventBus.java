package com.simple.ai.service.executionEvent;

import com.simple.ai.common.dto.command.CommandDispatchProgressEvent;
import com.simple.ai.common.entity.executionEvent.ExecutionEvent;
import com.simple.ai.common.service.executionEvent.ExecutionEventBus;
import com.simple.ai.common.view.executionEvent.ExecutionEventView;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.mp.common.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 执行事件总线默认实现。
 * <p>将调度进度事件转换为 ExecutionEvent 并持久化。使用 REQUIRES_NEW 事务传播确保事件记录
 * 独立于业务事务提交，避免业务回滚时丢失审计轨迹。</p>
 *
 * @author qty
 */
@Slf4j
@Service
class DefaultExecutionEventBus implements ExecutionEventBus {

    /**
     * 进入 ExecutionEvent 的事件类型白名单。
     * <p>AI_TOKEN 和 MESSAGE_* 等聊天层事件不入执行轨迹。</p>
     */
    private static final Set<String> TRACKED_EVENT_TYPES = new HashSet<>();

    static {
        // 上下文装配阶段
        TRACKED_EVENT_TYPES.add("CONTEXT_ASSEMBLING");
        TRACKED_EVENT_TYPES.add("CONTEXT_ASSEMBLED");
        TRACKED_EVENT_TYPES.add("RULE_LOADED");
        TRACKED_EVENT_TYPES.add("SKILL_LOADED");
        TRACKED_EVENT_TYPES.add("SUB_AGENT_LOADED");

        // 记忆匹配阶段
        TRACKED_EVENT_TYPES.add("MEMORY_MATCHING");
        TRACKED_EVENT_TYPES.add("MEMORY_MATCHED");
        TRACKED_EVENT_TYPES.add("MEMORY_MISSED");

        // 原子命令执行阶段
        TRACKED_EVENT_TYPES.add("STEP_STARTED");
        TRACKED_EVENT_TYPES.add("STEP_COMPLETED");

        // AI 调用阶段
        TRACKED_EVENT_TYPES.add("AI_STARTED");
        TRACKED_EVENT_TYPES.add("AI_COMPLETED");

        // 子智能体阶段
        TRACKED_EVENT_TYPES.add("SUB_AGENT_STARTED");
        TRACKED_EVENT_TYPES.add("SUB_AGENT_COMPLETED");

        // 任务完成/失败阶段
        TRACKED_EVENT_TYPES.add("TASK_COMPLETED");
        TRACKED_EVENT_TYPES.add("TASK_FAILED");
    }

    /**
     * 执行事件视图
     */
    @Autowired
    private ExecutionEventView executionEventView;

    /**
     * 轮次内事件序号生成器，按轮次主键隔离，每个轮次独立从 1 递增
     */
    private final Map<String, AtomicInteger> turnSequenceMap = new ConcurrentHashMap<>();

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordEvent(String turnId, String taskId, CommandDispatchProgressEvent event) {
        AssertUtils.notEmpty(turnId, "轮次主键不能为空");

        // 非轨迹事件不入 ExecutionEvent，仅聊天层消费
        if (!isTrackedEvent(event)) {
            return;
        }

        // 构建事件实体并持久化
        ExecutionEvent executionEvent = buildExecutionEvent(turnId, taskId, event);
        executionEventView.save(executionEvent);

        // 调试日志记录事件落库
        log.debug("执行事件已记录：turnId={}, eventType={}, sequenceNo={}", turnId, event.getEventType(), executionEvent.getSequenceNo());
    }

    /**
     * 判断当前事件是否应进入执行轨迹。
     *
     * @param event 调度进度事件
     * @return 是否进入轨迹
     */
    private boolean isTrackedEvent(CommandDispatchProgressEvent event) {

        // 事件或事件类型为空时不记录
        if (event == null || event.getEventType() == null || event.getEventType().isBlank()) {
            return false;
        }

        // 仅白名单内的事件类型进入执行轨迹
        return TRACKED_EVENT_TYPES.contains(event.getEventType());
    }

    /**
     * 构建执行事件实体。
     * <p>将调度进度事件映射为 ExecutionEvent，事件类型直接使用原始 eventType。</p>
     *
     * @param turnId 对话轮次主键
     * @param taskId 调度任务主键
     * @param event  调度进度事件
     * @return 执行事件实体
     */
    private ExecutionEvent buildExecutionEvent(String turnId, String taskId, CommandDispatchProgressEvent event) {
        ExecutionEvent executionEvent = new ExecutionEvent();
        executionEvent.setTurnId(turnId);
        executionEvent.setTaskId(resolveTaskId(taskId, event));
        executionEvent.setTaskDetailId(event.getStepId());
        executionEvent.setEventType(mapEventType(event.getEventType()));
        executionEvent.setStepName(event.getStepName());
        executionEvent.setCommandName(resolveCommandName(event));
        executionEvent.setCommandContent(resolveCommandContent(event));
        executionEvent.setResponseContent(truncateResponseContent(event));
        executionEvent.setFailureReason(event.getFailureReason());
        executionEvent.setSequenceNo(nextSequenceNo(turnId));
        executionEvent.setStartedAt(new Date());
        executionEvent.setFinishedAt(event.getCompleted() != null && event.getCompleted() ? new Date() : null);
        executionEvent.setProviderName(event.getProviderName());
        executionEvent.setModelCode(event.getModelCode());
        executionEvent.setStatus(Status.ON);
        return executionEvent;
    }

    /**
     * 获取指定轮次的下一个事件序号，按轮次主键隔离递增。
     *
     * @param turnId 轮次主键
     * @return 递增序号
     */
    private int nextSequenceNo(String turnId) {
        AtomicInteger counter = turnSequenceMap.computeIfAbsent(turnId, k -> new AtomicInteger(0));
        return counter.incrementAndGet();
    }

    /**
     * 解析任务主键，优先使用传入的 taskId，其次使用事件中的 taskId。
     *
     * @param taskId 外部传入的任务主键
     * @param event  调度进度事件
     * @return 任务主键
     */
    private String resolveTaskId(String taskId, CommandDispatchProgressEvent event) {

        // 事件中携带的任务主键优先
        if (event.getTaskId() != null && !event.getTaskId().isBlank()) {
            return event.getTaskId();
        }
        return taskId == null ? "" : taskId;
    }

    /**
     * 映射事件类型。STEP_STARTED 映射为 ATOMIC_COMMAND_START，
     * STEP_COMPLETED 映射为 ATOMIC_COMMAND_COMPLETE，其余保持原样。
     *
     * @param originalType 原始事件类型
     * @return 映射后的事件类型
     */
    private String mapEventType(String originalType) {

        // 原子命令步骤事件映射为细粒度执行事件类型
        if ("STEP_STARTED".equals(originalType)) {
            return "ATOMIC_COMMAND_START";
        }
        if ("STEP_COMPLETED".equals(originalType)) {
            return "ATOMIC_COMMAND_COMPLETE";
        }
        return originalType;
    }

    /**
     * 解析命令名称。
     * <p>STEP_STARTED/STEP_COMPLETED 事件的 stepName 包含命令名称。</p>
     *
     * @param event 调度进度事件
     * @return 命令名称
     */
    private String resolveCommandName(CommandDispatchProgressEvent event) {
        String eventType = event.getEventType();

        // 步骤事件取 stepName 作为命令名称
        if ("STEP_STARTED".equals(eventType) || "STEP_COMPLETED".equals(eventType)) {
            return event.getStepName();
        }
        return "";
    }

    /**
     * 解析命令内容。
     * <p>STEP_STARTED 事件的 payload 包含命令内容，其他事件不记录命令内容。</p>
     *
     * @param event 调度进度事件
     * @return 命令内容
     */
    private String resolveCommandContent(CommandDispatchProgressEvent event) {
        String eventType = event.getEventType();

        // 仅步骤开始事件记录命令内容
        if ("STEP_STARTED".equals(eventType)) {
            return truncateText(event.getPayload(), 500);
        }
        return "";
    }

    /**
     * 截断响应内容。
     * <p>STEP_COMPLETED 和 AI_COMPLETED 事件保留响应摘要（最大500字符），其余不记录。</p>
     *
     * @param event 调度进度事件
     * @return 截断后的响应内容
     */
    private String truncateResponseContent(CommandDispatchProgressEvent event) {
        String eventType = event.getEventType();

        // 步骤完成和 AI 完成事件记录响应摘要
        if ("STEP_COMPLETED".equals(eventType) || "AI_COMPLETED".equals(eventType)) {
            return truncateText(event.getPayload(), 500);
        }
        return "";
    }

    /**
     * 截断文本到指定最大长度。
     *
     * @param text      原始文本
     * @param maxLength 最大长度
     * @return 截断后的文本
     */
    private String truncateText(String text, int maxLength) {

        // 空文本直接返回
        if (text == null || text.isBlank()) {
            return "";
        }

        // 长度未超限时原样返回
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }
}
