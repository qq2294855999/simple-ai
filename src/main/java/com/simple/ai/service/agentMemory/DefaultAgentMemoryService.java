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
import com.simple.ai.common.service.memory.MemoryExecutor;
import com.simple.ai.common.view.agentMemory.AgentMemoryView;
import com.simple.ai.common.view.agentMemoryStep.AgentMemoryStepView;
import com.simple.ai.common.view.task.TaskView;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.core.utils.JsonUtils;
import com.simple.common.mp.common.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * 任务视图
     */
    @Autowired
    private TaskView taskView;

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

        // 加载记忆步骤列表
        List<AgentMemoryStep> steps = agentMemoryStepView.findAllByMemoryId(id);
        response.setSteps(steps);

        return response;
    }

    @Override
    public String save(CreateAgentMemoryRequest createRequest) {
        AgentMemory entity = copy.toEntity(createRequest);
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

        // 删除记忆步骤
        for (String id : ids) {
            agentMemoryStepView.deleteByMemoryId(id);
        }

        // 删除记忆主表
        agentMemoryView.deleteByIds(ids);
    }

    @Override
    public ParamsDefinitionResponse getParamsDefinition(String id) {
        AgentMemory memory = agentMemoryView.findById(id);
        AssertUtils.notEmpty(memory, "主键为[{}]的数据为空", id);

        // 仅启用状态的记忆可获取参数定义
        AssertUtils.isTrue(Status.ON.equals(memory.getStatus()), "记忆[{}]未启用，无法获取参数定义", id);

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

        // 仅启用状态的记忆可执行
        AssertUtils.isTrue(Status.ON.equals(memory.getStatus()), "记忆[{}]未启用，无法执行", id);

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

        // 使用结构化结果的成功标志判断
        if (execResult.isSuccess()) {
            task.setExecStatus(AgentExecutionStatusProcess.SUCCESS);
            task.setReturnParams(execResult.getDetail());
        } else {
            task.setExecStatus(AgentExecutionStatusProcess.FAILED);
            task.setFailureReason(execResult.getDetail() != null ? execResult.getDetail() : "记忆执行失败");
        }
        taskView.updateById(task);

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
            Matcher matcher = Pattern.compile("\\{(\\w+)}").matcher(template);
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
}