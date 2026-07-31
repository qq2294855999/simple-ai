package com.simple.ai.service.memory;

import com.simple.ai.common.dto.agent.AgentAiRequest;
import com.simple.ai.common.dto.agentChat.ChatSseEvent;
import com.simple.ai.common.dto.taskDetail.FindAllTaskDetailRequest;
import com.simple.ai.common.entity.agentMemory.AgentMemory;
import com.simple.ai.common.entity.agentMemoryStep.AgentMemoryStep;
import com.simple.ai.common.entity.executionEvent.ExecutionEvent;
import com.simple.ai.common.entity.task.Task;
import com.simple.ai.common.entity.taskDetail.TaskDetail;
import com.simple.ai.common.enums.AgentExecutionStatusProcess;
import com.simple.ai.common.service.agent.AgentAiClient;
import com.simple.ai.common.service.memory.MemoryReExploreService;
import com.simple.ai.common.view.agentMemory.AgentMemoryView;
import com.simple.ai.common.view.agentMemoryStep.AgentMemoryStepView;
import com.simple.ai.common.view.executionEvent.ExecutionEventView;
import com.simple.ai.common.view.task.TaskView;
import com.simple.ai.common.view.taskDetail.TaskDetailView;
import com.simple.ai.service.agentChat.AgentChatRuntimeContext;
import com.simple.ai.service.agentChat.ChatEventSender;
import com.simple.common.auth.client.util.LoginUserUtils;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.core.utils.IdUtils;
import com.simple.common.mp.common.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 记忆重新探索服务默认实现。
 * <p>记忆执行失败后，由 Web 端触发 AI 重新探索更优的命令序列。
 * 探索结果通过 AI reviseMemory 工具覆盖原记忆，执行事件落库供审计。</p>
 * <p>关键约束：
 * <ul>
 *   <li>不创建 Task/TaskDetail，仅落 execution_event（turnId 新生成，taskId 复用失败任务ID）</li>
 *   <li>AI 通过 MemoryToolCallback 的 reviseMemory 工具完成记忆覆盖</li>
 *   <li>进度通过 ChatEventSender 流式透传 SSE</li>
 * </ul></p>
 *
 * @author qty
 */
@Slf4j
@Service
class DefaultMemoryReExploreService implements MemoryReExploreService {

    /**
     * 任务视图
     */
    @Autowired
    private TaskView taskView;

    /**
     * 任务详情视图
     */
    @Autowired
    private TaskDetailView taskDetailView;

    /**
     * 智能体记忆视图
     */
    @Autowired
    private AgentMemoryView agentMemoryView;

    /**
     * 智能体记忆步骤视图
     */
    @Autowired
    private AgentMemoryStepView agentMemoryStepView;

    /**
     * 智能体 AI 调用客户端
     */
    @Autowired
    private AgentAiClient agentAiClient;

    /**
     * 执行事件视图
     */
    @Autowired
    private ExecutionEventView executionEventView;

    @Override
    public void reExplore(String taskId, Consumer<ChatSseEvent> eventConsumer) {

        // 创建事件发送器，统一 SSE 事件输出
        ChatEventSender sender = new ChatEventSender(eventConsumer);

        try {
            // 加载并校验失败任务，仅允许对失败状态且关联记忆的任务重新探索
            sender.sendProgress(taskId, "开始分析失败任务");
            Task task = taskView.findById(taskId);
            AssertUtils.notEmpty(task, "任务[{}]不存在", taskId);
            AssertUtils.isTrue(AgentExecutionStatusProcess.FAILED.equals(task.getExecStatus()), "只能重新探索失败的任务，当前状态为[{}]",
                               task.getExecStatus() != null ? task.getExecStatus().getLabel() : "未知");
            String memoryId = task.getMemoryId();
            AssertUtils.notEmpty(memoryId, "失败任务未关联记忆，无法重新探索");

            // 读取全部执行轨迹详情，用于定位失败步骤与失败原因
            sender.sendProgress(taskId, "正在读取执行轨迹");
            FindAllTaskDetailRequest detailReq = new FindAllTaskDetailRequest();
            detailReq.setTaskId(taskId);
            List<TaskDetail> allDetails = taskDetailView.findAll(detailReq);

            // 筛选失败步骤，作为 AI 分析的核心上下文
            List<TaskDetail> failedDetails = allDetails.stream().filter(d -> AgentExecutionStatusProcess.FAILED.equals(d.getExecStatus())).collect(Collectors.toList());

            // 加载原记忆及现有步骤序列，供 AI 参考当前实现
            AgentMemory memory = agentMemoryView.findById(memoryId);
            AssertUtils.notEmpty(memory, "记忆[{}]不存在", memoryId);
            List<AgentMemoryStep> existingSteps = agentMemoryStepView.findAllByMemoryId(memoryId);

            // 生成新轮次主键，执行事件按此归集本次探索过程
            String turnId = IdUtils.getSnowflakeNextIdStr();

            // 轮次内事件序号计数器，保证同一轮次事件有序
            AtomicInteger sequenceCounter = new AtomicInteger(0);

            // 记录 AI 开始探索事件，标记探索阶段起点
            recordExplorationEvent(turnId, taskId, "AI_STARTED", "AI重新探索开始", sequenceCounter);

            // 构造探索 prompt，注入失败上下文引导 AI 产出更优命令序列
            sender.sendProgress(taskId, "正在启动AI重新探索");
            String promptContent = buildExplorationPrompt(task, memory, existingSteps, allDetails, failedDetails);

            // 构造 AI 调用请求，复用失败任务所属智能体和模型
            AgentAiRequest aiRequest = new AgentAiRequest();
            aiRequest.setAgentId(task.getAgentId());
            aiRequest.setModelId(task.getModelId());
            aiRequest.setPromptContent(promptContent);
            aiRequest.setCommandContent("重新探索记忆");
            aiRequest.setSessionId("");

            // 构造运行时上下文，turnId 新生成、taskId 复用失败任务、clientId 取自记忆绑定
            String userId = LoginUserUtils.getUserTemporary().getUserId();
            AgentChatRuntimeContext runtimeContext = new AgentChatRuntimeContext("", turnId, userId, task.getAgentId(), task.getModelId(), memory.getClientId(), taskId, null, sender);

            // 调用 AI 流式探索，token 实时透传 SSE，由 AI 自主调用 reviseMemory 工具覆盖记忆
            agentAiClient.chatStream(aiRequest, runtimeContext, token -> sender.sendReply(taskId, token), thinkingToken -> sender.sendThinking(taskId, thinkingToken));

            // 记录 AI 完成与轮次结束事件，闭合本次探索审计轨迹
            recordExplorationEvent(turnId, taskId, "AI_COMPLETED", "AI重新探索完成", sequenceCounter);
            recordExplorationEvent(turnId, taskId, "TURN_COMPLETED", "重新探索轮次结束", sequenceCounter);

            // 推送终态事件，通知前端本次探索流结束
            sender.sendFinal(taskId, "", turnId, "");

            log.info("记忆重新探索完成：taskId={}, memoryId={}, turnId={}", taskId, memoryId, turnId);
        } catch (RuntimeException e) {

            // 异常时推送错误事件并记录日志，避免前端无限等待
            log.error("重新探索失败：taskId={}", taskId, e);
            sender.sendError(taskId, "重新探索失败：" + e.getMessage());
        }
    }

    /**
     * 构造探索 prompt。
     * <p>包含记忆名称、现有步骤序列、失败步骤及原因、原始执行意图，
     * 引导 AI 分析失败原因并产出更优的命令序列。</p>
     *
     * @param task          失败任务
     * @param memory        关联记忆
     * @param existingSteps 现有步骤序列
     * @param allDetails    所有步骤详情
     * @param failedDetails 失败步骤详情
     * @return 探索 prompt 文本
     */
    private String buildExplorationPrompt(Task task, AgentMemory memory, List<AgentMemoryStep> existingSteps, List<TaskDetail> allDetails, List<TaskDetail> failedDetails) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个智能体记忆优化助手。以下记忆在执行时发生了失败，请重新探索更好的命令序列。\n\n");

        // 记忆名称
        prompt.append("【记忆名称】\n");
        prompt.append(memory.getMemoryName() != null && !memory.getMemoryName().isBlank() ? memory.getMemoryName() : "未命名");
        prompt.append("\n\n");

        // 现有完整步骤序列
        prompt.append("【现有步骤序列】（共 ").append(existingSteps.size()).append(" 步）\n");
        for (AgentMemoryStep step : existingSteps) {
            prompt.append("- 步骤").append(step.getSequenceNo()).append(": ").append(step.getStepName());
            prompt.append(" (命令: ").append(step.getAtomicCommandCode()).append(")");
            String argsTemplate = step.getArgsTemplate();
            if (argsTemplate != null && !argsTemplate.isBlank()) {
                prompt.append(" 参数: ").append(truncateArgsTemplate(argsTemplate));
            }
            prompt.append("\n");
        }
        prompt.append("\n");

        // 失败步骤及原因
        prompt.append("【失败步骤及原因】\n");
        if (failedDetails.isEmpty()) {
            prompt.append("未找到明确的失败步骤，所有步骤状态: \n");
            for (TaskDetail td : allDetails) {
                prompt.append("- ").append(td.getTaskName() != null ? td.getTaskName() : "步骤");
                prompt.append(": ").append(td.getExecStatus() != null ? td.getExecStatus().getLabel() : "未知");
                String returnParams = td.getReturnParams();
                if (returnParams != null && !returnParams.isBlank()) {
                    prompt.append(" (结果: ").append(truncateText(returnParams, 200)).append(")");
                }
                prompt.append("\n");
            }
        } else {
            for (TaskDetail fd : failedDetails) {
                prompt.append("- ").append(fd.getTaskName() != null ? fd.getTaskName() : "步骤");
                prompt.append(": 执行失败");

                // 返回参数中通常包含失败原因的详细描述
                String returnParams = fd.getReturnParams();
                if (returnParams != null && !returnParams.isBlank()) {
                    prompt.append(", 失败原因: ").append(truncateText(returnParams, 300));
                }
                String requestParams = fd.getRequestParams();
                if (requestParams != null && !requestParams.isBlank()) {
                    prompt.append(", 请求参数: ").append(truncateText(requestParams, 200));
                }
                prompt.append("\n");
            }
        }
        prompt.append("\n");

        // 原始执行意图
        prompt.append("【原始执行意图】\n");
        prompt.append("任务名称: ").append(task.getTaskName() != null ? task.getTaskName() : "未知");
        prompt.append("\n");
        String requestParams = task.getRequestParams();
        if (requestParams != null && !requestParams.isBlank()) {
            prompt.append("请求参数: ").append(truncateText(requestParams, 300));
        }
        prompt.append("\n\n");

        // 指令
        prompt.append("【任务】\n");
        prompt.append("请分析以上失败原因，重新设计更合理的命令序列。");
        prompt.append("你可以使用 queryMemory 工具查看记忆详情，");
        prompt.append("使用 getMemorySteps 工具查看当前步骤，");
        prompt.append("然后调用 reviseMemory(memoryId=\"").append(memory.getId()).append("\", steps=新步骤列表, paramsDefinition=参数定义JSON, memoryNameHint=记忆名称) 工具更新记忆。\n");
        prompt.append("确保新步骤覆盖了失败原因中暴露的问题，参数模板使用 {paramName} 占位符格式。");

        return prompt.toString();
    }

    /**
     * 记录一条执行事件。
     *
     * @param turnId          轮次主键
     * @param taskId          任务主键
     * @param eventType       事件类型
     * @param stepName        步骤名称
     * @param sequenceCounter 序号计数器
     */
    private void recordExplorationEvent(String turnId, String taskId, String eventType, String stepName, AtomicInteger sequenceCounter) {
        try {
            ExecutionEvent event = new ExecutionEvent();
            event.setTurnId(turnId);
            event.setTaskId(taskId);
            event.setTaskDetailId("");

            // 事件类型
            event.setEventType(eventType);
            event.setStepName(stepName);

            // 命令相关信息
            event.setCommandName("");
            event.setCommandContent("");
            event.setResponseContent("");
            event.setFailureReason("");

            // 序号递增
            event.setSequenceNo(sequenceCounter.incrementAndGet());
            event.setStartedAt(new Date());
            event.setFinishedAt(new Date());
            event.setStatus(Status.ON);
            executionEventView.save(event);
        } catch (RuntimeException e) {

            // 执行事件落库失败不影响主流程
            log.warn("执行事件记录失败，turnId={}, eventType={}", turnId, eventType, e);
        }
    }

    /**
     * 截断参数模板文本，避免 prompt 过长。
     * <p>参数模板通常是 JSON，截断到100字符展示关键信息。</p>
     *
     * @param text 原始文本
     * @return 截断后的文本
     */
    private String truncateArgsTemplate(String text) {
        return truncateText(text, 100);
    }

    /**
     * 截断文本到指定最大长度。
     *
     * @param text      原始文本
     * @param maxLength 最大长度
     * @return 截断后的文本，超长时追加省略号
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
        return text.substring(0, maxLength) + "...";
    }
}
