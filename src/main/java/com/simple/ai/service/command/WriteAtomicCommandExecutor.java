package com.simple.ai.service.command;

import com.simple.ai.common.dto.agentDefinition.CreateAgentDefinitionRequest;
import com.simple.ai.common.dto.agentMemory.CreateAgentMemoryRequest;
import com.simple.ai.common.dto.agentRule.CreateAgentRuleRequest;
import com.simple.ai.common.dto.agentSkill.CreateAgentSkillRequest;
import com.simple.ai.common.dto.command.AtomicCommandInvokeRequest;
import com.simple.ai.common.dto.command.AtomicCommandInvokeResponse;
import com.simple.ai.common.service.agentDefinition.AgentDefinitionService;
import com.simple.ai.common.service.agentMemory.AgentMemoryService;
import com.simple.ai.common.service.agentRule.AgentRuleService;
import com.simple.ai.common.service.agentSkill.AgentSkillService;
import com.simple.ai.common.service.command.AtomicCommandExecutor;
import com.simple.common.core.utils.AssertUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.simple.common.core.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.Map;

/**
 * 写入类原子命令执行器。
 *
 * <p>支持白名单内的本地写入操作：创建记忆、规则、技能、智能体定义。
 * 非白名单写入命令仍按安全策略阻断。</p>
 *
 * @author qty
 */
@Component
public class WriteAtomicCommandExecutor implements AtomicCommandExecutor {

    private static final Logger log = LoggerFactory.getLogger(WriteAtomicCommandExecutor.class);

    /**
     * 写入角色标识
     */
    private static final String WRITE_ROLE = "WRITE";

    /**
     * 保存角色标识
     */
    private static final String SAVE_ROLE = "SAVE";

    /**
     * 更新角色标识
     */
    private static final String UPDATE_ROLE = "UPDATE";

    /**
     * 删除角色标识
     */
    private static final String DELETE_ROLE = "DELETE";

    /**
     * 写入中文标识
     */
    private static final String WRITE_NAME = "写入";

    /**
     * 创建记忆关键字
     */
    private static final String CREATE_MEMORY_KEY = "创建记忆";

    /**
     * 创建规则关键字
     */
    private static final String CREATE_RULE_KEY = "创建规则";

    /**
     * 创建技能关键字
     */
    private static final String CREATE_SKILL_KEY = "创建技能";

    /**
     * 创建智能体关键字
     */
    private static final String CREATE_AGENT_KEY = "创建智能体";

    @Autowired
    private AgentMemoryService agentMemoryService;

    @Autowired
    private AgentRuleService agentRuleService;

    @Autowired
    private AgentSkillService agentSkillService;

    @Autowired
    private AgentDefinitionService agentDefinitionService;

    @Override
    public boolean supports(AtomicCommandInvokeRequest request) {

        // 命令作用命中写入类时使用当前执行器
        if (isWriteText(request.getAtomicCommandRole())) {
            return true;
        }
        return isWriteText(request.getCommandContent());
    }

    @Override
    public AtomicCommandInvokeResponse execute(AtomicCommandInvokeRequest request) {

        // 参数校验：任务ID不能为空
        AssertUtils.notEmpty(request.getTaskId(), "任务ID不能为空");

        // 参数校验：命令内容不能为空
        AssertUtils.notEmpty(request.getCommandContent(), "命令内容不能为空");

        // 尝试从命令内容中解析写入参数
        Map<String, Object> writeParams = parseWriteParams(request.getCommandContent());

        // 无法解析写入参数时按安全策略阻断
        if (writeParams == null || writeParams.isEmpty()) {
            return buildBlockedWriteResponse(request, "无法解析写入命令参数，命令内容需为JSON格式");
        }

        // 根据写入类型分发到对应创建方法
        String writeType = (String) writeParams.getOrDefault("type", "");
        return dispatchWrite(request, writeType, writeParams);
    }

    /**
     * 根据写入类型分发执行。
     *
     * @param request 原子命令调用请求
     * @param writeType 写入类型
     * @param writeParams 写入参数
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse dispatchWrite(AtomicCommandInvokeRequest request, String writeType,
                                                       Map<String, Object> writeParams) {
        try {
            if (CREATE_MEMORY_KEY.equals(writeType)) {
                return handleCreateMemory(request, writeParams);
            }
            if (CREATE_RULE_KEY.equals(writeType)) {
                return handleCreateRule(request, writeParams);
            }
            if (CREATE_SKILL_KEY.equals(writeType)) {
                return handleCreateSkill(request, writeParams);
            }
            if (CREATE_AGENT_KEY.equals(writeType)) {
                return handleCreateAgent(request, writeParams);
            }

            // type 不在白名单内则阻断
            return buildBlockedWriteResponse(request, "写入类型[" + writeType + "]不在白名单内");
        } catch (RuntimeException e) {
            log.error("执行写入命令失败 [taskId={}]", request.getTaskId(), e);
            return buildBlockedWriteResponse(request, "写入执行失败：" + e.getMessage());
        }
    }

    /**
     * 处理创建记忆。
     *
     * @param request 原子命令调用请求
     * @param writeParams 写入参数
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse handleCreateMemory(AtomicCommandInvokeRequest request,
                                                            Map<String, Object> writeParams) {
        CreateAgentMemoryRequest createRequest = new CreateAgentMemoryRequest();
        createRequest.setAgentId((String) writeParams.get("agentId"));
        createRequest.setMemoryName((String) writeParams.get("memoryName"));
        createRequest.setStepName((String) writeParams.getOrDefault("stepName", ""));
        createRequest.setTriggerCondition((String) writeParams.get("triggerCondition"));
        createRequest.setTriggerAction((String) writeParams.get("triggerAction"));
        createRequest.setRemark((String) writeParams.getOrDefault("remark", "AI对话创建"));

        String memoryId = agentMemoryService.save(createRequest);
        return buildSuccessResponse(request, "记忆创建成功", memoryId);
    }

    /**
     * 处理创建规则。
     *
     * @param request 原子命令调用请求
     * @param writeParams 写入参数
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse handleCreateRule(AtomicCommandInvokeRequest request,
                                                          Map<String, Object> writeParams) {
        CreateAgentRuleRequest createRequest = new CreateAgentRuleRequest();
        createRequest.setAgentId((String) writeParams.get("agentId"));
        createRequest.setDefinitionDesc((String) writeParams.get("definitionDesc"));
        createRequest.setTriggerCondition((String) writeParams.get("triggerCondition"));
        createRequest.setTriggerAction((String) writeParams.get("triggerAction"));
        createRequest.setRemark((String) writeParams.getOrDefault("remark", "AI对话创建"));

        String ruleId = agentRuleService.save(createRequest);
        return buildSuccessResponse(request, "规则创建成功", ruleId);
    }

    /**
     * 处理创建技能。
     *
     * @param request 原子命令调用请求
     * @param writeParams 写入参数
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse handleCreateSkill(AtomicCommandInvokeRequest request,
                                                           Map<String, Object> writeParams) {
        CreateAgentSkillRequest createRequest = new CreateAgentSkillRequest();
        createRequest.setAgentId((String) writeParams.get("agentId"));
        createRequest.setDefinitionDesc((String) writeParams.get("definitionDesc"));
        createRequest.setExecContent((String) writeParams.get("execContent"));
        createRequest.setReturnDataFormat((String) writeParams.get("returnDataFormat"));
        createRequest.setRemark((String) writeParams.getOrDefault("remark", "AI对话创建"));

        String skillId = agentSkillService.save(createRequest);
        return buildSuccessResponse(request, "技能创建成功", skillId);
    }

    /**
     * 处理创建智能体定义。
     *
     * @param request 原子命令调用请求
     * @param writeParams 写入参数
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse handleCreateAgent(AtomicCommandInvokeRequest request,
                                                           Map<String, Object> writeParams) {
        CreateAgentDefinitionRequest createRequest = new CreateAgentDefinitionRequest();
        createRequest.setName((String) writeParams.get("name"));
        createRequest.setDefinitionDesc((String) writeParams.get("definitionDesc"));
        createRequest.setFirstPrinciple((String) writeParams.getOrDefault("firstPrinciple", ""));
        createRequest.setSecondRule((String) writeParams.getOrDefault("secondRule", ""));
        createRequest.setThirdSkill((String) writeParams.getOrDefault("thirdSkill", ""));
        createRequest.setDefaultModelId((String) writeParams.getOrDefault("defaultModelId", null));
        createRequest.setRemark((String) writeParams.getOrDefault("remark", "AI对话创建"));

        String agentId = agentDefinitionService.save(createRequest);
        return buildSuccessResponse(request, "智能体创建成功", agentId);
    }

    /**
     * 从命令内容解析写入参数。
     *
     * @param commandContent 命令内容
     * @return 写入参数Map，解析失败返回null
     */
    private Map<String, Object> parseWriteParams(String commandContent) {
        try {

            // 尝试按JSON解析命令内容
            ObjectMapper objectMapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> result = objectMapper.readValue(commandContent, Map.class);
            return result;
        } catch (Exception e) {
            log.debug("命令内容非JSON格式，无法解析写入参数：{}", commandContent);
            return null;
        }
    }

    /**
     * 判断文本是否为写入类命令。
     *
     * @param text 命令文本
     * @return 是否写入类命令
     */
    private boolean isWriteText(String text) {

        // 文本为空时不匹配写入类执行器
        if (text == null || text.isBlank()) {
            return false;
        }
        String upperText = text.toUpperCase(Locale.ROOT);
        return upperText.contains(WRITE_ROLE)
                || upperText.contains(SAVE_ROLE)
                || upperText.contains(UPDATE_ROLE)
                || upperText.contains(DELETE_ROLE)
                || text.contains(WRITE_NAME);
    }

    /**
     * 构建写入成功响应。
     *
     * @param request 原子命令调用请求
     * @param message 成功消息
     * @param entityId 创建的实体ID
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse buildSuccessResponse(AtomicCommandInvokeRequest request, String message,
                                                              String entityId) {
        AtomicCommandInvokeResponse response = new AtomicCommandInvokeResponse();
        response.setSuccess(Boolean.TRUE);
        response.setResponseContent(message + "，ID：" + entityId);
        response.setFailureReason("");
        return response;
    }

    /**
     * 构建写入类命令阻断响应。
     *
     * @param request 原子命令调用请求
     * @param reason 阻断原因
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse buildBlockedWriteResponse(AtomicCommandInvokeRequest request, String reason) {
        AtomicCommandInvokeResponse response = new AtomicCommandInvokeResponse();
        response.setSuccess(Boolean.FALSE);
        response.setResponseContent(JsonUtils.toJsonStr(request));
        response.setFailureReason(reason);
        return response;
    }
}
