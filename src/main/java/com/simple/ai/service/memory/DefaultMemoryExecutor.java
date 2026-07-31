package com.simple.ai.service.memory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simple.ai.common.dto.command.AtomicCommandInvokeRequest;
import com.simple.ai.common.dto.command.AtomicCommandInvokeResponse;
import com.simple.ai.common.dto.command.CommandDispatchProgressEvent;
import com.simple.ai.common.dto.command.CommandDispatchRequest;
import com.simple.ai.common.entity.agentMemory.AgentMemory;
import com.simple.ai.common.entity.agentMemoryStep.AgentMemoryStep;
import com.simple.ai.common.entity.task.Task;
import com.simple.ai.common.entity.taskDetail.TaskDetail;
import com.simple.ai.common.enums.AgentExecutionStatusProcess;
import com.simple.ai.common.enums.AgentStepTypeProcess;
import com.simple.ai.common.service.command.AtomicCommandExecutor;
import com.simple.ai.common.service.memory.MemoryExecutor;
import com.simple.ai.common.view.agentMemory.AgentMemoryView;
import com.simple.ai.common.view.agentMemoryStep.AgentMemoryStepView;
import com.simple.ai.common.view.task.TaskView;
import com.simple.ai.common.view.taskDetail.TaskDetailView;
import com.simple.common.core.utils.IdUtils;
import com.simple.common.core.utils.JsonUtils;
import com.simple.common.core.utils.ThreadUtils;
import com.simple.common.mp.common.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 记忆执行器默认实现。
 *
 * <p>按记忆步骤直接创建任务详情并下发原子命令到客户端执行，
 * 无需 AI 探索。步骤中的 {param} 占位符由用户输入参数替换。</p>
 *
 * <p>特性：
 * <ul>
 *   <li>过滤 OFF 状态步骤，跳过禁用的步骤</li>
 *   <li>参数校验：步骤模板中存在未替换占位符时记录警告</li>
 *   <li>RETRY 策略保留首次失败信息</li>
 *   <li>每个步骤执行前后发布进度事件</li>
 * </ul>
 * </p>
 *
 * @author qty
 */
@Slf4j
@Service
class DefaultMemoryExecutor implements MemoryExecutor {

    /**
     * 未替换占位符检测正则：匹配 {xxx} 格式
     */
    private static final Pattern UNRESOLVED_PLACEHOLDER = Pattern.compile("\\{[^}]+}");

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
     * 原子命令执行器列表
     */
    @Autowired
    private List<AtomicCommandExecutor> atomicCommandExecutors;

    @Override
    public MemoryExecutionResult execute(Task task, CommandDispatchRequest request, String memoryId, Consumer<CommandDispatchProgressEvent> progressConsumer) {

        // 加载记忆及其步骤
        AgentMemory memory = agentMemoryView.findById(memoryId);
        if (memory == null) {
            log.error("记忆执行失败：记忆不存在，memoryId={}", memoryId);
            return new MemoryExecutionResult().setSuccess(false).setDetail("记忆不存在");
        }

        // 防御性校验：仅启用状态的记忆可执行
        if (!Status.ON.equals(memory.getStatus())) {
            log.error("记忆执行失败：记忆未启用，memoryId={}, status={}", memoryId, memory.getStatus());
            return new MemoryExecutionResult().setSuccess(false).setDetail("记忆未启用，无法执行");
        }

        // 参数校验：检查必填参数和占位符完整性
        String paramValidationError = validateExecutionParams(memory, request.getRequestParams());
        if (paramValidationError != null) {
            log.error("记忆执行失败：参数校验不通过，memoryId={}, reason={}", memoryId, paramValidationError);
            return new MemoryExecutionResult().setSuccess(false).setDetail(paramValidationError);
        }

        List<AgentMemoryStep> allSteps = agentMemoryStepView.findAllByMemoryId(memoryId);
        if (allSteps.isEmpty()) {
            log.error("记忆执行失败：记忆无步骤，memoryId={}", memoryId);
            return new MemoryExecutionResult().setSuccess(false).setDetail("记忆无步骤");
        }

        // 过滤 OFF 状态步骤，只执行 ON 状态的步骤
        List<AgentMemoryStep> steps = allSteps.stream().filter(step -> !"OFF".equalsIgnoreCase(step.getStatus())).toList();
        if (steps.isEmpty()) {
            log.warn("记忆执行跳过：所有步骤均为OFF状态，memoryId={}", memoryId);
            return new MemoryExecutionResult().setSuccess(false).setDetail("所有步骤均已禁用");
        }

        // clientId 兜底：不传则使用记忆绑定的客户端
        String clientId = request.getClientId();
        if (clientId == null || clientId.isBlank()) {
            clientId = memory.getClientId();
        }

        // 替换参数占位符
        Map<String, Object> params = request.getRequestParams();
        StringBuilder resultBuilder = new StringBuilder();

        // 用布尔标志位追踪是否存在失败步骤
        boolean hasFailure = false;

        // 发布记忆执行开始进度
        publishStepProgress(progressConsumer, request, task, "MEMORY_STEP_START", "开始执行记忆步骤，共" + steps.size() + "步", memoryId, false, "");

        // 按序号依次执行每个步骤
        for (int i = 0; i < steps.size(); i++) {
            AgentMemoryStep step = steps.get(i);
            String resolvedArgs = resolveArgsTemplate(step.getArgsTemplate(), params);

            // 参数校验：检测未替换的占位符
            validateResolvedArgs(resolvedArgs, step, memoryId);

            // 发布步骤执行前进度
            publishStepProgress(progressConsumer, request, task, "MEMORY_STEP_EXECUTING", "执行步骤 " + (i + 1) + "/" + steps.size() + "：" + step.getStepName(), memoryId, false,
                                "sequenceNo=" + step.getSequenceNo());

            // 执行前随机延迟
            applyDelay(step);

            // 创建任务详情记录
            TaskDetail taskDetail = createTaskDetail(task, step, resolvedArgs);
            taskDetailView.save(taskDetail);

            // 构造原子命令调用请求
            AtomicCommandInvokeRequest invokeRequest = buildInvokeRequest(task, step, resolvedArgs, clientId);

            // 执行原子命令（含超时控制）
            AtomicCommandInvokeResponse invokeResponse = invokeAtomicCommandWithTimeout(invokeRequest, step);

            // 更新任务详情执行结果
            updateTaskDetailResult(taskDetail, invokeResponse);

            // 拼接执行结果
            if (resultBuilder.length() > 0) {
                resultBuilder.append("\n");
            }
            boolean stepSuccess = Boolean.TRUE.equals(invokeResponse.getSuccess());
            resultBuilder.append(step.getStepName()).append(": ").append(stepSuccess ? "成功" : "失败");

            // 步骤执行失败时根据策略处理
            if (!stepSuccess) {
                log.warn("记忆步骤执行失败：memoryId={}, sequenceNo={}, reason={}", memoryId, step.getSequenceNo(), invokeResponse.getFailureReason());

                // 根据失败策略决定后续行为
                String strategy = step.getFailureStrategy();
                if ("SKIP".equalsIgnoreCase(strategy)) {
                    // 跳过当前步骤，标记失败但继续执行后续步骤
                    hasFailure = true;
                    resultBuilder.append("(跳过)");
                    log.info("记忆步骤失败策略为SKIP，跳过继续执行：sequenceNo={}", step.getSequenceNo());

                    // 发布步骤跳过进度
                    publishStepProgress(progressConsumer, request, task, "MEMORY_STEP_SKIPPED", "步骤 " + step.getStepName() + " 失败后跳过", memoryId, false,
                                        invokeResponse.getFailureReason());
                    continue;
                } else if ("RETRY".equalsIgnoreCase(strategy)) {
                    // 重试当前步骤一次，保留首次失败信息
                    String firstFailureReason = invokeResponse.getFailureReason();
                    log.info("记忆步骤失败策略为RETRY，重试执行：sequenceNo={}, 首次失败原因={}", step.getSequenceNo(), firstFailureReason);

                    // 发布重试进度
                    publishStepProgress(progressConsumer, request, task, "MEMORY_STEP_RETRYING", "步骤 " + step.getStepName() + " 失败后重试", memoryId, false, firstFailureReason);

                    // 创建重试的独立任务详情记录，保留首次失败记录不覆盖
                    TaskDetail retryTaskDetail = createTaskDetail(task, step, resolvedArgs);
                    retryTaskDetail.setRemark("记忆步骤重试执行详情");
                    taskDetailView.save(retryTaskDetail);

                    AtomicCommandInvokeResponse retryResponse = invokeAtomicCommandWithTimeout(invokeRequest, step);
                    updateTaskDetailResult(retryTaskDetail, retryResponse);
                    if (Boolean.TRUE.equals(retryResponse.getSuccess())) {
                        // 重试成功：当前步骤最终成功，追加标记后继续
                        resultBuilder.append(" → 成功(重试)");

                        // 发布重试成功进度
                        publishStepProgress(progressConsumer, request, task, "MEMORY_STEP_RETRY_SUCCESS", "步骤 " + step.getStepName() + " 重试成功", memoryId, false, "");
                        continue;
                    }
                    // 重试仍失败则按STOP处理，保留首次失败信息
                    hasFailure = true;
                    resultBuilder.append("(重试仍失败，首次原因: ").append(firstFailureReason).append(")");
                    log.warn("记忆步骤重试仍失败：sequenceNo={}, 首次失败原因={}, 重试失败原因={}", step.getSequenceNo(), firstFailureReason, retryResponse.getFailureReason());
                }

                // STOP策略或RETRY失败时标记失败并中断后续步骤
                hasFailure = true;

                // 发布步骤失败进度
                publishStepProgress(progressConsumer, request, task, "MEMORY_STEP_FAILED", "步骤 " + step.getStepName() + " 执行失败，终止后续步骤", memoryId, true,
                                    invokeResponse.getFailureReason());
                break;
            }

            // 成功断言校验
            if (!assertSuccess(step, invokeResponse)) {
                hasFailure = true;
                resultBuilder.append("(断言未通过)");
                log.warn("记忆步骤成功断言未通过：memoryId={}, sequenceNo={}", memoryId, step.getSequenceNo());

                // 发布断言失败进度
                publishStepProgress(progressConsumer, request, task, "MEMORY_STEP_ASSERTION_FAILED", "步骤 " + step.getStepName() + " 断言未通过", memoryId, true,
                                    "successAssertion=" + step.getSuccessAssertion());
                break;
            }

            // 发布步骤成功进度
            publishStepProgress(progressConsumer, request, task, "MEMORY_STEP_SUCCESS", "步骤 " + step.getStepName() + " 执行成功", memoryId, false, "");
        }

        // 发布记忆执行结束进度
        String finalStatus = hasFailure ? "失败" : "成功";
        publishStepProgress(progressConsumer, request, task, "MEMORY_EXECUTION_DONE", "记忆执行" + finalStatus, memoryId, hasFailure, resultBuilder.toString());

        return new MemoryExecutionResult().setSuccess(!hasFailure).setDetail(resultBuilder.toString());
    }

    /**
     * 发布步骤执行进度事件。
     *
     * @param progressConsumer 进度事件消费者
     * @param request          命令调度请求
     * @param task             任务主记录
     * @param eventType        事件类型
     * @param message          进度消息
     * @param memoryId         记忆ID
     * @param isError          是否错误
     * @param detail           详情
     */
    private void publishStepProgress(Consumer<CommandDispatchProgressEvent> progressConsumer, CommandDispatchRequest request, Task task, String eventType, String message, String memoryId,
                                     boolean isError, String detail) {
        if (progressConsumer == null) {
            return;
        }
        try {
            CommandDispatchProgressEvent event = new CommandDispatchProgressEvent();
            event.setTaskId(task.getId());
            event.setSessionId(request.getSessionId());
            event.setEventType(eventType);
            event.setStepId(memoryId);
            event.setStepName(message);
            event.setMessage(message);
            event.setPayload(detail);
            event.setCompleted(isError);
            event.setFailureReason(isError ? detail : "");
            progressConsumer.accept(event);
        } catch (RuntimeException e) {
            log.warn("记忆步骤进度事件发送失败，任务ID：{}，事件类型：{}", task.getId(), eventType, e);
        }
    }

    /**
     * 校验替换后的参数是否仍包含未替换的占位符。
     * <p>步骤模板中的 {param} 占位符如果未被替换，
     * 说明用户输入或 AI 提取的参数中缺少对应的值。
     * 此时不中断执行（占位符可能是有意保留的），
     * 但记录警告日志供排查。</p>
     *
     * @param resolvedArgs 替换后的参数字符串
     * @param step         记忆步骤
     * @param memoryId     记忆ID
     */
    private void validateResolvedArgs(String resolvedArgs, AgentMemoryStep step, String memoryId) {
        if (resolvedArgs == null || resolvedArgs.isBlank()) {
            return;
        }

        Matcher matcher = UNRESOLVED_PLACEHOLDER.matcher(resolvedArgs);
        if (matcher.find()) {
            log.warn("记忆步骤参数存在未替换占位符：memoryId={}, sequenceNo={}, unresolvedArgs={}", memoryId, step.getSequenceNo(), resolvedArgs);
        }
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
     * 执行前随机延迟。
     *
     * <p>记忆步骤串行执行，延迟必须同步等待后才能继续下一步。
     * 此处 Thread.sleep 在记忆执行场景下是合理的同步等待。</p>
     *
     * @param step 记忆步骤
     */
    private void applyDelay(AgentMemoryStep step) {
        int delayMin = step.getDelayMinMs() != null ? step.getDelayMinMs() : 0;
        int delayMax = step.getDelayMaxMs() != null ? step.getDelayMaxMs() : 0;

        // 无延迟配置时跳过
        if (delayMin <= 0 && delayMax <= 0) {
            return;
        }

        // 确保最小值不大于最大值
        if (delayMin > delayMax) {
            int temp = delayMin;
            delayMin = delayMax;
            delayMax = temp;
        }

        // 在[min, max]范围内随机延迟
        int delayMs = delayMin == delayMax ? delayMin : ThreadLocalRandom.current().nextInt(delayMin, delayMax + 1);
        if (delayMs > 0) {
            try {
                Thread.sleep(delayMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("记忆步骤延迟被中断：sequenceNo={}", step.getSequenceNo());
            }
        }
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
        taskDetail.setParentTaskId("");
        taskDetail.setNextTaskId("");
        taskDetail.setStepType(AgentStepTypeProcess.ATOMIC_COMMAND);
        taskDetail.setBranchCondition("");
        taskDetail.setBranchRoute("");
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
     * @param resolvedArgs 替换后的参数JSON字符串
     * @param clientId     客户端ID
     * @return 原子命令调用请求
     */
    private AtomicCommandInvokeRequest buildInvokeRequest(Task task, AgentMemoryStep step, String resolvedArgs, String clientId) {
        AtomicCommandInvokeRequest invokeRequest = new AtomicCommandInvokeRequest();
        invokeRequest.setTaskId(task.getId());
        invokeRequest.setAtomicCommandId(step.getAtomicCommandId());
        invokeRequest.setCommandId(IdUtils.getFastSimpleUUID());
        invokeRequest.setClientId(clientId);
        invokeRequest.setCommandContent(step.getAtomicCommandCode() != null && !step.getAtomicCommandCode().isBlank() ? step.getAtomicCommandCode() : step.getStepName());

        // 将替换后的参数JSON解析为Map传入请求，确保原子命令收到实际参数
        if (resolvedArgs != null && !resolvedArgs.isBlank()) {
            try {
                Map<String, Object> resolvedMap = JsonUtils.toJsonObj(resolvedArgs, Map.class);
                invokeRequest.setRequestParams(resolvedMap);
            } catch (Exception e) {
                log.warn("解析替换后参数失败，使用原始模板：sequenceNo={}", step.getSequenceNo(), e);
            }
        }

        return invokeRequest;
    }

    /**
     * 执行原子命令（含超时控制）。
     *
     * <p>使用框架统一管理的线程池提交异步任务，通过 CompletableFuture 超时机制控制执行时间，
     * 避免每个步骤创建新线程导致资源浪费。</p>
     *
     * @param request 原子命令调用请求
     * @param step    记忆步骤（含超时配置）
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse invokeAtomicCommandWithTimeout(AtomicCommandInvokeRequest request, AgentMemoryStep step) {
        long timeoutMs = step.getTimeoutMs() != null ? step.getTimeoutMs() : 30000L;

        try {
            // 使用框架统一管理的高性能线程池异步执行命令
            CompletableFuture<AtomicCommandInvokeResponse> future = CompletableFuture.supplyAsync(() -> invokeAtomicCommand(request), ThreadUtils.getAsyncExecutor());
            return future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            log.warn("记忆步骤执行超时：sequenceNo={}, timeoutMs={}", step.getSequenceNo(), timeoutMs);
            AtomicCommandInvokeResponse response = new AtomicCommandInvokeResponse();
            response.setSuccess(Boolean.FALSE);
            response.setResponseContent("");
            response.setFailureReason("执行超时(" + timeoutMs + "ms)");
            return response;
        } catch (Exception e) {
            log.warn("记忆步骤执行异常：sequenceNo={}", step.getSequenceNo(), e);
            AtomicCommandInvokeResponse response = new AtomicCommandInvokeResponse();
            response.setSuccess(Boolean.FALSE);
            response.setResponseContent("");
            response.setFailureReason("执行异常: " + e.getMessage());
            return response;
        }
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
     * 成功断言校验。
     *
     * <p>如果步骤配置了 successAssertion，则检查响应内容是否匹配断言规则。
     * 当前实现为简单的包含检查：断言字符串是否出现在响应内容中。</p>
     *
     * @param step           记忆步骤
     * @param invokeResponse 原子命令调用响应
     * @return 断言是否通过
     */
    private boolean assertSuccess(AgentMemoryStep step, AtomicCommandInvokeResponse invokeResponse) {
        String assertion = step.getSuccessAssertion();

        // 无断言配置时默认通过
        if (assertion == null || assertion.isBlank()) {
            return true;
        }

        // 检查响应内容是否包含断言字符串
        String responseContent = invokeResponse.getResponseContent();
        if (responseContent == null) {
            return false;
        }

        return responseContent.contains(assertion);
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

    /**
     * 校验执行参数的完整性、类型和占位符匹配。
     *
     * <p>params_definition 格式为 JSON 对象，key 为参数名，value 为属性定义：
     * {"contact_name": {"type": "string", "description": "...", "required": true}}</p>
     *
     * <p>此校验在 MemoryExecutor 中执行，确保无论从哪个入口（Controller 或 CommandDispatchService）
     * 调用记忆执行，参数都会被完整校验。</p>
     *
     * @param memory 记忆对象
     * @param params 用户输入参数
     * @return 校验错误信息，null 表示校验通过
     */
    private String validateExecutionParams(AgentMemory memory, Map<String, Object> params) {

        // 无参数定义的记忆无需校验
        String paramsDefinition = memory.getParamsDefinition();
        if (paramsDefinition == null || paramsDefinition.isBlank()) {
            return null;
        }

        // 解析参数定义
        Map<String, Map<String, Object>> paramDefs;
        try {
            ObjectMapper mapper = new ObjectMapper();
            paramDefs = mapper.readValue(paramsDefinition, new TypeReference<Map<String, Map<String, Object>>>() {
            });
        } catch (Exception e) {
            log.warn("参数定义JSON解析失败，跳过参数校验：memoryId={}", memory.getId(), e);
            return null;
        }

        if (paramDefs == null || paramDefs.isEmpty()) {
            return null;
        }

        // 遍历参数定义，校验必填参数和类型
        for (Map.Entry<String, Map<String, Object>> entry : paramDefs.entrySet()) {
            String paramName = entry.getKey();
            Map<String, Object> attrDef = entry.getValue();
            Boolean required = attrDef != null ? (Boolean) attrDef.get("required") : null;

            // 必填参数校验
            if (Boolean.TRUE.equals(required)) {
                if (params == null || !params.containsKey(paramName)) {
                    return "必填参数[" + paramName + "]未提供";
                }
                if (params.get(paramName) == null) {
                    return "必填参数[" + paramName + "]的值不能为空";
                }
            }

            // 类型校验：当前仅支持 string 和 number 两种类型
            if (params != null && params.containsKey(paramName) && params.get(paramName) != null && attrDef != null) {
                String type = attrDef.get("type") != null ? attrDef.get("type").toString() : "string";
                Object value = params.get(paramName);

                // number 类型校验
                if ("number".equalsIgnoreCase(type)) {
                    boolean isNumeric = value instanceof Number || (value instanceof String && ((String) value).matches("-?\\d+(\\.\\d+)?"));
                    if (!isNumeric) {
                        return "参数[" + paramName + "]的类型应为number，实际值为[" + value + "]";
                    }
                }
            }
        }

        // 校验步骤模板中的占位符是否都有对应参数
        List<AgentMemoryStep> steps = agentMemoryStepView.findAllByMemoryId(memory.getId());
        for (AgentMemoryStep step : steps) {
            String template = step.getArgsTemplate();
            if (template == null || template.isBlank()) {
                continue;
            }

            // 提取模板中的占位符 {xxx}
            java.util.regex.Matcher matcher = UNRESOLVED_PLACEHOLDER.matcher(template);
            while (matcher.find()) {
                String placeholder = matcher.group(1);
                if (params == null || !params.containsKey(placeholder)) {
                    return "步骤[" + step.getStepName() + "]的模板占位符{" + placeholder + "}未提供对应参数";
                }
            }
        }

        return null;
    }
}