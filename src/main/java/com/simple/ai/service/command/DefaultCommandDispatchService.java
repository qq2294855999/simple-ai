package com.simple.ai.service.command;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.simple.ai.common.dto.agent.AgentAiRequest;
import com.simple.ai.common.dto.agent.AgentAiResponse;
import com.simple.ai.common.dto.agent.AgentContext;
import com.simple.ai.common.dto.atomicCommand.FindAllAtomicCommandRequest;
import com.simple.ai.common.dto.command.AtomicCommandInvokeRequest;
import com.simple.ai.common.dto.command.CommandDispatchProgressEvent;
import com.simple.ai.common.dto.command.CommandDispatchRequest;
import com.simple.ai.common.dto.command.CommandDispatchResponse;
import com.simple.ai.common.dto.taskDetail.FindAllTaskDetailRequest;
import com.simple.ai.common.entity.agentClient.AgentClient;
import com.simple.ai.common.entity.agentDefinition.AgentDefinition;
import com.simple.ai.common.entity.agentSkill.AgentSkill;
import com.simple.ai.common.entity.atomicCommand.AtomicCommand;
import com.simple.ai.common.entity.task.Task;
import com.simple.ai.common.entity.taskDetail.TaskDetail;
import com.simple.ai.common.enums.AgentClientStatusProcess;
import com.simple.ai.common.enums.AgentExecutionStatusProcess;
import com.simple.ai.common.enums.AgentStepTypeProcess;
import com.simple.ai.common.service.agent.AgentAiClient;
import com.simple.ai.common.service.command.CommandDispatchService;
import com.simple.ai.common.service.command.SubAgentDispatchService;
import com.simple.ai.common.service.memory.MemoryDistiller;
import com.simple.ai.common.service.memory.MemoryExecutor;
import com.simple.ai.common.service.memory.MemoryMatcher;
import com.simple.ai.common.service.session.AgentSessionService;
import com.simple.ai.common.view.atomicCommand.AtomicCommandView;
import com.simple.ai.common.view.task.TaskView;
import com.simple.ai.common.view.taskDetail.TaskDetailView;
import com.simple.ai.service.agent.AgentContextAssembler;
import com.simple.ai.view.agentClient.AgentClientRepository;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.core.utils.JsonUtils;
import com.simple.common.mp.common.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * 默认智能体命令调度服务实现。
 *
 * @author qty
 */
@Slf4j
@Service
@Transactional
class DefaultCommandDispatchService implements CommandDispatchService, InternalCommandDispatchExecutor {

    /**
     * 子智能体递归最大深度
     */
    private static final int MAX_SUB_AGENT_DEPTH = 3;

    /**
     * 单个循环步骤最大执行次数
     */
    private static final int MAX_LOOP_COUNT = 5;

    /**
     * 智能体上下文组装器
     */
    @Autowired
    private AgentContextAssembler agentContextAssembler;

    /**
     * 记忆匹配器（AI意图识别）
     */
    @Autowired
    private MemoryMatcher memoryMatcher;

    /**
     * 记忆执行器（按记忆步骤直接执行）
     */
    @Autowired
    private MemoryExecutor memoryExecutor;

    /**
     * 记忆蒸馏器（AI探索成功后提炼记忆）
     */
    @Autowired
    private MemoryDistiller memoryDistiller;

    /**
     * 智能体 AI 调用客户端
     */
    @Autowired
    private AgentAiClient agentAiClient;

    /**
     * 原子命令执行器注册表
     */
    @Autowired
    private AtomicCommandExecutorRegistry atomicCommandExecutorRegistry;

    /**
     * 子智能体调度服务
     */
    @Autowired
    private SubAgentDispatchService subAgentDispatchService;

    /**
     * 智能体会话服务
     */
    @Autowired
    private AgentSessionService agentSessionService;

    /**
     * 客户端实例仓库，用于按用户查询在线客户端
     */
    @Autowired
    private AgentClientRepository agentClientRepository;

    /**
     * 原子命令视图
     */
    @Autowired
    private AtomicCommandView atomicCommandView;

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

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public CommandDispatchResponse dispatch(CommandDispatchRequest request) {
        return dispatchStream(request, progressEvent -> {
        });
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public CommandDispatchResponse dispatchStream(CommandDispatchRequest request, Consumer<CommandDispatchProgressEvent> progressConsumer) {
        return dispatchInternal(request, progressConsumer, "", 0);
    }

    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public CommandDispatchResponse dispatchInternal(CommandDispatchRequest request, Consumer<CommandDispatchProgressEvent> progressConsumer,
                                                    String parentTaskId, int recursionDepth) {

        // 通过内部执行器入口承接子智能体递归上下文
        return executeDispatchInternal(request, progressConsumer, parentTaskId, recursionDepth);
    }

    /**
     * 执行智能体命令内部调度。
     *
     * @param request 命令调度请求
     * @param progressConsumer 进度事件消费者
     * @param parentTaskId 父任务ID
     * @param recursionDepth 当前递归深度
     * @return 命令调度响应
     */
    private CommandDispatchResponse executeDispatchInternal(CommandDispatchRequest request, Consumer<CommandDispatchProgressEvent> progressConsumer,
                                                            String parentTaskId, int recursionDepth) {

        // 参数校验：智能体ID不能为空
        AssertUtils.notEmpty(request.getAgentId(), "智能体ID不能为空");

        // 参数校验：命令名称不能为空
        AssertUtils.notEmpty(request.getCommandName(), "命令名称不能为空");

        // 参数校验：命令内容不能为空
        AssertUtils.notEmpty(request.getCommandContent(), "命令内容不能为空");

        // 校验子智能体递归深度，防止配置环路导致无限调度
        AssertUtils.isTrue(recursionDepth <= MAX_SUB_AGENT_DEPTH, "子智能体递归调度超过安全深度");

        // 解析客户端ID：用户指定优先，否则由后续逻辑自动匹配唯一在线客户端
        resolveClientIdIfAbsent(request);

        // 创建任务主记录并标记执行中
        Task task = createRunningTask(request, parentTaskId);
        publishProgress(progressConsumer, request, task, "TASK_CREATED", "任务已创建", "", Boolean.FALSE, "");

        try {
            // 组装智能体上下文
            publishProgress(progressConsumer, request, task, "CONTEXT_ASSEMBLING", "正在装配智能体上下文", "", Boolean.FALSE, "");
            AgentContext context = agentContextAssembler.assemble(request);
            publishContextTraceProgress(progressConsumer, request, task, context);

            // AI意图识别匹配已发布记忆
            publishProgress(progressConsumer, request, task, "MEMORY_MATCHING", "正在匹配候选记忆", "", Boolean.FALSE, "");
            String matchedMemoryId = memoryMatcher.match(request.getCommandContent(), context);
            publishMemoryMatchProgress(progressConsumer, request, task, matchedMemoryId);

            // 执行智能体命令流程
            String responseContent = executeCommand(task, request, context, matchedMemoryId, progressConsumer, recursionDepth);

            // 标记任务执行成功
            markTaskSuccess(task, responseContent);

            // 保存会话摘要
            saveSessionSummary(request, responseContent);
            publishProgress(progressConsumer, request, task, "TASK_COMPLETED", "任务执行成功", responseContent, Boolean.TRUE, "");
            return buildSuccessResponse(task, responseContent);
        } catch (Exception e) {
            String failureReason = resolveFailureReason(e);

            // 保存失败任务详情，确保失败链路可追踪且不重复落库
            saveFailedTaskDetailIfAbsent(task, request, failureReason);

            // 标记任务执行失败
            markTaskFailed(task, failureReason);
            publishProgress(progressConsumer, request, task, "TASK_FAILED", "任务执行失败", "", Boolean.TRUE, failureReason);
            return buildFailedResponse(task, failureReason);
        }
    }

    /**
     * 创建执行中的任务主记录。
     *
     * @param request 命令调度请求
     * @param parentTaskId 父任务ID
     * @return 任务主记录
     */
    private Task createRunningTask(CommandDispatchRequest request, String parentTaskId) {
        Task task = new Task();
        task.setMemoryId("");
        task.setMemoryVersionNo(null);
        task.setAgentId(request.getAgentId());
        task.setTaskName(request.getCommandName());
        task.setParentTaskId(parentTaskId == null ? "" : parentTaskId);
        task.setNextTaskId("");
        task.setStepType(AgentStepTypeProcess.ATOMIC_COMMAND);
        task.setBranchCondition("");
        task.setBranchRoute("");
        task.setRequestParams(JsonUtils.toJsonStr(request));
        task.setReturnParams("");
        task.setExecStatus(AgentExecutionStatusProcess.RUNNING);
        task.setFailureReason("");
        task.setStatus(Status.ON);
        task.setReserve("");
        task.setRemark("智能体命令调度任务");
        taskView.save(task);
        return task;
    }

    /**
     * 执行智能体命令流程。
     *
     * @param task 任务主记录
     * @param request 命令调度请求
     * @param context 智能体上下文
     * @param matchedMemoryId 命中的记忆ID，为空表示未命中
     * @param progressConsumer 进度事件消费者
     * @param recursionDepth 当前递归深度
     * @return 响应内容
     */
    private String executeCommand(Task task, CommandDispatchRequest request, AgentContext context, String matchedMemoryId,
                                  Consumer<CommandDispatchProgressEvent> progressConsumer, int recursionDepth) {

        // 命中记忆时由 MemoryExecutor 直接按记忆步骤创建任务并下发客户端
        if (matchedMemoryId != null && !matchedMemoryId.isBlank()) {
            task.setMemoryId(matchedMemoryId);
            taskView.updateById(task);
            publishProgress(progressConsumer, request, task, "MEMORY_EXECUTING", "按记忆直接执行", matchedMemoryId, Boolean.FALSE, "");
            return executeMemoryDirectly(task, request, context, matchedMemoryId, progressConsumer);
        }

        // 未命中记忆时调用 AI 生成探索响应
        return executeAiExploration(task, request, context, progressConsumer);
    }

    /**
     * 按记忆直接执行：加载记忆步骤 → 替换参数占位符 → 创建 task_detail → 下发客户端。
     *
     * @param task 任务主记录
     * @param request 命令调度请求
     * @param context 智能体上下文
     * @param memoryId 命中的记忆ID
     * @param progressConsumer 进度事件消费者
     * @return 响应内容
     */
    private String executeMemoryDirectly(Task task, CommandDispatchRequest request, AgentContext context, String memoryId, Consumer<CommandDispatchProgressEvent> progressConsumer) {

        // 委托 MemoryExecutor 按记忆步骤直接执行
        String result = memoryExecutor.execute(task, request, memoryId, progressConsumer);

        // 标记任务执行完成
        if (result != null && !result.contains("失败")) {
            markTaskSuccess(task, result);
        } else {
            markTaskFailed(task, result != null ? result : "记忆执行失败");
        }
        return result;
    }

    /**
     * 执行 AI 探索流程。
     *
     * <p>AI 输出结构化 AgentExecutionPlan JSON，包含命令列表和校验规则。
     * 探索成功后触发记忆沉淀判定，按规则生成 DRAFT 版本记忆。</p>
     *
     * @param task 任务主记录
     * @param request 命令调度请求
     * @param context 智能体上下文
     * @param progressConsumer 进度事件消费者
     * @return 响应内容
     */
    private String executeAiExploration(Task task, CommandDispatchRequest request, AgentContext context,
                                        Consumer<CommandDispatchProgressEvent> progressConsumer) {
        publishProgress(progressConsumer, request, task, "AI_STARTED", "AI 开始生成探索方案", request.getCommandContent(), Boolean.FALSE, "");

        // 设置会话上下文，供 ToolCallback 在异步线程中获取 sessionId
        com.simple.ai.service.agent.AgentSessionContext.setCurrentSessionId(request.getSessionId());
        try {
            AgentAiRequest aiRequest = buildAiRequest(request, context);
            AgentAiResponse aiResponse = agentAiClient.chatStream(aiRequest, token -> publishAiTokenProgress(progressConsumer, request, task, token));

            // 持久化当前 AI 调用的不可变供应商和模型快照
            persistRuntimeSnapshot(task, aiResponse);
            saveAiTaskDetail(task, request, aiRequest, aiResponse);
            AssertUtils.isTrue(Boolean.TRUE.equals(aiResponse.getSuccess()), "AI探索执行失败");
            publishProgress(progressConsumer, request, task, "AI_COMPLETED", "AI 探索方案生成完成", aiResponse.getResponseContent(), Boolean.FALSE, "");

            // 触发记忆沉淀判定：AI 输出的结构化 AgentExecutionPlan 通过校验后，
            // 由记忆沉淀服务提炼最短执行链并创建 agent_memory_version (DRAFT)
            triggerMemoryPrecipitation(task, request, aiResponse);

            return aiResponse.getResponseContent();
        } finally {
            // 清理 ThreadLocal，防止内存泄漏
            com.simple.ai.service.agent.AgentSessionContext.clear();
        }
    }

    /**
     * 发布 AI token 进度事件。
     *
     * @param progressConsumer 进度事件消费者
     * @param request 命令调度请求
     * @param task 任务主记录
     * @param token token 内容
     */
    private void publishAiTokenProgress(Consumer<CommandDispatchProgressEvent> progressConsumer, CommandDispatchRequest request, Task task, String token) {

        // token 为空时不发布流式事件
        if (token == null || token.isBlank()) {
            return;
        }
        publishProgress(progressConsumer, request, task, "AI_TOKEN", "AI 生成内容片段", token, Boolean.FALSE, "");
    }

    /**
     * 构建原子命令调用请求。
     *
     * @param task 任务主记录
     * @param request 命令调度请求
     * @param commandContent 命令内容
     * @param atomicCommands 预加载的启用原子命令列表
     * @return 原子命令调用请求
     */
    private AtomicCommandInvokeRequest buildAtomicCommandRequest(Task task, CommandDispatchRequest request, String commandContent,
                                                                  List<AtomicCommand> atomicCommands) {
        AtomicCommand atomicCommand = findMatchedAtomicCommand(commandContent, atomicCommands);
        AtomicCommandInvokeRequest invokeRequest = new AtomicCommandInvokeRequest();
        invokeRequest.setTaskId(task.getId());
        invokeRequest.setTaskDetailId("");
        invokeRequest.setAtomicCommandId(resolveAtomicCommandId(atomicCommand));
        invokeRequest.setAtomicCommandRole(resolveAtomicCommandRole(atomicCommand));
        invokeRequest.setCommandContent(resolveCommandContent(request, commandContent, atomicCommand));
        invokeRequest.setRequestParams(request.getRequestParams());
        invokeRequest.setClientId(request.getClientId());
        return invokeRequest;
    }

    /**
     * 从预加载列表中匹配原子命令定义。
     *
     * @param commandContent 命令内容
     * @param preloadedCommands 预加载的启用原子命令列表
     * @return 原子命令定义
     */
    private AtomicCommand findMatchedAtomicCommand(String commandContent, List<AtomicCommand> preloadedCommands) {

        // 遍历预加载的启用原子命令，查找名称或命令内容命中的定义
        for (AtomicCommand atomicCommand : preloadedCommands) {
            if (isAtomicCommandMatched(commandContent, atomicCommand)) {
                return atomicCommand;
            }
        }
        return null;
    }

    /**
     * 加载当前智能体技能对应的启用原子命令列表。
     *
     * <p>在记忆步骤链执行前调用一次，后续所有步骤复用此列表，
     * 避免每个步骤都重复查询数据库。</p>
     *
     * <p>按技能ID列表批量查询一次，避免 N+1 循环查询。
     * 同时纳入 skill_id 为空的全局通用命令，
     * 实现"智能体 → 技能 → 原子命令"三级关联。</p>
     *
     * @param context 智能体上下文
     * @return 该智能体技能对应的启用原子命令列表
     */
    private List<AtomicCommand> loadEnabledAtomicCommands(AgentContext context) {
        List<String> skillIds = extractSkillIds(context);
        List<AtomicCommand> result = new ArrayList<>();

        // 按技能ID列表批量查询启用原子命令，避免 N+1 循环查询
        if (CollectionUtil.isNotEmpty(skillIds)) {
            FindAllAtomicCommandRequest batchRequest = new FindAllAtomicCommandRequest();
            batchRequest.setSkillIds(skillIds);
            batchRequest.setStatus(Status.ON);
            result.addAll(atomicCommandView.findAll(batchRequest));
        }

        // 追加 skill_id 为空的全局通用命令，确保基础执行器始终可用
        FindAllAtomicCommandRequest globalRequest = new FindAllAtomicCommandRequest();
        globalRequest.setSkillId("");
        globalRequest.setStatus(Status.ON);
        result.addAll(atomicCommandView.findAll(globalRequest));
        return result;
    }

    /**
     * 从智能体上下文中提取技能ID列表。
     *
     * @param context 智能体上下文
     * @return 技能ID列表
     */
    private List<String> extractSkillIds(AgentContext context) {
        List<String> skillIds = new ArrayList<>();

        // 上下文中无技能时不筛选，后续仅加载全局命令
        if (context.getSkills() == null || context.getSkills().isEmpty()) {
            return skillIds;
        }

        // 遍历技能列表收集ID
        for (AgentSkill skill : context.getSkills()) {
            if (skill.getId() != null && !skill.getId().isBlank()) {
                skillIds.add(skill.getId());
            }
        }
        return skillIds;
    }

    /**
     * 判断原子命令是否命中。
     *
     * @param commandContent 命令内容
     * @param atomicCommand 原子命令定义
     * @return 是否命中
     */
    private boolean isAtomicCommandMatched(String commandContent, AtomicCommand atomicCommand) {

        // 命令内容或原子命令定义为空时不允许模糊命中
        if (commandContent == null || commandContent.isBlank() || atomicCommand == null) {
            return false;
        }

        // 原子命令名称按完整词命中，避免短名称被长文本误包含
        if (isAtomicCommandTokenMatched(commandContent, atomicCommand.getName())) {
            return true;
        }
        return isAtomicCommandTokenMatched(commandContent, atomicCommand.getCommand());
    }

    /**
     * 判断原子命令令牌是否完整命中。
     *
     * @param commandContent 命令内容
     * @param atomicCommandText 原子命令名称或正文
     * @return 是否完整命中
     */
    private boolean isAtomicCommandTokenMatched(String commandContent, String atomicCommandText) {

        // 原子命令文本为空时不参与匹配
        if (atomicCommandText == null || atomicCommandText.isBlank()) {
            return false;
        }
        String trimmedCommandContent = commandContent.trim();
        String trimmedAtomicCommandText = atomicCommandText.trim();

        // 完全一致时优先视为准确命中
        if (trimmedCommandContent.equals(trimmedAtomicCommandText)) {
            return true;
        }
        Pattern tokenPattern = Pattern.compile("(^|[^\\p{IsAlphabetic}\\p{IsDigit}_])"
                + Pattern.quote(trimmedAtomicCommandText)
                + "($|[^\\p{IsAlphabetic}\\p{IsDigit}_])");
        return tokenPattern.matcher(trimmedCommandContent).find();
    }

    /**
     * 解析原子命令主键。
     *
     * @param atomicCommand 原子命令定义
     * @return 原子命令主键
     */
    private String resolveAtomicCommandId(AtomicCommand atomicCommand) {

        // 未匹配到原子命令时保留为空，由默认安全执行器处理
        if (atomicCommand == null) {
            return "";
        }
        return atomicCommand.getId();
    }

    /**
     * 解析原子命令作用。
     *
     * @param atomicCommand 原子命令定义
     * @return 原子命令作用
     */
    private String resolveAtomicCommandRole(AtomicCommand atomicCommand) {

        // 未匹配到原子命令时不指定作用，由默认安全执行器兜底
        if (atomicCommand == null) {
            return "";
        }
        return atomicCommand.getRole();
    }

    /**
     * 解析最终执行命令内容。
     *
     * @param request 命令调度请求
     * @param commandContent 步骤命令内容
     * @param atomicCommand 原子命令定义
     * @return 最终执行命令内容
     */
    private String resolveCommandContent(CommandDispatchRequest request, String commandContent, AtomicCommand atomicCommand) {

        // 匹配到原子命令时优先使用原子命令标准正文
        if (atomicCommand != null) {
            return atomicCommand.getCommand();
        }

        // 记忆步骤命令为空时回退到用户原始命令
        if (commandContent == null || commandContent.isBlank()) {
            return request.getCommandContent();
        }
        return commandContent;
    }

    /**
     * 构建 AI 调用请求。
     *
     * @param request 命令调度请求
     * @param context 智能体上下文
     * @return AI 调用请求
     */
    private AgentAiRequest buildAiRequest(CommandDispatchRequest request, AgentContext context) {
        AgentAiRequest aiRequest = new AgentAiRequest();

        // 透传智能体与显式模型选择，由运行时路由服务解析实际模型
        aiRequest.setAgentId(request.getAgentId());
        aiRequest.setModelId(request.getModelId());
        aiRequest.setPromptContent(context.getPromptContent());
        aiRequest.setCommandContent(request.getCommandContent());
        aiRequest.setSessionSummary(context.getSessionSummary());

        // 传递会话ID和用户ID，供工具回调中获取用户上下文
        aiRequest.setSessionId(request.getSessionId());
        aiRequest.setUserId(request.getUserId());
        return aiRequest;
    }

    /**
     * 保存 AI 探索任务详情。
     *
     * @param task 任务主记录
     * @param request 命令调度请求
     * @param aiRequest AI 调用请求
     * @param aiResponse AI 调用响应
     */
    private void saveAiTaskDetail(Task task, CommandDispatchRequest request, AgentAiRequest aiRequest, AgentAiResponse aiResponse) {
        TaskDetail taskDetail = new TaskDetail();
        taskDetail.setTaskId(task.getId());
        taskDetail.setTaskName(request.getCommandName());
        taskDetail.setParentTaskId("");
        taskDetail.setNextTaskId("");
        taskDetail.setStepType(AgentStepTypeProcess.JUDGE);
        taskDetail.setBranchCondition("");
        taskDetail.setBranchRoute("");
        taskDetail.setRequestParams(JsonUtils.toJsonStr(aiRequest));
        taskDetail.setReturnParams(JsonUtils.toJsonStr(aiResponse));
        taskDetail.setExecStatus(resolveDetailStatus(aiResponse.getSuccess()));
        taskDetail.setProviderId(aiResponse.getProviderId());
        taskDetail.setProviderName(aiResponse.getProviderName());
        taskDetail.setModelId(aiResponse.getModelId());
        taskDetail.setModelCode(aiResponse.getModelCode());
        taskDetail.setStatus(Status.ON);
        taskDetail.setReserve("");
        taskDetail.setRemark("AI探索执行详情");
        taskDetailView.save(taskDetail);
    }

    /**
     * 持久化 AI 调用的运行时模型快照。
     *
     * @param task 当前任务
     * @param aiResponse AI 调用响应
     */
    private void persistRuntimeSnapshot(Task task, AgentAiResponse aiResponse) {
        task.setProviderId(aiResponse.getProviderId());
        task.setProviderName(aiResponse.getProviderName());
        task.setModelId(aiResponse.getModelId());
        task.setModelCode(aiResponse.getModelCode());
        taskView.updateById(task);
    }

    /**
     * 缺失时保存失败任务详情。
     *
     * @param task 任务主记录
     * @param request 命令调度请求
     * @param failureReason 失败原因
     */
    private void saveFailedTaskDetailIfAbsent(Task task, CommandDispatchRequest request, String failureReason) {

        // 已存在失败详情时跳过兜底落库，避免外层异常重复记录
        if (hasFailedTaskDetail(task)) {
            return;
        }
        saveFailedTaskDetail(task, request, failureReason);
    }

    /**
     * 判断任务是否已有失败详情。
     *
     * @param task 任务主记录
     * @return 是否已有失败详情
     */
    private boolean hasFailedTaskDetail(Task task) {
        FindAllTaskDetailRequest findAllRequest = new FindAllTaskDetailRequest();
        findAllRequest.setTaskId(task.getId());
        findAllRequest.setExecStatus(AgentExecutionStatusProcess.FAILED);
        List<TaskDetail> taskDetails = taskDetailView.findAll(findAllRequest);
        return !taskDetails.isEmpty();
    }

    /**
     * 保存失败任务详情。
     *
     * @param task 任务主记录
     * @param request 命令调度请求
     * @param failureReason 失败原因
     */
    private void saveFailedTaskDetail(Task task, CommandDispatchRequest request, String failureReason) {
        TaskDetail taskDetail = new TaskDetail();
        taskDetail.setTaskId(task.getId());
        taskDetail.setTaskName(request.getCommandName());
        taskDetail.setParentTaskId("");
        taskDetail.setNextTaskId("");
        taskDetail.setStepType(AgentStepTypeProcess.JUDGE);
        taskDetail.setBranchCondition("");
        taskDetail.setBranchRoute("");
        taskDetail.setRequestParams(JsonUtils.toJsonStr(request));
        taskDetail.setReturnParams(failureReason == null ? "" : failureReason);
        taskDetail.setExecStatus(AgentExecutionStatusProcess.FAILED);
        taskDetail.setStatus(Status.ON);
        taskDetail.setReserve("");
        taskDetail.setRemark("智能体命令调度失败详情");
        taskDetailView.save(taskDetail);
    }

    /**
     * 发布记忆匹配进度事件。
     *
     * @param progressConsumer 进度事件消费者
     * @param request 命令调度请求
     * @param task 任务主记录
     * @param matchedMemoryId 命中的记忆ID，为空表示未命中
     */
    private void publishMemoryMatchProgress(Consumer<CommandDispatchProgressEvent> progressConsumer, CommandDispatchRequest request, Task task, String matchedMemoryId) {

        // 未命中记忆时发布未命中事件
        if (matchedMemoryId == null || matchedMemoryId.isBlank()) {
            publishProgress(progressConsumer, request, task, "MEMORY_MISSED", "未命中候选记忆，转入 AI 探索", "", Boolean.FALSE, "");
            return;
        }
        publishProgress(progressConsumer, request, task, "MEMORY_MATCHED", "已命中候选记忆", matchedMemoryId, Boolean.FALSE, "");
    }

    /**
     * 发布上下文装配安全摘要。
     *
     * @param progressConsumer 进度事件消费者
     * @param request 命令调度请求
     * @param task 调度任务
     * @param context 智能体上下文
     */
    private void publishContextTraceProgress(Consumer<CommandDispatchProgressEvent> progressConsumer,
                                             CommandDispatchRequest request, Task task, AgentContext context) {
        publishProgress(progressConsumer, request, task, "CONTEXT_ASSEMBLED", "智能体定义已装配",
                buildContextPayload(context), Boolean.FALSE, "");
        publishProgress(progressConsumer, request, task, "RULE_LOADED", "智能体规则已装配",
                buildCountPayload("rule", context.getRules().size()), Boolean.FALSE, "");
        publishProgress(progressConsumer, request, task, "SKILL_LOADED", "智能体技能已装配",
                buildCountPayload("skill", context.getSkills().size()), Boolean.FALSE, "");
        publishProgress(progressConsumer, request, task, "SUB_AGENT_LOADED", "子智能体关系已装配",
                buildCountPayload("subAgent", context.getSubAgentRelations().size()), Boolean.FALSE, "");
    }

    /**
     * 构建上下文安全摘要。
     *
     * @param context 智能体上下文
     * @return JSON 摘要
     */
    private String buildContextPayload(AgentContext context) {
        Map<String, Object> payload = new HashMap<>();
        AgentDefinition definition = context.getAgentDefinition();
        payload.put("agentName", definition.getName());
        payload.put("ruleCount", context.getRules().size());
        payload.put("skillCount", context.getSkills().size());
        payload.put("memoryCount", context.getMemories().size());
        payload.put("subAgentCount", context.getSubAgentRelations().size());
        return JsonUtils.toJsonStr(payload);
    }

    /**
     * 构建资源数量安全摘要。
     *
     * @param resourceType 资源类型
     * @param count 资源数量
     * @return JSON 摘要
     */
    private String buildCountPayload(String resourceType, int count) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("resourceType", resourceType);
        payload.put("count", count);
        return JsonUtils.toJsonStr(payload);
    }

    /**
     * 发布调度进度事件。
     *
     * @param progressConsumer 进度事件消费者
     * @param request 命令调度请求
     * @param task 任务主记录
     * @param eventType 事件类型
     * @param message 事件消息
     * @param payload 事件数据
     * @param completed 是否完成
     * @param failureReason 失败原因
     */
    private void publishProgress(Consumer<CommandDispatchProgressEvent> progressConsumer, CommandDispatchRequest request, Task task,
                                 String eventType, String message, String payload, Boolean completed, String failureReason) {

        // 未传入进度消费者时跳过事件发布
        if (progressConsumer == null) {
            return;
        }
        CommandDispatchProgressEvent event = buildProgressEvent(request, task, eventType, message, payload, completed, failureReason);
        try {
            progressConsumer.accept(event);
        } catch (RuntimeException e) {

            // 进度通道异常不改变业务任务状态，避免客户端断开导致任务被误标记失败
            log.warn("智能体命令调度进度事件发送失败，任务ID：{}，事件类型：{}", task.getId(), eventType, e);
        }
    }

    /**
     * 构建调度进度事件。
     *
     * @param request 命令调度请求
     * @param task 任务主记录
     * @param eventType 事件类型
     * @param message 事件消息
     * @param payload 事件数据
     * @param completed 是否完成
     * @param failureReason 失败原因
     * @return 调度进度事件
     */
    private CommandDispatchProgressEvent buildProgressEvent(CommandDispatchRequest request, Task task, String eventType, String message,
                                                            String payload, Boolean completed, String failureReason) {
        CommandDispatchProgressEvent event = new CommandDispatchProgressEvent();
        event.setTaskId(task.getId());
        event.setSessionId(request.getSessionId());
        event.setEventType(eventType);
        event.setStepId("");
        event.setStepName(message);
        event.setExecStatus(task.getExecStatus());
        event.setMessage(message);
        event.setPayload(resolveSafeProgressPayload(eventType, payload));
        event.setCompleted(completed);
        event.setFailureReason(failureReason);

        // 传递运行时模型快照供前端展示
        event.setProviderName(task.getProviderName());
        event.setModelCode(task.getModelCode());
        return event;
    }

    /**
     * 收敛执行轨迹事件载荷。
     *
     * @param eventType 事件类型
     * @param payload 原始事件载荷
     * @return 可公开展示的事件载荷
     */
    private String resolveSafeProgressPayload(String eventType, String payload) {

        // AI token 仅作为聊天消息流临时内容，不进入执行轨迹
        if ("AI_TOKEN".equals(eventType)) {
            return payload;
        }

        // 已结构化生成的资源统计摘要可直接用于时间线展示
        if ("CONTEXT_ASSEMBLED".equals(eventType) || "RULE_LOADED".equals(eventType)
                || "SKILL_LOADED".equals(eventType) || "SUB_AGENT_LOADED".equals(eventType)
                || "MEMORY_MATCHED".equals(eventType) || "MEMORY_SUMMARIZED".equals(eventType)) {
            return payload;
        }
        return "";
    }

    /**
     * 解析任务详情执行状态。
     *
     * @param success 是否成功
     * @return 执行状态
     */
    private AgentExecutionStatusProcess resolveDetailStatus(Boolean success) {

        // 执行响应不为成功时统一标记失败
        if (!Boolean.TRUE.equals(success)) {
            return AgentExecutionStatusProcess.FAILED;
        }
        return AgentExecutionStatusProcess.SUCCESS;
    }

    /**
     * 标记任务执行成功。
     *
     * @param task 任务主记录
     * @param responseContent 响应内容
     */
    private void markTaskSuccess(Task task, String responseContent) {
        task.setReturnParams(responseContent);
        task.setExecStatus(AgentExecutionStatusProcess.SUCCESS);
        task.setFailureReason("");
        taskView.updateById(task);
    }

    /**
     * 标记任务执行失败。
     *
     * @param task 任务主记录
     * @param failureReason 失败原因
     */
    private void markTaskFailed(Task task, String failureReason) {
        task.setExecStatus(AgentExecutionStatusProcess.FAILED);
        task.setFailureReason(failureReason);
        taskView.updateById(task);
    }

    /**
     * 保存会话摘要。
     *
     * @param request 命令调度请求
     * @param responseContent 响应内容
     */
    private void saveSessionSummary(CommandDispatchRequest request, String responseContent) {

        // 会话ID为空时不保存会话摘要
        if (request.getSessionId() == null || request.getSessionId().isBlank()) {
            return;
        }
        try {
            agentSessionService.saveSummary(request.getSessionId(), responseContent);
            agentSessionService.appendMessage(request.getSessionId(), request.getCommandContent());
        } catch (RuntimeException e) {

            // 会话摘要属于辅助上下文，写入失败不改变核心任务成功状态
            log.warn("智能体命令调度会话摘要写入失败，会话ID：{}", request.getSessionId(), e);
        }
    }

    /**
     * 解析客户端ID：用户指定优先，未指定时由后续自动匹配。
     *
     * @param request 命令调度请求
     */
    private void resolveClientIdIfAbsent(CommandDispatchRequest request) {

        // 用户已指定客户端ID时直接使用
        if (request.getClientId() != null && !request.getClientId().isBlank()) {
            return;
        }

        // 根据当前用户ID查询状态为ACTIVE的客户端列表
        String userId = request.getUserId();
        if (userId == null || userId.isBlank()) {
            return;
        }

        // 查询当前用户下所有ACTIVE状态的客户端
        LambdaQueryWrapper<AgentClient> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AgentClient::getUserId, userId).eq(AgentClient::getStatus, AgentClientStatusProcess.ACTIVE);
        List<AgentClient> activeClients = agentClientRepository.selectList(queryWrapper);

        // 唯一在线时自动绑定，多个在线时暂不绑定由前端指定
        if (activeClients.size() == 1) {
            AgentClient client = activeClients.get(0);
            request.setClientId(client.getId());
        }
    }

    /**
     * 触发记忆沉淀。
     *
     * <p>AI 探索成功后，由 MemoryDistiller 从 task + task_details 提炼记忆。
     * 复用当前会话的 AI 模型识别参数占位符，创建 agent_memory (DRAFT) + agent_memory_step × N。</p>
     *
     * @param task       任务主记录
     * @param request    命令调度请求
     * @param aiResponse AI 响应
     */
    private void triggerMemoryPrecipitation(Task task, CommandDispatchRequest request, AgentAiResponse aiResponse) {

        // AI 探索成功后，由 MemoryDistiller 从 task + task_details 提炼记忆
        try {
            memoryDistiller.distill(task.getId());
        } catch (RuntimeException e) {

            // 沉淀失败不影响主流程
            log.warn("记忆沉淀失败，taskId={}", task.getId(), e);
        }
    }

    /**
     * 解析完整失败原因。
     *
     * @param exception 异常对象
     * @return 完整失败原因
     */
    private String resolveFailureReason(Exception exception) {

        // 异常对象为空时返回调度失败默认语义
        if (exception == null) {
            return "智能体命令调度失败，未捕获到具体异常对象";
        }
        String message = exception.getMessage();

        // 异常消息存在时优先保留业务失败原因
        if (message != null && !message.isBlank()) {
            return message;
        }
        return "智能体命令调度失败，异常类型：" + exception.getClass().getSimpleName();
    }

    /**
     * 构建成功响应。
     *
     * @param task 任务主记录
     * @param responseContent 响应内容
     * @return 命令调度响应
     */
    private CommandDispatchResponse buildSuccessResponse(Task task, String responseContent) {
        CommandDispatchResponse response = new CommandDispatchResponse();
        response.setTaskId(task.getId());
        response.setExecStatus(AgentExecutionStatusProcess.SUCCESS);
        response.setResponseContent(responseContent);
        response.setFailureReason("");
        fillResponseRuntimeSnapshot(response, task);
        return response;
    }

    /**
     * 构建失败响应。
     *
     * @param task 任务主记录
     * @param failureReason 失败原因
     * @return 命令调度响应
     */
    private CommandDispatchResponse buildFailedResponse(Task task, String failureReason) {
        CommandDispatchResponse response = new CommandDispatchResponse();
        response.setTaskId(task.getId());
        response.setExecStatus(AgentExecutionStatusProcess.FAILED);
        response.setResponseContent("");
        response.setFailureReason(failureReason);
        fillResponseRuntimeSnapshot(response, task);
        return response;
    }

    /**
     * 填充调度响应的运行时模型快照。
     *
     * @param response 调度响应
     * @param task 调度任务
     */
    private void fillResponseRuntimeSnapshot(CommandDispatchResponse response, Task task) {
        response.setProviderId(task.getProviderId());
        response.setProviderName(task.getProviderName());
        response.setModelId(task.getModelId());
        response.setModelCode(task.getModelCode());
    }
}