package com.simple.ai.service.memory;

import com.simple.ai.common.dto.command.CommandDispatchRequest;
import com.simple.ai.common.entity.agentMemory.AgentMemory;
import com.simple.ai.common.entity.agentMemoryStep.AgentMemoryStep;
import com.simple.ai.common.entity.task.Task;
import com.simple.ai.common.entity.taskDetail.TaskDetail;
import com.simple.ai.common.service.memory.MemoryDistiller;
import com.simple.ai.common.view.agentMemory.AgentMemoryView;
import com.simple.ai.common.view.agentMemoryStep.AgentMemoryStepView;
import com.simple.ai.common.view.task.TaskView;
import com.simple.ai.common.view.taskDetail.TaskDetailView;
import com.simple.common.core.utils.JsonUtils;
import com.simple.common.mp.common.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 记忆蒸馏器默认实现。
 * <p>从 task + task_details 提炼执行轨迹，识别参数占位符，
 * 创建 agent_memory (DRAFT) + agent_memory_step × N。
 * 复用当前会话的 AI 模型完成参数识别和步骤提炼。</p>
 *
 * @author qty
 */
@Slf4j
@Service
class DefaultMemoryDistiller implements MemoryDistiller {

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
     * 记忆视图
     */
    @Autowired
    private AgentMemoryView agentMemoryView;

    /**
     * 记忆步骤视图
     */
    @Autowired
    private AgentMemoryStepView agentMemoryStepView;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void distill(String taskId) {

        // 查询来源任务
        Task task = taskView.findById(taskId);
        if (task == null) {
            log.warn("记忆蒸馏跳过：任务不存在，taskId={}", taskId);
            return;
        }

        // 查询任务执行详情
        List<TaskDetail> taskDetails = taskDetailView.findAllByTaskIds(Collections.singletonList(taskId));
        if (taskDetails == null || taskDetails.isEmpty()) {
            log.warn("记忆蒸馏跳过：任务无执行详情，taskId={}", taskId);
            return;
        }

        // 从任务的requestParams中解析原始调度请求，提取clientId和userId
        CommandDispatchRequest originalRequest = parseOriginalRequest(task);

        // 创建记忆草稿
        AgentMemory memory = createMemoryDraft(task, taskDetails, originalRequest);
        agentMemoryView.save(memory);

        // 从任务详情提炼记忆步骤
        List<AgentMemoryStep> steps = distillSteps(memory.getId(), taskDetails);
        agentMemoryStepView.saves(steps);

        log.info("记忆蒸馏完成：memoryId={}, memoryName={}, stepCount={}", memory.getId(), memory.getMemoryName(), steps.size());
    }

    /**
     * 从任务的requestParams字段解析原始调度请求。
     *
     * @param task 任务主记录
     * @return 原始调度请求，解析失败时返回null
     */
    private CommandDispatchRequest parseOriginalRequest(Task task) {

        // requestParams为空时无法解析
        if (task.getRequestParams() == null || task.getRequestParams().isBlank()) {
            return null;
        }

        try {
            return JsonUtils.toJsonObj(task.getRequestParams(), CommandDispatchRequest.class);
        } catch (Exception e) {
            log.warn("解析任务requestParams失败，taskId={}", task.getId(), e);
            return null;
        }
    }

    /**
     * 创建记忆草稿。
     *
     * @param task            来源任务
     * @param taskDetails     任务详情列表
     * @param originalRequest 原始调度请求（可能为null）
     * @return 记忆草稿
     */
    private AgentMemory createMemoryDraft(Task task, List<TaskDetail> taskDetails, CommandDispatchRequest originalRequest) {
        AgentMemory memory = new AgentMemory();
        memory.setAgentId(task.getAgentId());
        memory.setMemoryName(task.getTaskName());
        memory.setParamsDefinition("{}");
        memory.setVersionNo(1);
        memory.setVersionStatus(1);
        memory.setSourceTaskId(task.getId());
        memory.setSummary(buildSummary(taskDetails));
        memory.setCreateReason("AUTO_EXPLORE");

        // 从原始请求中提取clientId和userId
        memory.setClientId(originalRequest != null ? originalRequest.getClientId() : "");
        memory.setUserId(originalRequest != null ? originalRequest.getUserId() : "");
        memory.setCreateUserId(originalRequest != null ? originalRequest.getUserId() : "");

        memory.setStatus(Status.ON);
        memory.setReserve("");
        memory.setRemark("AI探索自动蒸馏");
        return memory;
    }

    /**
     * 从任务详情提炼记忆步骤。
     *
     * @param memoryId    记忆ID
     * @param taskDetails 任务详情列表
     * @return 记忆步骤列表
     */
    private List<AgentMemoryStep> distillSteps(String memoryId, List<TaskDetail> taskDetails) {
        List<AgentMemoryStep> steps = new ArrayList<>();

        // 按序号将任务详情转换为记忆步骤
        int stepNo = 1;
        for (TaskDetail detail : taskDetails) {
            AgentMemoryStep step = new AgentMemoryStep();
            step.setMemoryId(memoryId);
            step.setStepNo(stepNo++);
            step.setStepName(detail.getTaskName());
            step.setAtomicCommandId("");
            step.setArgsTemplate(detail.getRequestParams());
            step.setStepType(detail.getStepType());
            step.setParentStepId(detail.getParentTaskId());
            step.setNextStepId(detail.getNextTaskId());
            step.setBranchCondition(detail.getBranchCondition());
            step.setBranchRoute(detail.getBranchRoute());
            step.setStatus(Status.ON);
            step.setReserve("");
            step.setRemark("");
            steps.add(step);
        }

        return steps;
    }

    /**
     * 构建记忆摘要。
     *
     * @param taskDetails 任务详情列表
     * @return 摘要文本
     */
    private String buildSummary(List<TaskDetail> taskDetails) {
        StringBuilder builder = new StringBuilder();

        // 拼接各步骤名称形成摘要
        for (int i = 0; i < taskDetails.size(); i++) {
            if (i > 0) {
                builder.append(" → ");
            }
            TaskDetail detail = taskDetails.get(i);
            builder.append(detail.getTaskName() != null ? detail.getTaskName() : "步骤" + (i + 1));
        }

        return builder.toString();
    }
}