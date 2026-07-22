package com.simple.ai.service.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simple.ai.common.dto.agentClient.InfoAgentClientResponse;
import com.simple.ai.common.dto.agentDefinition.InfoAgentDefinitionResponse;
import com.simple.ai.common.dto.agentExecutor.InfoAgentExecutorResponse;
import com.simple.ai.common.dto.agentMemory.InfoAgentMemoryResponse;
import com.simple.ai.common.dto.agentRule.InfoAgentRuleResponse;
import com.simple.ai.common.dto.agentSkill.InfoAgentSkillResponse;
import com.simple.ai.common.dto.atomicCommand.InfoAtomicCommandResponse;
import com.simple.ai.common.dto.command.AtomicCommandInvokeRequest;
import com.simple.ai.common.dto.command.AtomicCommandInvokeResponse;
import com.simple.ai.common.service.agentClient.AgentClientService;
import com.simple.ai.common.service.agentDefinition.AgentDefinitionService;
import com.simple.ai.common.service.agentExecutor.AgentExecutorService;
import com.simple.ai.common.service.agentMemory.AgentMemoryService;
import com.simple.ai.common.service.agentRule.AgentRuleService;
import com.simple.ai.common.service.agentSkill.AgentSkillService;
import com.simple.ai.common.service.atomicCommand.AtomicCommandService;
import com.simple.ai.common.service.command.AtomicCommandExecutor;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.core.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

/**
 * 只读信息原子命令执行器。
 *
 * <p>支持白名单内的查询操作：查询记忆、规则、技能、智能体定义、原子命令、执行器、客户端。
 * 非白名单查询命令返回失败。</p>
 *
 * @author qty
 */
@Component
public class ReadOnlyInfoAtomicCommandExecutor implements AtomicCommandExecutor {

    private static final Logger log = LoggerFactory.getLogger(ReadOnlyInfoAtomicCommandExecutor.class);

    /**
     * 只读角色标识
     */
    private static final String READ_ROLE = "READ";

    /**
     * 查询角色标识
     */
    private static final String QUERY_ROLE = "QUERY";

    /**
     * 信息角色标识
     */
    private static final String INFO_ROLE = "INFO";

    /**
     * 查询记忆关键字
     */
    private static final String QUERY_MEMORY_KEY = "查询记忆";

    /**
     * 查询规则关键字
     */
    private static final String QUERY_RULE_KEY = "查询规则";

    /**
     * 查询技能关键字
     */
    private static final String QUERY_SKILL_KEY = "查询技能";

    /**
     * 查询智能体关键字
     */
    private static final String QUERY_AGENT_KEY = "查询智能体";

    /**
     * 查询原子命令关键字
     */
    private static final String QUERY_COMMAND_KEY = "查询原子命令";

    /**
     * 查询执行器关键字
     */
    private static final String QUERY_EXECUTOR_KEY = "查询执行器";

    /**
     * 查询客户端关键字
     */
    private static final String QUERY_CLIENT_KEY = "查询客户端";

    @Autowired
    private AgentMemoryService agentMemoryService;

    @Autowired
    private AgentRuleService agentRuleService;

    @Autowired
    private AgentSkillService agentSkillService;

    @Autowired
    private AgentDefinitionService agentDefinitionService;

    @Autowired
    private AtomicCommandService atomicCommandService;

    @Autowired
    private AgentExecutorService agentExecutorService;

    @Autowired
    private AgentClientService agentClientService;

    @Override
    public boolean supports(AtomicCommandInvokeRequest request) {

        // 命令作用命中只读信息类时使用当前执行器
        if (isReadOnlyRole(request.getAtomicCommandRole())) {
            return true;
        }
        return isReadOnlyCommand(request.getCommandContent());
    }

    @Override
    public AtomicCommandInvokeResponse execute(AtomicCommandInvokeRequest request) {

        // 参数校验：任务ID不能为空
        AssertUtils.notEmpty(request.getTaskId(), "任务ID不能为空");

        // 参数校验：命令内容不能为空
        AssertUtils.notEmpty(request.getCommandContent(), "命令内容不能为空");

        // 尝试从命令内容中解析查询参数
        Map<String, Object> readParams = parseReadParams(request.getCommandContent());

        // 无法解析查询参数时返回失败
        if (readParams == null || readParams.isEmpty()) {
            return buildReadOnlyResponse(request, "无法解析查询命令参数，命令内容需为JSON格式");
        }

        // 根据查询类型分发到对应查询方法
        String readType = (String) readParams.getOrDefault("type", "");
        return dispatchRead(request, readType, readParams);
    }

    /**
     * 根据查询类型分发执行。
     *
     * @param request    原子命令调用请求
     * @param readType   查询类型
     * @param readParams 查询参数
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse dispatchRead(AtomicCommandInvokeRequest request, String readType, Map<String, Object> readParams) {
        try {
            if (QUERY_MEMORY_KEY.equals(readType)) {
                return handleQueryMemory(request, readParams);
            }
            if (QUERY_RULE_KEY.equals(readType)) {
                return handleQueryRule(request, readParams);
            }
            if (QUERY_SKILL_KEY.equals(readType)) {
                return handleQuerySkill(request, readParams);
            }
            if (QUERY_AGENT_KEY.equals(readType)) {
                return handleQueryAgent(request, readParams);
            }
            if (QUERY_COMMAND_KEY.equals(readType)) {
                return handleQueryCommand(request, readParams);
            }
            if (QUERY_EXECUTOR_KEY.equals(readType)) {
                return handleQueryExecutor(request, readParams);
            }
            if (QUERY_CLIENT_KEY.equals(readType)) {
                return handleQueryClient(request, readParams);
            }

            // type 不在白名单内则返回失败
            return buildReadOnlyResponse(request, "查询类型[" + readType + "]不在白名单内");
        } catch (RuntimeException e) {
            log.error("执行查询命令失败 [taskId={}]", request.getTaskId(), e);
            return buildReadOnlyResponse(request, "查询执行失败：" + e.getMessage());
        }
    }

    /**
     * 处理查询记忆。
     *
     * @param request    原子命令调用请求
     * @param readParams 查询参数
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse handleQueryMemory(AtomicCommandInvokeRequest request, Map<String, Object> readParams) {
        String id = (String) readParams.get("id");

        // 按 id 查询单条记忆
        if (id != null && !id.isBlank()) {
            InfoAgentMemoryResponse info = agentMemoryService.findById(id);
            return buildReadSuccessResponse(request, "记忆查询成功", JsonUtils.toJsonStr(info));
        }

        // 未提供 id 时返回提示
        return buildReadSuccessResponse(request, "查询记忆需要提供 id 参数", "");
    }

    /**
     * 处理查询规则。
     *
     * @param request    原子命令调用请求
     * @param readParams 查询参数
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse handleQueryRule(AtomicCommandInvokeRequest request, Map<String, Object> readParams) {
        String id = (String) readParams.get("id");

        // 按 id 查询单条规则
        if (id != null && !id.isBlank()) {
            InfoAgentRuleResponse info = agentRuleService.findById(id);
            return buildReadSuccessResponse(request, "规则查询成功", JsonUtils.toJsonStr(info));
        }

        // 未提供 id 时返回提示
        return buildReadSuccessResponse(request, "查询规则需要提供 id 参数", "");
    }

    /**
     * 处理查询技能。
     *
     * @param request    原子命令调用请求
     * @param readParams 查询参数
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse handleQuerySkill(AtomicCommandInvokeRequest request, Map<String, Object> readParams) {
        String id = (String) readParams.get("id");

        // 按 id 查询单条技能
        if (id != null && !id.isBlank()) {
            InfoAgentSkillResponse info = agentSkillService.findById(id);
            return buildReadSuccessResponse(request, "技能查询成功", JsonUtils.toJsonStr(info));
        }

        // 未提供 id 时返回提示
        return buildReadSuccessResponse(request, "查询技能需要提供 id 参数", "");
    }

    /**
     * 处理查询智能体定义。
     *
     * @param request    原子命令调用请求
     * @param readParams 查询参数
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse handleQueryAgent(AtomicCommandInvokeRequest request, Map<String, Object> readParams) {
        String id = (String) readParams.get("id");

        // 按 id 查询单条智能体定义
        if (id != null && !id.isBlank()) {
            InfoAgentDefinitionResponse info = agentDefinitionService.findById(id);
            return buildReadSuccessResponse(request, "智能体查询成功", JsonUtils.toJsonStr(info));
        }

        // 未提供 id 时返回提示
        return buildReadSuccessResponse(request, "查询智能体需要提供 id 参数", "");
    }

    /**
     * 处理查询原子命令。
     *
     * @param request    原子命令调用请求
     * @param readParams 查询参数
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse handleQueryCommand(AtomicCommandInvokeRequest request, Map<String, Object> readParams) {
        String id = (String) readParams.get("id");

        // 按 id 查询单条原子命令
        if (id != null && !id.isBlank()) {
            InfoAtomicCommandResponse info = atomicCommandService.findById(id);
            return buildReadSuccessResponse(request, "原子命令查询成功", JsonUtils.toJsonStr(info));
        }

        // 未提供 id 时返回提示
        return buildReadSuccessResponse(request, "查询原子命令需要提供 id 参数", "");
    }

    /**
     * 处理查询执行器。
     *
     * @param request    原子命令调用请求
     * @param readParams 查询参数
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse handleQueryExecutor(AtomicCommandInvokeRequest request, Map<String, Object> readParams) {
        String id = (String) readParams.get("id");

        // 按 id 查询单条执行器
        if (id != null && !id.isBlank()) {
            InfoAgentExecutorResponse info = agentExecutorService.findById(id);
            return buildReadSuccessResponse(request, "执行器查询成功", JsonUtils.toJsonStr(info));
        }

        // 未提供 id 时返回提示
        return buildReadSuccessResponse(request, "查询执行器需要提供 id 参数", "");
    }

    /**
     * 处理查询客户端。
     *
     * @param request    原子命令调用请求
     * @param readParams 查询参数
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse handleQueryClient(AtomicCommandInvokeRequest request, Map<String, Object> readParams) {
        String id = (String) readParams.get("id");

        // 按 id 查询单条客户端
        if (id != null && !id.isBlank()) {
            InfoAgentClientResponse info = agentClientService.findById(id);
            return buildReadSuccessResponse(request, "客户端查询成功", JsonUtils.toJsonStr(info));
        }

        // 未提供 id 时返回提示
        return buildReadSuccessResponse(request, "查询客户端需要提供 id 参数", "");
    }

    /**
     * 从命令内容解析查询参数。
     *
     * @param commandContent 命令内容
     * @return 查询参数Map，解析失败返回null
     */
    private Map<String, Object> parseReadParams(String commandContent) {
        try {

            // 尝试按JSON解析命令内容
            ObjectMapper objectMapper = new ObjectMapper();
            @SuppressWarnings("unchecked") Map<String, Object> result = objectMapper.readValue(commandContent, Map.class);
            return result;
        } catch (Exception e) {
            log.debug("命令内容非JSON格式，无法解析查询参数：{}", commandContent);
            return null;
        }
    }

    /**
     * 构建查询成功响应。
     *
     * @param request 原子命令调用请求
     * @param message 成功消息
     * @param data    查询结果数据
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse buildReadSuccessResponse(AtomicCommandInvokeRequest request, String message, String data) {
        AtomicCommandInvokeResponse response = new AtomicCommandInvokeResponse();
        response.setSuccess(Boolean.TRUE);
        response.setResponseContent(data);
        response.setFailureReason("");
        return response;
    }

    /**
     * 构建只读信息类失败响应。
     * <p>当查询类型不在白名单或解析失败时使用。</p>
     *
     * @param request 原子命令调用请求
     * @param reason  失败原因
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse buildReadOnlyResponse(AtomicCommandInvokeRequest request, String reason) {
        AtomicCommandInvokeResponse response = new AtomicCommandInvokeResponse();
        response.setSuccess(Boolean.FALSE);
        response.setResponseContent(JsonUtils.toJsonStr(request));
        response.setFailureReason(reason);
        return response;
    }

    /**
     * 判断命令作用是否为只读信息类。
     *
     * @param role 命令作用
     * @return 是否只读信息类
     */
    private boolean isReadOnlyRole(String role) {

        // 命令作用为空时交由命令内容继续判断
        if (role == null || role.isBlank()) {
            return false;
        }
        String upperRole = role.toUpperCase(Locale.ROOT);
        return upperRole.contains(READ_ROLE) || upperRole.contains(QUERY_ROLE) || upperRole.contains(INFO_ROLE);
    }

    /**
     * 判断命令内容是否为只读信息类。
     *
     * @param commandContent 命令内容
     * @return 是否只读信息类
     */
    private boolean isReadOnlyCommand(String commandContent) {

        // 命令内容为空时不匹配专用执行器
        if (commandContent == null || commandContent.isBlank()) {
            return false;
        }
        String upperCommandContent = commandContent.toUpperCase(Locale.ROOT);
        return upperCommandContent.contains(READ_ROLE) || upperCommandContent.contains(QUERY_ROLE) || upperCommandContent.contains(INFO_ROLE);
    }

}
