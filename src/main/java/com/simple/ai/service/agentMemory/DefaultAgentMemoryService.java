package com.simple.ai.service.agentMemory;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simple.ai.common.copy.agentMemory.AgentMemoryCopyMapper;
import com.simple.ai.common.dto.agentMemory.*;
import com.simple.ai.common.dto.command.CommandDispatchRequest;
import com.simple.ai.common.entity.agentMemory.AgentMemory;
import com.simple.ai.common.entity.agentMemoryStep.AgentMemoryStep;
import com.simple.ai.common.entity.task.Task;
import com.simple.ai.common.enums.AgentExecutionStatusProcess;
import com.simple.ai.common.enums.AgentStepTypeProcess;
import com.simple.ai.common.service.agentMemory.AgentMemoryService;
import com.simple.ai.common.service.memory.MemoryDistiller;
import com.simple.ai.common.service.memory.MemoryExecutor;
import com.simple.ai.common.view.agentMemory.AgentMemoryView;
import com.simple.ai.common.view.agentMemoryStep.AgentMemoryStepView;
import com.simple.ai.common.view.task.TaskView;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.core.utils.JsonUtils;
import com.simple.common.mp.common.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 智能体记忆(agent_memory)默认接口实现
 *
 * @author qty
 */
@Slf4j
@Service
@Transactional
class DefaultAgentMemoryService implements AgentMemoryService {

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
     * 对象属性复制
     */
    @Autowired
    private AgentMemoryCopyMapper copy;

    /**
     * 记忆执行器
     */
    @Autowired
    private MemoryExecutor memoryExecutor;

    /**
     * 记忆蒸馏器，执行失败时触发修订
     */
    @Autowired
    private MemoryDistiller memoryDistiller;

    /**
     * 任务视图
     */
    @Autowired
    private TaskView taskView;

    /**
     * 自注入代理，确保 @Transactional(propagation = REQUIRES_NEW) 注解通过 Spring AOP 代理生效
     */
    @Lazy
    @Autowired
    private DefaultAgentMemoryService self;

    @Override
    public IPage<PageAgentMemoryResponse> findAll(PageAgentMemoryRequest pageRequest) {
        var pageInfo = agentMemoryView.findAll(pageRequest);
        return pageInfo.convert(entity -> {
            PageAgentMemoryResponse response = copy.toPageResponse(entity);

            // 填充步骤数量
            List<AgentMemoryStep> steps = agentMemoryStepView.findAllByMemoryId(entity.getId());
            response.setStepCount(steps.size());

            // 填充参数数量：从 paramsDefinition JSON 解析
            response.setParamCount(calcParamCount(entity.getParamsDefinition()));

            return response;
        });
    }

    @Override
    public InfoAgentMemoryResponse findById(String id) {
        AgentMemory memory = agentMemoryView.findById(id);
        AssertUtils.notEmpty(memory, "主键为[{}]的数据为空", id);

        InfoAgentMemoryResponse response = copy.toInfoResponse(memory);

        // 填充父记忆名称
        if (memory.getParentMemoryId() != null && !memory.getParentMemoryId().isBlank()) {
            AgentMemory parentMemory = agentMemoryView.findById(memory.getParentMemoryId());
            response.setParentMemoryName(parentMemory != null ? parentMemory.getMemoryName() : "");
        }

        // 加载记忆步骤列表
        List<AgentMemoryStep> steps = agentMemoryStepView.findAllByMemoryId(id);
        response.setSteps(steps);

        return response;
    }

    @Override
    public String save(CreateAgentMemoryRequest createRequest) {
        AgentMemory entity = copy.toEntity(createRequest);
        entity.setVersionNo(1);
        entity.setVersionStatus(1);
        entity.setCreateReason("MANUAL");
        entity.setStatus(Status.ON);
        entity.setReserve("");
        agentMemoryView.save(entity);
        return entity.getId();
    }

    @Override
    public String updateById(UpdateAgentMemoryRequest updateRequest) {
        AgentMemory existing = agentMemoryView.findById(updateRequest.getId());
        AssertUtils.notEmpty(existing, "主键[{}]的数据不存在", updateRequest.getId());

        AgentMemory entity = copy.toEntity(updateRequest);
        agentMemoryView.updateById(entity);
        return entity.getId();
    }

    @Override
    public void deleteByIds(List<String> ids) {

        // 校验版本状态：PUBLISHED 状态的记忆不允许删除，防止关联任务悬空引用
        for (String id : ids) {
            AgentMemory memory = agentMemoryView.findById(id);
            if (memory != null && Integer.valueOf(2).equals(memory.getVersionStatus())) {
                AssertUtils.error("记忆[{}]处于已发布状态，请先退役后再删除", id);
            }
        }

        // 处理子代版本的 parentMemoryId 悬空引用：将子代的 parentMemoryId 置为空字符串
        // 避免删除后子代记忆的 parentMemoryId 指向不存在的记录
        // 注意：必须使用空字符串而非null，因为MyBatis-Plus默认字段策略NOT_NULL会忽略null值
        for (String id : ids) {
            FindAllAgentMemoryRequest childRequest = new FindAllAgentMemoryRequest();
            childRequest.setParentMemoryId(id);
            List<AgentMemory> children = agentMemoryView.findAll(childRequest);
            for (AgentMemory child : children) {
                child.setParentMemoryId("");
                agentMemoryView.updateById(child);
                log.info("子代记忆parentMemoryId已置空：childMemoryId={}, 原parentMemoryId={}", child.getId(), id);
            }
        }

        // 删除记忆步骤
        for (String id : ids) {
            agentMemoryStepView.deleteByMemoryId(id);
        }

        // 删除记忆主表
        agentMemoryView.deleteByIds(ids);
    }

    @Override
    public void publish(String id) {
        AgentMemory memory = agentMemoryView.findById(id);
        AssertUtils.notEmpty(memory, "主键为[{}]的数据为空", id);

        // 仅 DRAFT 状态可发布
        AssertUtils.isTrue(Integer.valueOf(1).equals(memory.getVersionStatus()), "记忆[{}]不是草稿状态，无法发布", id);

        memory.setVersionStatus(2);
        agentMemoryView.updateById(memory);

        log.info("记忆发布成功：memoryId={}, memoryName={}", id, memory.getMemoryName());
    }

    @Override
    public void retire(String id) {
        AgentMemory memory = agentMemoryView.findById(id);
        AssertUtils.notEmpty(memory, "主键为[{}]的数据为空", id);

        // 仅 PUBLISHED 状态可退役
        AssertUtils.isTrue(Integer.valueOf(2).equals(memory.getVersionStatus()), "记忆[{}]不是已发布状态，无法退役", id);

        memory.setVersionStatus(3);
        agentMemoryView.updateById(memory);

        log.info("记忆退役成功：memoryId={}, memoryName={}", id, memory.getMemoryName());
    }

    @Override
    public ParamsDefinitionResponse getParamsDefinition(String id) {
        AgentMemory memory = agentMemoryView.findById(id);
        AssertUtils.notEmpty(memory, "主键为[{}]的数据为空", id);

        // 仅已发布状态的记忆可获取参数定义
        AssertUtils.isTrue(Integer.valueOf(2).equals(memory.getVersionStatus()), "记忆[{}]不是已发布状态，无法获取参数定义", id);

        ParamsDefinitionResponse response = new ParamsDefinitionResponse();
        response.setMemoryId(memory.getId());
        response.setMemoryName(memory.getMemoryName());
        response.setParamsDefinition(memory.getParamsDefinition());

        // 加载步骤列表供前端展示
        List<AgentMemoryStep> steps = agentMemoryStepView.findAllByMemoryId(id);
        response.setSteps(steps);

        return response;
    }

    @Override
    public ExecuteMemoryResponse execute(String id, ExecuteMemoryRequest request) {
        AgentMemory memory = agentMemoryView.findById(id);
        AssertUtils.notEmpty(memory, "主键为[{}]的数据为空", id);

        // 仅已发布状态的记忆可执行
        AssertUtils.isTrue(Integer.valueOf(2).equals(memory.getVersionStatus()), "记忆[{}]不是已发布状态，无法执行", id);

        // 参数校验：检查必填参数和占位符完整性
        validateParams(memory, request.getParams());

        // clientId 兜底：不传则使用记忆绑定的客户端
        String clientId = request.getClientId();
        if (clientId == null || clientId.isBlank()) {
            clientId = memory.getClientId();
        }

        // userId 兜底：不传则使用记忆绑定的用户
        String userId = request.getUserId();
        if (userId == null || userId.isBlank()) {
            userId = memory.getUserId();
        }

        // 防御性校验：clientId 和 userId 至少有一个来源，否则原子命令无法路由到客户端
        AssertUtils.isTrue(clientId != null && !clientId.isBlank(), "记忆[{}]未绑定客户端且请求未指定客户端，无法执行", id);

        // 构造命令调度请求（先于Task创建，确保Task.requestParams存储完整调度请求）
        CommandDispatchRequest dispatchRequest = new CommandDispatchRequest();
        dispatchRequest.setAgentId(memory.getAgentId());
        dispatchRequest.setCommandContent(memory.getMemoryName());
        dispatchRequest.setCommandName(memory.getMemoryName());
        dispatchRequest.setClientId(clientId);
        dispatchRequest.setRequestParams(request.getParams());
        dispatchRequest.setSessionId(request.getSessionId() != null ? request.getSessionId() : "");
        dispatchRequest.setUserId(userId);

        // 创建任务主记录，requestParams存储完整CommandDispatchRequest，与路径B保持一致
        Task task = new Task();
        task.setMemoryId(id);
        task.setMemoryVersionNo(memory.getVersionNo());
        task.setAgentId(memory.getAgentId());
        task.setTaskName(memory.getMemoryName());
        task.setParentTaskId("");
        task.setNextTaskId("");
        task.setStepType(AgentStepTypeProcess.ATOMIC_COMMAND);
        task.setBranchCondition("");
        task.setBranchRoute("");
        task.setRequestParams(JsonUtils.toJsonStr(dispatchRequest));
        task.setReturnParams("");
        task.setExecStatus(AgentExecutionStatusProcess.RUNNING);
        task.setFailureReason("");
        task.setStatus(Status.ON);
        task.setReserve("");
        task.setRemark("记忆直执行任务");
        taskView.save(task);

        // 委托 MemoryExecutor 按记忆步骤直接执行
        MemoryExecutor.MemoryExecutionResult execResult = memoryExecutor.execute(task, dispatchRequest, id, progressEvent -> {
        });

        // 构造执行响应
        ExecuteMemoryResponse response = new ExecuteMemoryResponse();
        response.setTaskId(task.getId());
        response.setMemoryId(id);
        response.setMemoryVersionNo(memory.getVersionNo());

        // 使用结构化结果的成功标志判断
        if (execResult.isSuccess()) {
            task.setExecStatus(AgentExecutionStatusProcess.SUCCESS);
            task.setReturnParams(execResult.getDetail());
        } else {
            task.setExecStatus(AgentExecutionStatusProcess.FAILED);
            task.setFailureReason(execResult.getDetail() != null ? execResult.getDetail() : "记忆执行失败");
        }
        taskView.updateById(task);

        // 记忆执行失败时触发修订，蒸馏新版本记忆
        // 先持久化任务状态再触发修订，确保蒸馏器读取到最新的任务状态
        // 使用事务提交后回调，确保 REQUIRES_NEW 新事务能读到已提交的 Task + TaskDetail
        if (!execResult.isSuccess()) {
            String revisionTaskId = task.getId();
            String revisionMemoryId = id;
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    self.triggerMemoryRevision(revisionTaskId, revisionMemoryId);
                }
            });
        }

        response.setExecStatus(task.getExecStatus().name());
        return response;
    }

    /**
     * 校验执行参数的完整性、类型和占位符匹配。
     * <p>params_definition 格式为 JSON 对象，key 为参数名，value 为属性定义：
     * {"contact_name": {"type": "string", "description": "...", "required": true}}</p>
     *
     * @param memory 记忆对象
     * @param params 用户输入参数
     */
    private void validateParams(AgentMemory memory, Map<String, Object> params) {

        // 无参数定义的记忆无需校验
        String paramsDefinition = memory.getParamsDefinition();
        if (paramsDefinition == null || paramsDefinition.isBlank()) {
            return;
        }

        // 解析参数定义为 Map
        Map<String, Map<String, Object>> paramDefs = parseParamsDefinition(paramsDefinition);
        if (paramDefs == null || paramDefs.isEmpty()) {
            return;
        }

        // 遍历参数定义，校验必填参数和类型
        for (Map.Entry<String, Map<String, Object>> entry : paramDefs.entrySet()) {
            String paramName = entry.getKey();
            Map<String, Object> attrDef = entry.getValue();
            Boolean required = attrDef != null ? (Boolean) attrDef.get("required") : null;

            // 必填参数校验
            if (Boolean.TRUE.equals(required)) {
                AssertUtils.isTrue(params != null && params.containsKey(paramName), "必填参数[{}]未提供", paramName);
                AssertUtils.isTrue(params.get(paramName) != null, "必填参数[{}]的值不能为空", paramName);
            }

            // 类型校验：当前仅支持 string 和 number 两种类型
            if (params != null && params.containsKey(paramName) && params.get(paramName) != null && attrDef != null) {
                String type = attrDef.get("type") != null ? attrDef.get("type").toString() : "string";
                Object value = params.get(paramName);

                // number 类型校验：值必须为数字或可解析为数字的字符串
                if ("number".equalsIgnoreCase(type)) {
                    boolean isNumeric = value instanceof Number || (value instanceof String && ((String) value).matches("-?\\d+(\\.\\d+)?"));
                    AssertUtils.isTrue(isNumeric, "参数[{}]的类型应为number，实际值为[{}]", paramName, value);
                }
            }
        }

        // 校验占位符完整性：检查步骤模板中的占位符是否都有对应参数
        List<AgentMemoryStep> steps = agentMemoryStepView.findAllByMemoryId(memory.getId());
        for (AgentMemoryStep step : steps) {
            String template = step.getArgsTemplate();
            if (template == null || template.isBlank()) {
                continue;
            }

            // 提取模板中的占位符 {xxx}
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\{(\\w+)}").matcher(template);
            while (matcher.find()) {
                String placeholder = matcher.group(1);
                AssertUtils.isTrue(params != null && params.containsKey(placeholder), "步骤[{}]的模板占位符{{{}}}未提供对应参数", step.getStepName(), placeholder);
            }
        }
    }

    /**
     * 计算参数定义中的参数数量。
     *
     * @param paramsDefinition 参数定义JSON
     * @return 参数数量
     */
    private Integer calcParamCount(String paramsDefinition) {

        // 无参数定义时返回0
        if (paramsDefinition == null || paramsDefinition.isBlank()) {
            return 0;
        }

        // 解析参数定义 Map，取 key 的数量
        Map<String, Map<String, Object>> paramDefs = parseParamsDefinition(paramsDefinition);
        if (paramDefs == null) {
            return 0;
        }

        return paramDefs.size();
    }

    /**
     * 解析参数定义JSON为Map。
     * <p>params_definition 格式为 JSON 对象，key 为参数名，value 为属性定义：
     * {"contact_name": {"type": "string", "description": "...", "required": true}}</p>
     *
     * @param paramsDefinition 参数定义JSON字符串
     * @return 参数名到属性定义的映射
     */
    private Map<String, Map<String, Object>> parseParamsDefinition(String paramsDefinition) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(paramsDefinition, new TypeReference<Map<String, Map<String, Object>>>() {
            });
        } catch (Exception e) {
            log.warn("参数定义JSON解析失败：{}", paramsDefinition, e);
            return null;
        }
    }

    @Override
    public List<MemoryVersionHistoryResponse> findVersionHistory(String id) {
        AgentMemory current = agentMemoryView.findById(id);
        AssertUtils.notEmpty(current, "主键为[{}]的数据为空", id);

        // 先沿 parentMemoryId 链路向上追溯到根祖先，确保从整棵版本树的根开始向下遍历
        // 这样可以收集完整的版本链，包括并发修订产生的分叉
        AgentMemory root = current;
        int maxDepth = 50;
        while (root.getParentMemoryId() != null && !root.getParentMemoryId().isBlank() && maxDepth-- > 0) {
            AgentMemory parent = agentMemoryView.findById(root.getParentMemoryId());
            if (parent == null) {
                break;
            }
            root = parent;
        }

        // 从根祖先开始向下遍历整棵版本树，收集所有版本
        List<MemoryVersionHistoryResponse> history = new ArrayList<>();
        Set<String> visited = new java.util.HashSet<>();

        // 使用队列按广度优先遍历，确保版本树层级清晰
        List<String> toVisit = new ArrayList<>();
        toVisit.add(root.getId());
        visited.add(root.getId());

        while (!toVisit.isEmpty()) {
            String currentId = toVisit.remove(0);
            AgentMemory node = currentId.equals(root.getId()) ? root : agentMemoryView.findById(currentId);
            if (node == null) {
                continue;
            }
            history.add(toVersionHistory(node));

            // 查询 parentMemoryId 指向当前节点的子版本
            FindAllAgentMemoryRequest childRequest = new FindAllAgentMemoryRequest();
            childRequest.setParentMemoryId(currentId);
            List<AgentMemory> children = agentMemoryView.findAll(childRequest);

            for (AgentMemory child : children) {
                // 跳过已访问的记忆，防止环路引用
                if (visited.contains(child.getId())) {
                    log.warn("版本链路检测到环路引用，跳过：memoryId={}", child.getId());
                    continue;
                }
                visited.add(child.getId());
                toVisit.add(child.getId());
            }
        }

        // 按版本号降序排列
        history.sort((a, b) -> Integer.compare(b.getVersionNo() != null ? b.getVersionNo() : 0, a.getVersionNo() != null ? a.getVersionNo() : 0));

        return history;
    }

    /**
     * 将记忆实体转换为版本历史响应。
     *
     * @param memory 记忆实体
     * @return 版本历史响应
     */
    private MemoryVersionHistoryResponse toVersionHistory(AgentMemory memory) {
        MemoryVersionHistoryResponse response = new MemoryVersionHistoryResponse();
        response.setId(memory.getId());
        response.setVersionNo(memory.getVersionNo());
        response.setVersionStatus(memory.getVersionStatus());
        response.setParentMemoryId(memory.getParentMemoryId());
        response.setCreateReason(memory.getCreateReason());
        response.setSummary(memory.getSummary());
        response.setCreateTime(memory.getCreateTime());
        return response;
    }

    /**
     * 触发记忆修订。
     * <p>记忆执行失败后，由 MemoryDistiller 重新蒸馏任务执行轨迹，
     * 生成新版本记忆（createReason=MEMORY_REVISE，versionNo递增）。
     * 修订失败不影响主流程，仅记录警告日志。</p>
     * <p>使用 REQUIRES_NEW 传播级别在独立事务中执行蒸馏，
     * 避免内层事务标记 rollback-only 后导致外层事务提交时抛出 UnexpectedRollbackException。</p>
     * <p>通过 afterCommit 回调调用，确保外层事务已提交，
     * REQUIRES_NEW 新事务能读到已持久化的 Task + TaskDetail。</p>
     *
     * @param taskId   失败任务的ID
     * @param memoryId 失败的记忆ID
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public void triggerMemoryRevision(String taskId, String memoryId) {
        try {
            log.info("触发记忆修订：taskId={}, memoryId={}", taskId, memoryId);
            memoryDistiller.distill(taskId);
        } catch (RuntimeException e) {
            log.warn("记忆修订失败，taskId={}, memoryId={}", taskId, memoryId, e);
        }
    }
}