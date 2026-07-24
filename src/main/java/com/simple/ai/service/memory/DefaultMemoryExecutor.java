package com.simple.ai.service.memory;

import com.simple.ai.common.dto.command.AtomicCommandInvokeRequest;
import com.simple.ai.common.dto.command.AtomicCommandInvokeResponse;
import com.simple.ai.common.dto.command.CommandDispatchProgressEvent;
import com.simple.ai.common.dto.command.CommandDispatchRequest;
import com.simple.ai.common.entity.agentMemory.AgentMemory;
import com.simple.ai.common.entity.agentMemoryStep.AgentMemoryStep;
import com.simple.ai.common.entity.task.Task;
import com.simple.ai.common.entity.taskDetail.TaskDetail;
import com.simple.ai.common.enums.AgentExecutionStatusProcess;
import com.simple.ai.common.service.command.AtomicCommandExecutor;
import com.simple.ai.common.service.memory.MemoryExecutor;
import com.simple.ai.common.view.agentMemory.AgentMemoryView;
import com.simple.ai.common.view.agentMemoryStep.AgentMemoryStepView;
import com.simple.ai.common.view.taskDetail.TaskDetailView;
import com.simple.common.core.utils.IdUtils;
import com.simple.common.core.utils.JsonUtils;
import com.simple.common.mp.common.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 记忆执行器默认实现。
 * <p>按记忆步骤直接创建任务详情并下发原子命令到客户端执行，
 * 无需 AI 探索。步骤中的 {param} 占位符由用户输入参数替换。</p>
 *
 * @author qty
 */
@Slf4j
@Service
class DefaultMemoryExecutor implements MemoryExecutor {

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

    /**
     * 任务详情视图
     */
    @Autowired
    private TaskDetailView taskDetailView;

    /**
     * 原子命令执行器列表
     */
    @Autowired
    private List<AtomicCommandExecutor> atomicCommandExecutors;

    @Override
    public String execute(Task task, CommandDispatchRequest request, String memoryId, Consumer<CommandDispatchProgressEvent> progressConsumer) {

        // 加载记忆及其步骤
        AgentMemory memory = agentMemoryView.findById(memoryId);
        if (memory == null) {
            log.error("记忆执行失败：记忆不存在，memoryId={}", memoryId);
            return "记忆不存在";
        }

        List<AgentMemoryStep> steps = agentMemoryStepView.findAllByMemoryId(memoryId);
        if (steps.isEmpty()) {
            log.error("记忆执行失败：记忆无步骤，memoryId={}", memoryId);
            return "记忆无步骤";
        }

        // 替换参数占位符
        Map<String, Object> params = request.getRequestParams();
        StringBuilder resultBuilder = new StringBuilder();

        // 按序号依次执行每个步骤
        for (AgentMemoryStep step : steps) {
            String resolvedArgs = resolveArgsTemplate(step.getArgsTemplate(), params);

            // 创建任务详情记录
            TaskDetail taskDetail = createTaskDetail(task, step, resolvedArgs);
            taskDetailView.save(taskDetail);

            // 构造原子命令调用请求
            AtomicCommandInvokeRequest invokeRequest = buildInvokeRequest(task, step, resolvedArgs, request.getClientId());

            // 执行原子命令
            AtomicCommandInvokeResponse invokeResponse = invokeAtomicCommand(invokeRequest);

            // 更新任务详情执行结果
            updateTaskDetailResult(taskDetail, invokeResponse);

            // 拼接执行结果
            if (resultBuilder.length() > 0) {
                resultBuilder.append("\n");
            }
            resultBuilder.append(step.getStepName()).append(": ").append(invokeResponse.getSuccess() ? "成功" : "失败");

            // 步骤执行失败时中断后续步骤
            if (!Boolean.TRUE.equals(invokeResponse.getSuccess())) {
                log.warn("记忆步骤执行失败：memoryId={}, stepNo={}, reason={}", memoryId, step.getStepNo(), invokeResponse.getFailureReason());
                break;
            }
        }

        return resultBuilder.toString();
    }

    /**
     * 替换参数模板中的占位符。
     *
     * @param argsTemplate 参数模板
     * @param params       用户输入参数
     * @return 替换后的参数字符串
     */
    private String resolveArgsTemplate(String argsTemplate, Map<String, Object> params) {

        // 模板为空时直接返回
        if (argsTemplate == null || argsTemplate.isBlank()) {
            return "";
        }

        // 参数为空时不替换
        if (params == null || params.isEmpty()) {
            return argsTemplate;
        }

        String result = argsTemplate;

        // 遍历参数替换 {key} 占位符
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }

        return result;
    }

    /**
     * 创建任务详情记录。
     *
     * @param task         任务主记录
     * @param step         记忆步骤
     * @param resolvedArgs 替换后的参数
     * @return 任务详情
     */
    private TaskDetail createTaskDetail(Task task, AgentMemoryStep step, String resolvedArgs) {
        TaskDetail taskDetail = new TaskDetail();
        taskDetail.setTaskId(task.getId());
        taskDetail.setTaskName(step.getStepName());
        taskDetail.setParentTaskId(step.getParentStepId() != null ? step.getParentStepId() : "");
        taskDetail.setNextTaskId(step.getNextStepId() != null ? step.getNextStepId() : "");
        taskDetail.setStepType(step.getStepType());
        taskDetail.setBranchCondition(step.getBranchCondition() != null ? step.getBranchCondition() : "");
        taskDetail.setBranchRoute(step.getBranchRoute() != null ? step.getBranchRoute() : "");
        taskDetail.setRequestParams(resolvedArgs);
        taskDetail.setReturnParams("");
        taskDetail.setExecStatus(AgentExecutionStatusProcess.RUNNING);
        taskDetail.setStatus(Status.ON);
        taskDetail.setReserve("");
        taskDetail.setRemark("记忆步骤执行详情");
        return taskDetail;
    }

    /**
     * 构造原子命令调用请求。
     *
     * @param task         任务主记录
     * @param step         记忆步骤
     * @param resolvedArgs 替换后的参数
     * @param clientId     客户端ID
     * @return 原子命令调用请求
     */
    private AtomicCommandInvokeRequest buildInvokeRequest(Task task, AgentMemoryStep step, String resolvedArgs, String clientId) {
        AtomicCommandInvokeRequest invokeRequest = new AtomicCommandInvokeRequest();
        invokeRequest.setTaskId(task.getId());
        invokeRequest.setAtomicCommandId(step.getAtomicCommandId());
        invokeRequest.setCommandId(IdUtils.getFastSimpleUUID());
        invokeRequest.setClientId(clientId);
        invokeRequest.setCommandContent(step.getStepName());
        return invokeRequest;
    }

    /**
     * 执行原子命令。
     *
     * @param request 原子命令调用请求
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse invokeAtomicCommand(AtomicCommandInvokeRequest request) {

        // 遍历执行器找到支持当前请求的执行器
        for (AtomicCommandExecutor executor : atomicCommandExecutors) {
            if (executor.supports(request)) {
                return executor.execute(request);
            }
        }

        // 无支持执行器时返回失败
        AtomicCommandInvokeResponse response = new AtomicCommandInvokeResponse();
        response.setSuccess(Boolean.FALSE);
        response.setResponseContent("");
        response.setFailureReason("无支持的原子命令执行器");
        return response;
    }

    /**
     * 更新任务详情执行结果。
     *
     * @param taskDetail     任务详情
     * @param invokeResponse 原子命令调用响应
     */
    private void updateTaskDetailResult(TaskDetail taskDetail, AtomicCommandInvokeResponse invokeResponse) {
        taskDetail.setReturnParams(JsonUtils.toJsonStr(invokeResponse));
        taskDetail.setExecStatus(Boolean.TRUE.equals(invokeResponse.getSuccess()) ? AgentExecutionStatusProcess.SUCCESS : AgentExecutionStatusProcess.FAILED);
        taskDetailView.updateById(taskDetail);
    }
}