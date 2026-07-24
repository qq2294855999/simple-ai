package com.simple.ai.service.command;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simple.ai.common.dto.agentClient.CreateAgentClientRequest;
import com.simple.ai.common.dto.agentClient.InfoAgentClientResponse;
import com.simple.ai.common.dto.agentDefinition.CreateAgentDefinitionRequest;
import com.simple.ai.common.dto.agentDefinition.UpdateAgentDefinitionRequest;
import com.simple.ai.common.dto.agentExecutor.CreateAgentExecutorRequest;
import com.simple.ai.common.dto.agentRule.CreateAgentRuleRequest;
import com.simple.ai.common.dto.agentRule.UpdateAgentRuleRequest;
import com.simple.ai.common.dto.agentSkill.CreateAgentSkillRequest;
import com.simple.ai.common.dto.agentSkill.UpdateAgentSkillRequest;
import com.simple.ai.common.dto.atomicCommand.CreateAtomicCommandRequest;
import com.simple.ai.common.dto.atomicCommand.UpdateAtomicCommandRequest;
import com.simple.ai.common.dto.command.AtomicCommandInvokeRequest;
import com.simple.ai.common.dto.command.AtomicCommandInvokeResponse;
import com.simple.ai.common.service.agentClient.AgentClientService;
import com.simple.ai.common.service.agentDefinition.AgentDefinitionService;
import com.simple.ai.common.service.agentExecutor.AgentExecutorService;
import com.simple.ai.common.service.agentRule.AgentRuleService;
import com.simple.ai.common.service.agentSkill.AgentSkillService;
import com.simple.ai.common.service.atomicCommand.AtomicCommandService;
import com.simple.ai.common.service.command.AtomicCommandExecutor;
import com.simple.common.auth.client.util.LoginUserUtils;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.core.utils.JsonUtils;
import com.simple.common.mp.common.enums.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 写入类原子命令执行器。
 *
 * <p>支持白名单内的本地写入操作：创建/更新/删除 记忆、规则、技能、智能体定义、原子命令、执行器、客户端、记忆版本。
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

    /**
     * 创建原子命令关键字
     */
    private static final String CREATE_COMMAND_KEY = "创建原子命令";

    /**
     * 创建执行器关键字
     */
    private static final String CREATE_EXECUTOR_KEY = "创建执行器";

    /**
     * 创建客户端关键字
     */
    private static final String CREATE_CLIENT_KEY = "创建客户端";

    /**
     * 更新规则关键字
     */
    private static final String UPDATE_RULE_KEY = "更新规则";

    /**
     * 更新技能关键字
     */
    private static final String UPDATE_SKILL_KEY = "更新技能";

    /**
     * 更新智能体关键字
     */
    private static final String UPDATE_AGENT_KEY = "更新智能体";

    /**
     * 更新原子命令关键字
     */
    private static final String UPDATE_COMMAND_KEY = "更新原子命令";

    /**
     * 删除规则关键字
     */
    private static final String DELETE_RULE_KEY = "删除规则";

    /**
     * 删除技能关键字
     */
    private static final String DELETE_SKILL_KEY = "删除技能";

    /**
     * 删除智能体关键字
     */
    private static final String DELETE_AGENT_KEY = "删除智能体";

    /**
     * 删除原子命令关键字
     */
    private static final String DELETE_COMMAND_KEY = "删除原子命令";

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

        // 根据写入类型分发到对应方法
        String writeType = (String) writeParams.getOrDefault("type", "");

        // 根据角色决定分发到 CREATE / UPDATE / DELETE
        String role = request.getAtomicCommandRole();
        if (isUpdateRole(role)) {
            return dispatchUpdate(request, writeType, writeParams);
        }
        if (isDeleteRole(role)) {
            return dispatchDelete(request, writeType, writeParams);
        }
        return dispatchWrite(request, writeType, writeParams);
    }

    /**
     * 根据写入类型分发执行创建操作。
     *
     * @param request 原子命令调用请求
     * @param writeType 写入类型
     * @param writeParams 写入参数
     * @return 原子命令调用响应
     */
    private AtomicCommandInvokeResponse dispatchWrite(AtomicCommandInvokeRequest request, String writeType,
                                                       Map<String, Object> writeParams) {
        try {
            if (CREATE_RULE_KEY.equals(writeType)) {
                return handleCreateRule(request, writeParams);
            }
            if (CREATE_SKILL_KEY.equals(writeType)) {
                return handleCreateSkill(request, writeParams);
            }
            if (CREATE_AGENT_KEY.equals(writeType)) {
                return handleCreateAgent(request, writeParams);
            }
            if (CREATE_COMMAND_KEY.equals(writeType)) {
                return handleCreateAtomicCommand(request, writeParams);
            }
            if (CREATE_EXECUTOR_KEY.equals(writeType)) {
                return handleCreateExecutor(request, writeParams);
            }
            if (CREATE_CLIENT_KEY.equals(writeType)) {
                return handleCreateClient(request, writeParams);
            }

            // type 不在白名单内则阻断
            return buildBlockedWriteResponse(request, "写入类型[" + writeType + "]不在白名单内");
        } catch (RuntimeException e) {
            log.error("执行写入命令失败 [taskId={}]", request.getTaskId(), e);
            return buildBlockedWriteResponse(request, "写入执行失败：" + e.getMessage());
        }
    }

    /**
     * 根据更新类型分发执行更新操作。
     *
     * @param request     原子命令调用请求
     * @param writeType   更新类型
     * @param writeParams 更新参数
     * @return 原子命令调用响应
     * @author qty
     */
    private AtomicCommandInvokeResponse dispatchUpdate(AtomicCommandInvokeRequest request, String writeType, Map<String, Object> writeParams) {
        try {
            if (UPDATE_RULE_KEY.equals(writeType)) {
                return handleUpdateRule(request, writeParams);
            }
            if (UPDATE_SKILL_KEY.equals(writeType)) {
                return handleUpdateSkill(request, writeParams);
            }
            if (UPDATE_AGENT_KEY.equals(writeType)) {
                return handleUpdateAgent(request, writeParams);
            }
            if (UPDATE_COMMAND_KEY.equals(writeType)) {
                return handleUpdateAtomicCommand(request, writeParams);
            }

            // type 不在白名单内则阻断
            return buildBlockedWriteResponse(request, "更新类型[" + writeType + "]不在白名单内");
        } catch (RuntimeException e) {
            log.error("执行更新命令失败 [taskId={}]", request.getTaskId(), e);
            return buildBlockedWriteResponse(request, "更新执行失败：" + e.getMessage());
        }
    }

    /**
     * 根据删除类型分发执行删除操作。
     *
     * @param request     原子命令调用请求
     * @param writeType   删除类型
     * @param writeParams 删除参数
     * @return 原子命令调用响应
     * @author qty
     */
    private AtomicCommandInvokeResponse dispatchDelete(AtomicCommandInvokeRequest request, String writeType, Map<String, Object> writeParams) {
        try {
            if (DELETE_RULE_KEY.equals(writeType)) {
                return handleDeleteByIds(request, writeType, writeParams, agentRuleService);
            }
            if (DELETE_SKILL_KEY.equals(writeType)) {
                return handleDeleteByIds(request, writeType, writeParams, agentSkillService);
            }
            if (DELETE_AGENT_KEY.equals(writeType)) {
                return handleDeleteByIds(request, writeType, writeParams, agentDefinitionService);
            }
            if (DELETE_COMMAND_KEY.equals(writeType)) {
                return handleDeleteByIds(request, writeType, writeParams, atomicCommandService);
            }

            // type 不在白名单内则阻断
            return buildBlockedWriteResponse(request, "删除类型[" + writeType + "]不在白名单内");
        } catch (RuntimeException e) {
            log.error("执行删除命令失败 [taskId={}]", request.getTaskId(), e);
            return buildBlockedWriteResponse(request, "删除执行失败：" + e.getMessage());
        }
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
     * 处理创建原子命令。
     *
     * @param request     原子命令调用请求
     * @param writeParams 写入参数
     * @return 原子命令调用响应
     * @author qty
     */
    private AtomicCommandInvokeResponse handleCreateAtomicCommand(AtomicCommandInvokeRequest request, Map<String, Object> writeParams) {
        CreateAtomicCommandRequest createRequest = new CreateAtomicCommandRequest();
        createRequest.setName((String) writeParams.get("name"));
        createRequest.setCommand((String) writeParams.get("command"));
        createRequest.setRole((String) writeParams.get("role"));
        createRequest.setSkillId((String) writeParams.getOrDefault("skillId", null));

        // 备注默认为"AI对话创建"
        createRequest.setRemark((String) writeParams.getOrDefault("remark", "AI对话创建"));

        // 调用原子命令服务保存
        String commandId = atomicCommandService.save(createRequest);
        return buildSuccessResponse(request, "原子命令创建成功", commandId);
    }

    /**
     * 处理创建执行器。
     *
     * @param request     原子命令调用请求
     * @param writeParams 写入参数
     * @return 原子命令调用响应
     * @author qty
     */
    private AtomicCommandInvokeResponse handleCreateExecutor(AtomicCommandInvokeRequest request, Map<String, Object> writeParams) {
        CreateAgentExecutorRequest createRequest = new CreateAgentExecutorRequest();
        createRequest.setExecutorCode((String) writeParams.get("executorCode"));
        createRequest.setExecutorName((String) writeParams.get("executorName"));
        createRequest.setDescription((String) writeParams.getOrDefault("description", ""));
        // 处理状态参数，将字符串转为 Status 枚举
        String statusStr = (String) writeParams.get("status");
        if (statusStr != null) {
            createRequest.setStatus(Status.valueOf(statusStr));
        }
        createRequest.setRemark((String) writeParams.getOrDefault("remark", "AI对话创建"));

        // 调用执行器服务保存
        String executorId = agentExecutorService.save(createRequest);
        return buildSuccessResponse(request, "执行器创建成功", executorId);
    }

    /**
     * 处理创建客户端。
     * <p>客户端创建需要用户ID，从当前登录上下文获取。</p>
     *
     * @param request     原子命令调用请求
     * @param writeParams 写入参数
     * @return 原子命令调用响应
     * @author qty
     */
    private AtomicCommandInvokeResponse handleCreateClient(AtomicCommandInvokeRequest request, Map<String, Object> writeParams) {
        CreateAgentClientRequest createRequest = new CreateAgentClientRequest();
        createRequest.setExecutorId((String) writeParams.get("executorId"));
        createRequest.setClientName((String) writeParams.get("clientName"));
        createRequest.setRemark((String) writeParams.getOrDefault("remark", "AI对话创建"));

        // 过期时间配置（可选）
        Object expireDuration = writeParams.get("expireDuration");
        if (expireDuration instanceof Integer) {
            createRequest.setExpireDuration((Integer) expireDuration);
        }
        createRequest.setExpireUnit((String) writeParams.getOrDefault("expireUnit", null));

        // 从当前登录上下文获取用户ID
        String userId = LoginUserUtils.getUserTemporary().getUserId();
        AssertUtils.notEmpty(userId, "当前登录用户身份为空");

        // 调用客户端服务保存，返回含密钥的详情
        InfoAgentClientResponse clientInfo = agentClientService.save(createRequest, userId);
        return buildSuccessResponse(request, "客户端创建成功", clientInfo.getId());
    }

    /**
     * 处理更新规则。
     *
     * @param request     原子命令调用请求
     * @param writeParams 更新参数
     * @return 原子命令调用响应
     * @author qty
     */
    private AtomicCommandInvokeResponse handleUpdateRule(AtomicCommandInvokeRequest request, Map<String, Object> writeParams) {
        UpdateAgentRuleRequest updateRequest = new UpdateAgentRuleRequest();
        updateRequest.setId((String) writeParams.get("id"));
        updateRequest.setAgentId((String) writeParams.get("agentId"));
        updateRequest.setDefinitionDesc((String) writeParams.get("definitionDesc"));
        updateRequest.setTriggerCondition((String) writeParams.get("triggerCondition"));
        updateRequest.setTriggerAction((String) writeParams.get("triggerAction"));
        updateRequest.setRemark((String) writeParams.getOrDefault("remark", null));

        // 调用规则服务更新
        String ruleId = agentRuleService.updateById(updateRequest);
        return buildSuccessResponse(request, "规则更新成功", ruleId);
    }

    /**
     * 处理更新技能。
     *
     * @param request     原子命令调用请求
     * @param writeParams 更新参数
     * @return 原子命令调用响应
     * @author qty
     */
    private AtomicCommandInvokeResponse handleUpdateSkill(AtomicCommandInvokeRequest request, Map<String, Object> writeParams) {
        UpdateAgentSkillRequest updateRequest = new UpdateAgentSkillRequest();
        updateRequest.setId((String) writeParams.get("id"));
        updateRequest.setAgentId((String) writeParams.get("agentId"));
        updateRequest.setDefinitionDesc((String) writeParams.get("definitionDesc"));
        updateRequest.setExecContent((String) writeParams.get("execContent"));
        updateRequest.setReturnDataFormat((String) writeParams.get("returnDataFormat"));
        updateRequest.setRemark((String) writeParams.getOrDefault("remark", null));

        // 调用技能服务更新
        String skillId = agentSkillService.updateById(updateRequest);
        return buildSuccessResponse(request, "技能更新成功", skillId);
    }

    /**
     * 处理更新智能体定义。
     *
     * @param request     原子命令调用请求
     * @param writeParams 更新参数
     * @return 原子命令调用响应
     * @author qty
     */
    private AtomicCommandInvokeResponse handleUpdateAgent(AtomicCommandInvokeRequest request, Map<String, Object> writeParams) {
        UpdateAgentDefinitionRequest updateRequest = new UpdateAgentDefinitionRequest();
        updateRequest.setId((String) writeParams.get("id"));
        updateRequest.setName((String) writeParams.get("name"));
        updateRequest.setDefinitionDesc((String) writeParams.get("definitionDesc"));
        updateRequest.setFirstPrinciple((String) writeParams.getOrDefault("firstPrinciple", null));
        updateRequest.setSecondRule((String) writeParams.getOrDefault("secondRule", null));
        updateRequest.setThirdSkill((String) writeParams.getOrDefault("thirdSkill", null));
        updateRequest.setDefaultModelId((String) writeParams.getOrDefault("defaultModelId", null));
        updateRequest.setRemark((String) writeParams.getOrDefault("remark", null));

        // 调用智能体定义服务更新
        String agentId = agentDefinitionService.updateById(updateRequest);
        return buildSuccessResponse(request, "智能体更新成功", agentId);
    }

    /**
     * 处理更新原子命令。
     *
     * @param request     原子命令调用请求
     * @param writeParams 更新参数
     * @return 原子命令调用响应
     * @author qty
     */
    private AtomicCommandInvokeResponse handleUpdateAtomicCommand(AtomicCommandInvokeRequest request, Map<String, Object> writeParams) {
        UpdateAtomicCommandRequest updateRequest = new UpdateAtomicCommandRequest();
        updateRequest.setId((String) writeParams.get("id"));
        updateRequest.setName((String) writeParams.get("name"));
        updateRequest.setCommand((String) writeParams.get("command"));
        updateRequest.setRole((String) writeParams.get("role"));
        updateRequest.setSkillId((String) writeParams.getOrDefault("skillId", null));
        updateRequest.setRemark((String) writeParams.getOrDefault("remark", null));

        // 调用原子命令服务更新
        String commandId = atomicCommandService.updateById(updateRequest);
        return buildSuccessResponse(request, "原子命令更新成功", commandId);
    }

    /**
     * 统一处理按ID列表删除。
     * <p>各 Service 的 deleteByIds 方法签名一致，统一分发。</p>
     *
     * @param request      原子命令调用请求
     * @param targetType   删除目标类型
     * @param deleteParams 删除参数（含 ids 字段，逗号分隔）
     * @param service      目标服务对象，需提供 deleteByIds 方法
     * @return 原子命令调用响应
     * @author qty
     */
    private AtomicCommandInvokeResponse handleDeleteByIds(AtomicCommandInvokeRequest request, String targetType, Map<String, Object> deleteParams, Object service) {
        // 从参数中提取逗号分隔的ID列表
        String idsStr = (String) deleteParams.get("ids");
        AssertUtils.notEmpty(idsStr, "删除参数中ids不能为空");

        List<String> ids = Arrays.asList(idsStr.split(","));

        // 按目标类型分发到对应服务执行删除
        if (service instanceof AgentRuleService) {
            ((AgentRuleService) service).deleteByIds(ids);
        } else if (service instanceof AgentSkillService) {
            ((AgentSkillService) service).deleteByIds(ids);
        } else if (service instanceof AgentDefinitionService) {
            ((AgentDefinitionService) service).deleteByIds(ids);
        } else if (service instanceof AtomicCommandService) {
            ((AtomicCommandService) service).deleteByIds(ids);
        } else {
            return buildBlockedWriteResponse(request, "不支持的删除服务类型：" + service.getClass().getSimpleName());
        }

        return buildSuccessResponse(request, targetType + "成功", idsStr);
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
                || upperText.contains(DELETE_ROLE) || text.contains(WRITE_NAME);
    }

    /**
     * 判断角色是否为更新类。
     *
     * @param role 角色文本
     * @return 是否更新类角色
     */
    private boolean isUpdateRole(String role) {

        // 角色为空时不是更新类
        if (role == null || role.isBlank()) {
            return false;
        }
        return role.toUpperCase(Locale.ROOT).contains(UPDATE_ROLE);
    }

    /**
     * 判断角色是否为删除类。
     *
     * @param role 角色文本
     * @return 是否删除类角色
     */
    private boolean isDeleteRole(String role) {

        // 角色为空时不是删除类
        if (role == null || role.isBlank()) {
            return false;
        }
        return role.toUpperCase(Locale.ROOT).contains(DELETE_ROLE);
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