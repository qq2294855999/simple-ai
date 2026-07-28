package com.simple.ai.service.memory;

import com.simple.ai.common.dto.agent.AgentAiRequest;
import com.simple.ai.common.dto.agent.AgentAiResponse;
import com.simple.ai.common.dto.agentMemory.FindAllAgentMemoryRequest;
import com.simple.ai.common.dto.atomicCommand.FindOneAtomicCommandRequest;
import com.simple.ai.common.dto.command.CommandDispatchRequest;
import com.simple.ai.common.entity.agentMemory.AgentMemory;
import com.simple.ai.common.entity.agentMemoryStep.AgentMemoryStep;
import com.simple.ai.common.entity.atomicCommand.AtomicCommand;
import com.simple.ai.common.entity.executionEvent.ExecutionEvent;
import com.simple.ai.common.entity.task.Task;
import com.simple.ai.common.entity.taskDetail.TaskDetail;
import com.simple.ai.common.enums.AgentExecutionStatusProcess;
import com.simple.ai.common.enums.AgentMemoryVersionStatusProcess;
import com.simple.ai.common.service.agent.AgentAiClient;
import com.simple.ai.common.service.memory.MemoryDistiller;
import com.simple.ai.common.view.agentMemory.AgentMemoryView;
import com.simple.ai.common.view.agentMemoryStep.AgentMemoryStepView;
import com.simple.ai.common.view.atomicCommand.AtomicCommandView;
import com.simple.ai.common.view.executionEvent.ExecutionEventView;
import com.simple.ai.common.view.task.TaskView;
import com.simple.ai.common.view.taskDetail.TaskDetailView;
import com.simple.common.core.utils.JsonUtils;
import com.simple.common.mp.common.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 记忆蒸馏器默认实现。
 *
 * <p>从 task + task_details 提炼执行轨迹，通过 AI 识别参数占位符，
 * 创建 agent_memory (DRAFT) + agent_memory_step × N。
 * 复用当前会话的 AI 模型完成参数识别和步骤提炼。</p>
 *
 * @author qty
 */
@Slf4j
@Service
class DefaultMemoryDistiller implements MemoryDistiller {

    /**
     * 自注入代理，确保 @Transactional 注解通过 Spring AOP 代理生效
     */
    @Lazy
    @Autowired
    private DefaultMemoryDistiller self;

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

    /**
     * AI 调用客户端，用于参数识别
     */
    @Autowired
    private AgentAiClient agentAiClient;

    /**
     * 执行事件视图
     */
    @Autowired
    private ExecutionEventView executionEventView;

    /**
     * 原子命令视图，用于从协议定义表兜底查找原子命令信息
     */
    @Autowired
    private AtomicCommandView atomicCommandView;

    @Override
    public void distill(String taskId) {

        // 幂等性校验：同一来源任务已有记忆时跳过，防止重复蒸馏
        FindAllAgentMemoryRequest existRequest = new FindAllAgentMemoryRequest();
        existRequest.setSourceTaskId(taskId);
        List<AgentMemory> existingMemories = agentMemoryView.findAll(existRequest);
        if (existingMemories != null && !existingMemories.isEmpty()) {
            log.warn("记忆蒸馏跳过：来源任务已有记忆，taskId={}", taskId);
            return;
        }

        // 查询来源任务（非事务读取）
        Task task = taskView.findById(taskId);
        if (task == null) {
            log.warn("记忆蒸馏跳过：任务不存在，taskId={}", taskId);
            return;
        }

        // 查询任务执行详情（非事务读取）
        List<TaskDetail> taskDetails = taskDetailView.findAllByTaskIds(Collections.singletonList(taskId));
        if (taskDetails == null || taskDetails.isEmpty()) {
            log.warn("记忆蒸馏跳过：任务无执行详情，taskId={}", taskId);
            return;
        }

        // 过滤失败步骤，仅保留成功步骤用于参数识别和步骤提炼
        // 修订场景下部分步骤已失败，失败步骤不应纳入新版本记忆
        List<TaskDetail> successDetails = taskDetails.stream().filter(d -> !AgentExecutionStatusProcess.FAILED.equals(d.getExecStatus())).toList();
        if (successDetails.isEmpty()) {
            log.warn("记忆蒸馏跳过：任务无成功步骤，taskId={}", taskId);
            return;
        }

        // 从任务的requestParams中解析原始调度请求，提取clientId和userId
        CommandDispatchRequest originalRequest = parseOriginalRequest(task);

        // 查询执行事件，用于提取原子命令信息（非事务读取）
        List<ExecutionEvent> events = executionEventView.findAllByTaskIds(Collections.singletonList(taskId));

        // 调用 AI 识别参数占位符（非事务，避免长事务持有数据库连接）
        DistillResult distillResult = identifyParameters(task, successDetails, originalRequest);

        // 判断蒸馏场景：任务已关联记忆ID时为修订场景，否则为首次探索沉淀
        boolean isRevision = task.getMemoryId() != null && !task.getMemoryId().isBlank();

        // 修订场景下，查找旧记忆（非事务读取，后续在保存事务中重新读取并退役）
        AgentMemory oldMemory = null;
        if (isRevision) {
            oldMemory = agentMemoryView.findById(task.getMemoryId());

            // 校验旧记忆与当前任务属于同一智能体，防止数据错误导致误退役
            if (oldMemory != null && !task.getAgentId().equals(oldMemory.getAgentId())) {
                log.error("记忆修订跳过：旧记忆与任务不属于同一智能体，taskId={}, taskAgentId={}, memoryAgentId={}", task.getId(), task.getAgentId(), oldMemory.getAgentId());
                oldMemory = null;
            }
        }

        // 在独立事务中执行保存操作，将AI调用与数据库写入分离
        // 通过代理调用确保 @Transactional 注解生效，避免自调用绕过 Spring AOP
        self.saveDistillResult(task, successDetails, originalRequest, distillResult, isRevision, oldMemory, events);
    }

    /**
     * 在独立事务中保存蒸馏结果。
     * <p>将蒸馏的数据读取和 AI 调用从事务中分离，仅在保存阶段开启事务，
     * 避免 AI 调用耗时数秒到数十秒期间持有数据库连接和行锁。</p>
     *
     * @param task            来源任务
     * @param successDetails  成功步骤列表
     * @param originalRequest 原始调度请求
     * @param distillResult   AI参数识别结果
     * @param isRevision      是否为修订场景
     * @param oldMemory       修订场景下的旧记忆
     * @param events          执行事件列表
     */
    @Transactional(rollbackFor = Exception.class)
    void saveDistillResult(Task task, List<TaskDetail> successDetails, CommandDispatchRequest originalRequest, DistillResult distillResult, boolean isRevision, AgentMemory oldMemory,
                           List<ExecutionEvent> events) {

        // 修订场景下，在事务内重新读取旧记忆并退役，确保操作原子性
        if (isRevision && oldMemory != null) {
            AgentMemory freshOldMemory = agentMemoryView.findById(oldMemory.getId());
            if (freshOldMemory != null && (AgentMemoryVersionStatusProcess.DRAFT.equals(freshOldMemory.getVersionStatus()) || AgentMemoryVersionStatusProcess.PUBLISHED.equals(
                            freshOldMemory.getVersionStatus()))) {
                AgentMemoryVersionStatusProcess previousStatus = freshOldMemory.getVersionStatus();
                freshOldMemory.setVersionStatus(AgentMemoryVersionStatusProcess.RETIRED);
                agentMemoryView.updateById(freshOldMemory);
                log.info("旧版本记忆已退役：memoryId={}, versionNo={}, previousStatus={}", freshOldMemory.getId(), freshOldMemory.getVersionNo(), previousStatus);
            }
        }

        // 创建记忆草稿
        AgentMemory memory = createMemoryDraft(task, successDetails, originalRequest, distillResult, isRevision, oldMemory);
        agentMemoryView.save(memory);

        // 从任务详情提炼记忆步骤
        List<AgentMemoryStep> steps = distillSteps(memory.getId(), successDetails, events, distillResult);
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
     * 调用 AI 识别参数占位符。
     *
     * <p>将任务名称和步骤详情提交给 AI，由 AI 判断哪些值是可参数化的变量，
     * 返回参数定义和含占位符的步骤模板。</p>
     *
     * @param task            来源任务
     * @param taskDetails     任务详情列表
     * @param originalRequest 原始调度请求（可能为null），用于提取userId和sessionId
     * @return 蒸馏结果（含参数定义和步骤参数映射）
     */
    private DistillResult identifyParameters(Task task, List<TaskDetail> taskDetails, CommandDispatchRequest originalRequest) {
        try {

            // 构造参数识别提示词
            String prompt = buildParameterIdentificationPrompt(task, taskDetails);

            // 调用 AI 进行参数识别
            AgentAiRequest aiRequest = new AgentAiRequest();
            aiRequest.setPromptContent(prompt);
            aiRequest.setCommandContent("参数识别");
            aiRequest.setAgentId(task.getAgentId());

            // 从原始请求中提取userId和sessionId，确保AI调用具备完整上下文
            aiRequest.setUserId(originalRequest != null && originalRequest.getUserId() != null ? originalRequest.getUserId() : "");
            aiRequest.setSessionId(originalRequest != null && originalRequest.getSessionId() != null ? originalRequest.getSessionId() : "");

            AgentAiResponse aiResponse = agentAiClient.chat(aiRequest);

            // 解析 AI 返回的参数识别结果
            return parseDistillResult(aiResponse);
        } catch (Exception e) {
            log.warn("AI参数识别失败，使用原始值作为模板，taskId={}", task.getId(), e);
            return new DistillResult();
        }
    }

    /**
     * 构造参数识别提示词。
     *
     * @param task        来源任务
     * @param taskDetails 任务详情列表
     * @return 提示词内容
     */
    private String buildParameterIdentificationPrompt(Task task, List<TaskDetail> taskDetails) {
        StringBuilder builder = new StringBuilder();
        builder.append("你是一个参数识别助手。请分析以下任务执行轨迹，识别其中可参数化的变量。\n\n");
        builder.append("任务名称：").append(task.getTaskName()).append("\n\n");
        builder.append("执行步骤：\n");

        // 遍历步骤构造提示词
        for (int i = 0; i < taskDetails.size(); i++) {
            TaskDetail detail = taskDetails.get(i);
            builder.append("步骤").append(i + 1).append("：");
            builder.append("名称=").append(detail.getTaskName());
            builder.append("，参数=").append(detail.getRequestParams());
            builder.append("\n");
        }

        builder.append("\n请按以下JSON格式返回结果，不要返回其他内容：\n");
        builder.append("{\n");
        builder.append("  \"memoryName\": \"用{param}占位符替换可变值的记忆名称\",\n");
        builder.append("  \"paramsDefinition\": {\"paramName\": {\"type\": \"string\", \"description\": \"参数说明\"}},\n");
        builder.append("  \"stepParams\": [");
        builder.append("{\"stepIndex\": 0, \"argsTemplate\": \"用{param}占位符替换可变值后的参数JSON\"}");
        builder.append("]\n");
        builder.append("}\n");
        return builder.toString();
    }

    /**
     * 解析 AI 返回的参数识别结果。
     *
     * @param aiResponse AI 响应
     * @return 蒸馏结果
     */
    private DistillResult parseDistillResult(AgentAiResponse aiResponse) {
        DistillResult result = new DistillResult();

        // AI 响应为空或失败时使用默认值
        if (aiResponse == null || !Boolean.TRUE.equals(aiResponse.getSuccess()) || aiResponse.getResponseContent() == null) {
            return result;
        }

        try {
            String content = aiResponse.getResponseContent().trim();

            // 提取 JSON 部分
            int jsonStart = content.indexOf('{');
            int jsonEnd = content.lastIndexOf('}');
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                String json = content.substring(jsonStart, jsonEnd + 1);
                Map<String, Object> parsed = JsonUtils.toJsonObj(json, Map.class);

                // 解析记忆名称
                if (parsed.containsKey("memoryName")) {
                    result.memoryName = (String) parsed.get("memoryName");
                }

                // 解析参数定义
                if (parsed.containsKey("paramsDefinition")) {
                    result.paramsDefinition = JsonUtils.toJsonStr(parsed.get("paramsDefinition"));
                }

                // 解析步骤参数模板
                if (parsed.containsKey("stepParams")) {
                    Object stepParamsObj = parsed.get("stepParams");
                    if (stepParamsObj instanceof List) {
                        @SuppressWarnings("unchecked") List<Map<String, Object>> stepParamsList = (List<Map<String, Object>>) stepParamsObj;
                        for (Map<String, Object> sp : stepParamsList) {
                            int stepIndex = sp.containsKey("stepIndex") ? ((Number) sp.get("stepIndex")).intValue() : -1;
                            String argsTemplate = resolveArgsTemplateFromAiResult(sp.get("argsTemplate"));
                            if (stepIndex >= 0) {
                                result.stepArgsTemplates.put(stepIndex, argsTemplate);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析AI参数识别结果失败，使用默认值", e);
        }

        return result;
    }

    /**
     * 从AI参数识别结果中解析argsTemplate字段。
     *
     * <p>AI返回的argsTemplate可能是JSON对象（Map）或纯字符串。
     * 对象形式直接序列化为JSON字符串；字符串形式直接使用，
     * 避免双重序列化导致存储内容带有多余引号和转义符。</p>
     *
     * @param argsTemplateObj AI返回的argsTemplate原始对象
     * @return 解析后的参数模板字符串
     */
    private String resolveArgsTemplateFromAiResult(Object argsTemplateObj) {

        // AI未返回argsTemplate时使用空字符串
        if (argsTemplateObj == null) {
            return "";
        }

        // 对象形式（Map/List）序列化为JSON字符串
        if (argsTemplateObj instanceof Map || argsTemplateObj instanceof List) {
            return JsonUtils.toJsonStr(argsTemplateObj);
        }

        // 字符串形式直接使用，避免双重序列化
        return argsTemplateObj.toString();
    }

    /**
     * 创建记忆草稿。
     *
     * @param task            来源任务
     * @param taskDetails     任务详情列表
     * @param originalRequest 原始调度请求（可能为null）
     * @param distillResult   AI参数识别结果
     * @param isRevision      是否为修订场景
     * @param oldMemory       修订场景下的旧记忆（可能为null）
     * @return 记忆草稿
     */
    private AgentMemory createMemoryDraft(Task task, List<TaskDetail> taskDetails, CommandDispatchRequest originalRequest, DistillResult distillResult, boolean isRevision,
                                          AgentMemory oldMemory) {
        AgentMemory memory = new AgentMemory();
        memory.setAgentId(task.getAgentId());

        // AI识别的记忆名称优先，否则使用任务名称
        memory.setMemoryName(distillResult.memoryName != null ? distillResult.memoryName : task.getTaskName());

        // AI识别的参数定义优先，否则为空对象
        memory.setParamsDefinition(distillResult.paramsDefinition != null ? distillResult.paramsDefinition : "{}");

        // 版本号计算：修订场景沿血脉链路追溯最大版本号后递增，首次探索从1开始
        int nextVersionNo = isRevision && oldMemory != null ? calcNextVersionNo(oldMemory) : 1;
        memory.setVersionNo(nextVersionNo);

        memory.setVersionStatus(AgentMemoryVersionStatusProcess.DRAFT);

        // 填充父记忆ID：修订场景指向旧记忆，首次探索为空字符串
        // 使用空字符串而非null，与数据库DEFAULT ''保持一致，避免MyBatis-Plus NOT_NULL策略导致null不写入
        memory.setParentMemoryId(isRevision && oldMemory != null ? oldMemory.getId() : "");

        memory.setSourceTaskId(task.getId());
        memory.setSummary(buildSummary(taskDetails));

        // 修订场景使用MEMORY_REVISE，首次探索使用AI_EXPLORATION
        memory.setCreateReason(isRevision ? "MEMORY_REVISE" : "AI_EXPLORATION");

        // 从原始请求中提取clientId和userId，原始请求解析失败时从旧记忆继承
        String clientId = originalRequest != null && originalRequest.getClientId() != null ? originalRequest.getClientId() : "";
        String userId = originalRequest != null && originalRequest.getUserId() != null ? originalRequest.getUserId() : "";

        // 修订场景下原始请求可能为null（旧数据格式），从旧记忆继承clientId和userId
        if (isRevision && oldMemory != null) {
            if (clientId.isBlank() && oldMemory.getClientId() != null) {
                clientId = oldMemory.getClientId();
            }
            if (userId.isBlank() && oldMemory.getUserId() != null) {
                userId = oldMemory.getUserId();
            }
        }

        memory.setClientId(clientId);
        memory.setUserId(userId);
        memory.setCreateUserId(userId);

        memory.setStatus(Status.ON);
        memory.setReserve("");
        memory.setRemark("AI探索自动蒸馏");
        return memory;
    }

    /**
     * 从任务详情提炼记忆步骤。
     *
     * <p>原子命令信息优先从执行事件获取，当执行事件缺失时，
     * 从 atomic_command 表（协议定义，由客户端通过 system.capability 上报）兜底查找。</p>
     *
     * @param memoryId      记忆ID
     * @param taskDetails   任务详情列表
     * @param events        执行事件列表，用于提取原子命令信息
     * @param distillResult AI参数识别结果
     * @return 记忆步骤列表
     */
    private List<AgentMemoryStep> distillSteps(String memoryId, List<TaskDetail> taskDetails, List<ExecutionEvent> events, DistillResult distillResult) {
        List<AgentMemoryStep> steps = new ArrayList<>();

        // 构建taskDetailId到ExecutionEvent的映射，用于提取原子命令信息
        Map<String, ExecutionEvent> eventByTaskDetailId = events.stream()
                                                                .filter(e -> e.getTaskDetailId() != null && !e.getTaskDetailId().isBlank())
                                                                .collect(Collectors.toMap(ExecutionEvent::getTaskDetailId, e -> e, (existing, replacement) -> replacement));

        // 按序号将任务详情转换为记忆步骤，序号从10开始递增
        int sequenceNo = 10;
        for (int i = 0; i < taskDetails.size(); i++) {
            TaskDetail detail = taskDetails.get(i);
            AgentMemoryStep step = new AgentMemoryStep();
            step.setMemoryId(memoryId);
            step.setSequenceNo(sequenceNo);
            sequenceNo += 10;

            // 从执行事件中提取原子命令信息
            ExecutionEvent event = eventByTaskDetailId.get(detail.getId());
            String atomicCommandId = resolveAtomicCommandId(detail, event);
            String atomicCommandCode = resolveAtomicCommandCode(detail, event);
            step.setAtomicCommandId(atomicCommandId);
            step.setAtomicCommandCode(atomicCommandCode);

            step.setStepName(detail.getTaskName() != null ? detail.getTaskName() : "步骤" + (i + 1));

            // AI识别的参数模板优先，否则使用原始请求参数
            String argsTemplate = distillResult.stepArgsTemplates.getOrDefault(i, detail.getRequestParams());
            step.setArgsTemplate(argsTemplate);

            // 设置默认延迟和超时
            step.setDelayMinMs(100);
            step.setDelayMaxMs(500);
            step.setTimeoutMs(30000);
            step.setSuccessAssertion("");
            step.setFailureStrategy("STOP");
            step.setStatus("ON");
            steps.add(step);
        }

        return steps;
    }

    /**
     * 解析原子命令ID。
     * <p>优先从执行事件获取，当执行事件缺失时，
     * 从 atomic_command 表（协议定义，由客户端通过 system.capability 上报）兜底查找。</p>
     *
     * @param detail 任务详情
     * @param event  执行事件，可能为null
     * @return 原子命令ID
     */
    private String resolveAtomicCommandId(TaskDetail detail, ExecutionEvent event) {
        if (event != null && event.getAtomicCommandId() != null && !event.getAtomicCommandId().isBlank()) {
            return event.getAtomicCommandId();
        }

        // 执行事件缺失时，从 atomic_command 表按命令编码兜底查找
        AtomicCommand command = findAtomicCommandByTaskDetail(detail);
        if (command != null) {
            log.info("执行事件缺失，从协议定义表兜底查找原子命令ID：taskDetailId={}, atomicCommandId={}", detail.getId(), command.getId());
            return command.getId();
        }

        log.warn("无法获取原子命令ID：taskDetailId={}，执行事件缺失且协议定义表未匹配", detail.getId());
        return "";
    }

    /**
     * 解析原子命令编码。
     * <p>优先从执行事件获取，当执行事件缺失时，
     * 从 atomic_command 表（协议定义，由客户端通过 system.capability 上报）兜底查找。</p>
     *
     * @param detail 任务详情
     * @param event  执行事件，可能为null
     * @return 原子命令编码
     */
    private String resolveAtomicCommandCode(TaskDetail detail, ExecutionEvent event) {
        if (event != null && event.getAtomicCommandCode() != null && !event.getAtomicCommandCode().isBlank()) {
            return event.getAtomicCommandCode();
        }

        // 执行事件缺失时，从 atomic_command 表按命令编码兜底查找
        AtomicCommand command = findAtomicCommandByTaskDetail(detail);
        if (command != null) {
            log.info("执行事件缺失，从协议定义表兜底查找原子命令编码：taskDetailId={}, atomicCommandCode={}", detail.getId(), command.getCommand());
            return command.getCommand();
        }

        log.warn("无法获取原子命令编码：taskDetailId={}，执行事件缺失且协议定义表未匹配", detail.getId());
        return "";
    }

    /**
     * 从 atomic_command 表按任务详情匹配原子命令定义。
     * <p>原子命令定义由执行器客户端通过 system.capability 上报并持久化到 atomic_command 表。
     * 当执行事件缺失时，通过步骤名称匹配命令名称或命令编码来兜底查找。</p>
     *
     * @param detail 任务详情
     * @return 匹配的原子命令定义，未找到返回null
     */
    private AtomicCommand findAtomicCommandByTaskDetail(TaskDetail detail) {
        String taskName = detail.getTaskName();
        if (taskName == null || taskName.isBlank()) {
            return null;
        }

        // 按命令名称匹配原子命令定义
        FindOneAtomicCommandRequest findByName = new FindOneAtomicCommandRequest();
        findByName.setName(taskName);
        AtomicCommand command = atomicCommandView.findOne(findByName);
        if (command != null) {
            return command;
        }

        // 按命令编码匹配原子命令定义
        FindOneAtomicCommandRequest findByCode = new FindOneAtomicCommandRequest();
        findByCode.setCommand(taskName);
        return atomicCommandView.findOne(findByCode);
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

    /**
     * 计算修订场景的下一个版本号。
     *
     * <p>沿 parentMemoryId 血脉链路追溯所有版本，取最大版本号 + 1。
     * 避免并发修订或异常中断后恢复时产生版本号冲突。</p>
     *
     * <p>子代遍历时使用 visited 集合去重，防止数据异常导致环路引用时无限循环。</p>
     *
     * @param oldMemory 被修订的旧记忆
     * @return 下一个版本号
     */
    private int calcNextVersionNo(AgentMemory oldMemory) {
        int maxVersionNo = oldMemory.getVersionNo() != null ? oldMemory.getVersionNo() : 0;

        // 沿 parentMemoryId 链路向上追溯，收集所有祖先版本号
        String parentId = oldMemory.getParentMemoryId();
        int maxDepth = 50;
        while (parentId != null && !parentId.isBlank() && maxDepth-- > 0) {
            AgentMemory parent = agentMemoryView.findById(parentId);
            if (parent == null) {
                break;
            }
            int parentVersionNo = parent.getVersionNo() != null ? parent.getVersionNo() : 0;
            maxVersionNo = Math.max(maxVersionNo, parentVersionNo);
            parentId = parent.getParentMemoryId();
        }

        // 按 parentMemoryId 定向查询后代版本，避免全量加载该智能体下所有记忆
        // visited 集合防止环路引用导致无限循环
        Set<String> visited = new HashSet<>();
        List<String> toVisit = new ArrayList<>();
        toVisit.add(oldMemory.getId());
        visited.add(oldMemory.getId());
        while (!toVisit.isEmpty()) {
            String currentId = toVisit.remove(toVisit.size() - 1);

            // 仅查询 parentMemoryId 指向当前记忆的子版本
            FindAllAgentMemoryRequest childRequest = new FindAllAgentMemoryRequest();
            childRequest.setParentMemoryId(currentId);
            List<AgentMemory> children = agentMemoryView.findAll(childRequest);

            for (AgentMemory child : children) {

                // 跳过已访问的记忆，防止环路引用
                if (visited.contains(child.getId())) {
                    log.warn("版本链路检测到环路引用，跳过：memoryId={}", child.getId());
                    continue;
                }

                int childVersionNo = child.getVersionNo() != null ? child.getVersionNo() : 0;
                maxVersionNo = Math.max(maxVersionNo, childVersionNo);
                toVisit.add(child.getId());
                visited.add(child.getId());
            }
        }

        return maxVersionNo + 1;
    }

    /**
     * 蒸馏结果内部类，承载 AI 参数识别的输出。
     */
    private static class DistillResult {

        /**
         * 含占位符的记忆名称
         */
        String memoryName;

        /**
         * 参数定义JSON
         */
        String paramsDefinition;

        /**
         * 步骤序号到参数模板的映射
         */
        java.util.Map<Integer, String> stepArgsTemplates = new java.util.HashMap<>();
    }
}