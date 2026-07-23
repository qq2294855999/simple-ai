package com.simple.ai.service.agent;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.simple.ai.common.dto.agent.ToolQueryByIdRequest;
import com.simple.ai.common.dto.agentClient.CreateAgentClientRequest;
import com.simple.ai.common.dto.agentDefinition.CreateAgentDefinitionRequest;
import com.simple.ai.common.dto.agentExecutor.CreateAgentExecutorRequest;
import com.simple.ai.common.dto.agentExecutor.InfoAgentExecutorResponse;
import com.simple.ai.common.dto.agentMemory.CreateAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemoryVersion.CreateAgentMemoryVersionRequest;
import com.simple.ai.common.dto.agentRule.CreateAgentRuleRequest;
import com.simple.ai.common.dto.agentSkill.CreateAgentSkillRequest;
import com.simple.ai.common.dto.atomicCommand.CreateAtomicCommandRequest;
import com.simple.ai.common.entity.agentChatSession.AgentChatSession;
import com.simple.ai.common.entity.agentExecutor.AgentExecutor;
import com.simple.ai.common.service.agentClient.AgentClientService;
import com.simple.ai.common.service.agentDefinition.AgentDefinitionService;
import com.simple.ai.common.service.agentExecutor.AgentExecutorService;
import com.simple.ai.common.service.agentMemory.AgentMemoryService;
import com.simple.ai.common.service.agentMemoryVersion.AgentMemoryVersionService;
import com.simple.ai.common.service.agentRule.AgentRuleService;
import com.simple.ai.common.service.agentSkill.AgentSkillService;
import com.simple.ai.common.service.atomicCommand.AtomicCommandService;
import com.simple.ai.view.agentChatSession.AgentChatSessionRepository;
import com.simple.ai.view.agentExecutor.AgentExecutorRepository;
import com.simple.common.core.utils.AssertUtils;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 智能体 AI 工具注册中心。
 * <p>将现有 Service 层的 CRUD 操作封装为 Spring AI ToolCallback，
 * 让 AI 模型在对话中自主调用工具完成数据操作。</p>
 *
 * @author qty
 */
@Component
public class AgentToolRegistry {

    /**
     * 智能体会话上下文持有者，用于获取会话上下文中的用户ID
     */
    @Autowired
    private AgentSessionContextHolder agentSessionContextHolder;

    /**
     * 智能体聊天会话数据访问层，用于从会话获取用户上下文
     */
    @Autowired
    private AgentChatSessionRepository agentChatSessionRepository;

    // ──────────────────────────── CREATE 工具（8个）────────────────────────────

    /**
     * 创建智能体记忆工具。
     *
     * @param agentMemoryService 智能体记忆服务
     * @return 工具回调
     */
    @Bean
    public ToolCallback createMemory(AgentMemoryService agentMemoryService) {
        return FunctionToolCallback.builder("createMemory", agentMemoryService::save)
                                   .description("创建新的智能体记忆。参数：agentId（智能体定义主键ID，必填，注意：不是执行器ID，" + "可通过 queryAgentDefinition 查询已有智能体获取）、memoryName（记忆名称，必填）、"
                                                + "stepName（步骤名称，必填）、triggerCondition（触发条件，必填）、"
                                                + "triggerAction（触发动作，必填）、remark（备注，可选）")
                                   .inputType(CreateAgentMemoryRequest.class)
                                   .build();
    }

    /**
     * 创建智能体规则工具。
     *
     * @param agentRuleService 智能体规则服务
     * @return 工具回调
     */
    @Bean
    public ToolCallback createRule(AgentRuleService agentRuleService) {
        return FunctionToolCallback.builder("createRule", agentRuleService::save)
                                   .description("创建新的智能体规则。参数：agentId（智能体定义主键ID，必填，注意：不是执行器ID，" + "可通过 queryAgentDefinition 查询已有智能体获取）、definitionDesc（定义描述，必填）、"
                                                + "triggerCondition（触发条件，必填）、triggerAction（触发动作，必填）、remark（备注，可选）")
                                   .inputType(CreateAgentRuleRequest.class)
                                   .build();
    }

    /**
     * 创建智能体技能工具。
     *
     * @param agentSkillService 智能体技能服务
     * @return 工具回调
     */
    @Bean
    public ToolCallback createSkill(AgentSkillService agentSkillService) {
        return FunctionToolCallback.builder("createSkill", agentSkillService::save)
                                   .description("创建新的智能体技能。参数：agentId（智能体定义主键ID，必填，注意：不是执行器ID，" + "可通过 queryAgentDefinition 查询已有智能体获取）、definitionDesc（定义描述，必填）、"
                                                + "execContent（执行内容，必填）、returnDataFormat（返回的数据格式，必填）、remark（备注，可选）")
                                   .inputType(CreateAgentSkillRequest.class)
                                   .build();
    }

    /**
     * 创建智能体定义工具。
     *
     * @param agentDefinitionService 智能体定义服务
     * @return 工具回调
     */
    @Bean
    public ToolCallback createAgentDefinition(AgentDefinitionService agentDefinitionService) {
        return FunctionToolCallback.builder("createAgentDefinition", agentDefinitionService::save)
                                   .description("创建新的智能体定义。参数：name（名称，必填）、definitionDesc（定义描述，必填）、" + "firstPrinciple（第一铁律，可选）、secondRule（第二规则，可选）、thirdSkill（第三技能，可选）、"
                                                + "defaultModelId（默认模型主键，可选）、remark（备注，可选）。"
                                                + "创建成功后会返回智能体主键ID，后续创建记忆/规则/技能时需要使用此ID作为agentId参数。")
                                   .inputType(CreateAgentDefinitionRequest.class)
                                   .build();
    }

    /**
     * 创建原子命令工具。
     *
     * @param atomicCommandService 原子命令服务
     * @return 工具回调
     */
    @Bean
    public ToolCallback createAtomicCommand(AtomicCommandService atomicCommandService) {
        return FunctionToolCallback.builder("createAtomicCommand", atomicCommandService::save)
                                   .description("创建新的原子命令。参数：name（名称，必填）、command（命令，必填）、" + "role（作用，必填）、skillId（智能体技能主键ID，可选，注意：不是智能体ID，"
                                                + "是 createSkill 返回的技能ID）、remark（备注，可选）")
                                   .inputType(CreateAtomicCommandRequest.class)
                                   .build();
    }

    /**
     * 创建执行器工具。
     * <p>AI 不得传入 status，该字段由数据库默认值控制（int2=1 即启用）。</p>
     *
     * @param agentExecutorService 执行器服务
     * @return 工具回调
     */
    @Bean
    public ToolCallback createExecutor(AgentExecutorService agentExecutorService) {
        return FunctionToolCallback.builder("createExecutor", (CreateAgentExecutorRequest req) -> {
                                       // 阻止 AI 传入的 status 写入实体，使用数据库默认值
                                       req.setStatus(null);
                                       return agentExecutorService.save(req);
                                   })
                                   .description("创建新的执行器。参数：executorCode（执行器编码，必填，如 win_rpa、linux_shell）、" + "executorName（执行器名称，必填）、description（执行器描述，可选）、remark（备注，可选）。"
                                                + "注意：executorId 是执行器主键ID，与 agentId（智能体ID）完全不同。")
                                   .inputType(CreateAgentExecutorRequest.class)
                                   .build();
    }

    /**
     * 创建客户端实例工具。
     * <p>客户端创建需要用户归属，通过 sessionId 从会话获取 userId，避免 ThreadLocal 在异步线程中丢失。</p>
     *
     * @param agentClientService 客户端服务
     * @return 工具回调
     */
    @Bean
    public ToolCallback createClient(AgentClientService agentClientService) {
        return FunctionToolCallback.builder("createClient", (CreateAgentClientRequest req) -> {
                                       // 通过 sessionId 从会话获取 userId，SessionAwareToolCallback 已在执行线程上设置了 ThreadLocal
                                       String userId = resolveUserIdFromSession();
                                       AssertUtils.notEmpty(userId, "当前登录用户身份为空");
                                       return agentClientService.save(req, userId);
                                   })
                                   .description("创建新的客户端实例。参数：executorId（执行器类型主键ID，必填，注意：不是智能体ID，" + "可通过 queryExecutor 查询执行器获取）、clientName（客户端名称，必填）、"
                                                + "expireDuration（过期时间数字，可选）、expireUnit（过期时间单位，可选，" + "可选值：DAY/DAYS、WEEK/WEEKS、MONTH/MONTHS、YEAR/YEARS）、remark（备注，可选）。"
                                                + "创建成功后会返回客户端主键ID。")
                                   .inputType(CreateAgentClientRequest.class)
                                   .build();
    }

    /**
     * 创建记忆版本工具。
     *
     * @param agentMemoryVersionService 记忆版本服务
     * @return 工具回调
     */
    @Bean
    public ToolCallback createMemoryVersion(AgentMemoryVersionService agentMemoryVersionService) {
        return FunctionToolCallback.builder("createMemoryVersion", agentMemoryVersionService::save)
                                   .description("创建新的记忆版本。参数：memoryId（记忆主键ID，必填，注意：不是智能体ID，" + "是 createMemory 返回的记忆ID）、versionNo（版本号，必填）、"
                                                + "versionStatus（版本状态，可选）、sourceTaskId（来源任务ID，可选）、"
                                                + "successAssertion（成功判定规则，可选）、summary（版本摘要，可选）、createReason（创建原因，可选）")
                                   .inputType(CreateAgentMemoryVersionRequest.class)
                                   .build();
    }

    // ──────────────────────────── READ 工具（7个）────────────────────────────

    /**
     * 查询智能体记忆工具。
     *
     * @param agentMemoryService 智能体记忆服务
     * @return 工具回调
     */
    @Bean
    public ToolCallback queryMemory(AgentMemoryService agentMemoryService) {
        return FunctionToolCallback.builder("queryMemory", (ToolQueryByIdRequest req) -> agentMemoryService.findById(req.getId()))
                                   .description("根据ID查询智能体记忆的详细信息。参数：id（主键ID，必填）")
                                   .inputType(ToolQueryByIdRequest.class)
                                   .build();
    }

    /**
     * 查询智能体规则工具。
     *
     * @param agentRuleService 智能体规则服务
     * @return 工具回调
     */
    @Bean
    public ToolCallback queryRule(AgentRuleService agentRuleService) {
        return FunctionToolCallback.builder("queryRule", (ToolQueryByIdRequest req) -> agentRuleService.findById(req.getId()))
                                   .description("根据ID查询智能体规则的详细信息。参数：id（主键ID，必填）")
                                   .inputType(ToolQueryByIdRequest.class)
                                   .build();
    }

    /**
     * 查询智能体技能工具。
     *
     * @param agentSkillService 智能体技能服务
     * @return 工具回调
     */
    @Bean
    public ToolCallback querySkill(AgentSkillService agentSkillService) {
        return FunctionToolCallback.builder("querySkill", (ToolQueryByIdRequest req) -> agentSkillService.findById(req.getId()))
                                   .description("根据ID查询智能体技能的详细信息。参数：id（主键ID，必填）")
                                   .inputType(ToolQueryByIdRequest.class)
                                   .build();
    }

    /**
     * 查询智能体定义工具。
     * <p>支持按主键ID或智能体名称查询，优先按主键ID匹配，
     * 主键查不到时再尝试按名称查找。</p>
     *
     * @param agentDefinitionService    智能体定义服务
     * @param agentDefinitionRepository 智能体定义数据访问层
     * @return 工具回调
     */
    @Bean
    public ToolCallback queryAgentDefinition(AgentDefinitionService agentDefinitionService, com.simple.ai.view.agentDefinition.AgentDefinitionRepository agentDefinitionRepository) {
        return FunctionToolCallback.builder("queryAgentDefinition", (ToolQueryByIdRequest req) -> {
                                       // 先尝试按主键ID查询，失败时不抛异常，继续尝试按名称查询
                                       try {
                                           com.simple.ai.common.dto.agentDefinition.InfoAgentDefinitionResponse response = agentDefinitionService.findById(req.getId());
                                           if (response != null) {
                                               return response;
                                           }
                                       } catch (Exception ignored) {
                                           // 主键查询失败时，继续尝试按名称查询
                                       }

                                       // 主键查不到或查询失败时，按 name 查询
                                       com.simple.ai.common.entity.agentDefinition.AgentDefinition definition = agentDefinitionRepository.selectOne(
                                                       new LambdaQueryWrapper<com.simple.ai.common.entity.agentDefinition.AgentDefinition>().eq(com.simple.ai.common.entity.agentDefinition.AgentDefinition::getName,
                                                                                                                                                req.getId()));
                                       AssertUtils.notEmpty(definition, "智能体[{}]不存在", req.getId());

                                       // 通过真实ID再查一次，返回标准响应格式
                                       return agentDefinitionService.findById(definition.getId());
                                   }).description("根据ID或名称查询智能体定义的详细信息。参数：id（主键ID或智能体名称，必填）。" + "查询结果中的 id 字段就是 agentId，后续创建记忆/规则/技能时需要使用此ID。")
                                   .inputType(ToolQueryByIdRequest.class)
                                   .build();
    }

    /**
     * 查询原子命令工具。
     *
     * @param atomicCommandService 原子命令服务
     * @return 工具回调
     */
    @Bean
    public ToolCallback queryAtomicCommand(AtomicCommandService atomicCommandService) {
        return FunctionToolCallback.builder("queryAtomicCommand", (ToolQueryByIdRequest req) -> atomicCommandService.findById(req.getId()))
                                   .description("根据ID查询原子命令的详细信息。参数：id（主键ID，必填）")
                                   .inputType(ToolQueryByIdRequest.class)
                                   .build();
    }

    /**
     * 查询执行器工具。
     * <p>支持按主键ID或执行器编码（executorCode）查询，优先按主键ID匹配，
     * 主键查不到时再尝试按执行器编码查找。</p>
     *
     * @param agentExecutorService    执行器服务
     * @param agentExecutorRepository 执行器数据访问层
     * @return 工具回调
     */
    @Bean
    public ToolCallback queryExecutor(AgentExecutorService agentExecutorService, AgentExecutorRepository agentExecutorRepository) {
        return FunctionToolCallback.builder("queryExecutor", (ToolQueryByIdRequest req) -> {
            // 先尝试按主键ID查询，失败时不抛异常，继续尝试按执行器编码查询
            try {
                InfoAgentExecutorResponse response = agentExecutorService.findById(req.getId());
                if (response != null) {
                    return response;
                }
            } catch (Exception ignored) {
                // 主键查询失败时，继续尝试按执行器编码查询
            }

            // 主键查不到或查询失败时，按 executor_code 查询
            AgentExecutor executor = agentExecutorRepository.selectOne(new LambdaQueryWrapper<AgentExecutor>().eq(AgentExecutor::getExecutorCode, req.getId()));
            AssertUtils.notEmpty(executor, "执行器[{}]不存在", req.getId());

            // 通过真实ID再查一次，返回标准响应格式
            return agentExecutorService.findById(executor.getId());
        }).description("根据ID或执行器编码查询执行器的详细信息。参数：id（主键ID或执行器编码，必填）").inputType(ToolQueryByIdRequest.class).build();
    }

    /**
     * 查询客户端实例工具。
     *
     * @param agentClientService 客户端服务
     * @return 工具回调
     */
    @Bean
    public ToolCallback queryClient(AgentClientService agentClientService) {
        return FunctionToolCallback.builder("queryClient", (ToolQueryByIdRequest req) -> agentClientService.findById(req.getId()))
                                   .description("根据主键ID查询客户端实例的详细信息。参数：id（主键ID，必填）")
                                   .inputType(ToolQueryByIdRequest.class)
                                   .build();
    }

    /**
     * 从当前会话获取用户ID。
     * <p>优先从 Redis 获取 userId，若 Redis 未命中则从数据库查询会话实体获取。
     * SessionAwareToolCallback 已在 boundedElastic 执行线程上通过 ThreadLocal 设置了 sessionId，
     * 因此本方法可以直接通过 AgentSessionContext 获取。</p>
     *
     * @return 用户ID
     */
    private String resolveUserIdFromSession() {
        // 通过 AgentSessionContext 获取 sessionId（由 SessionAwareToolCallback 在执行线程上设置）
        String sessionId = AgentSessionContext.getCurrentSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            return null;
        }

        // 优先从 Redis 获取 userId（AI 调用前存入）
        String userId = agentSessionContextHolder.getUserId(sessionId);
        if (userId != null && !userId.isBlank()) {
            return userId;
        }

        // Redis 未命中时，从数据库查询会话获取 userId（兜底方案）
        AgentChatSession session = agentChatSessionRepository.selectById(sessionId);
        if (session == null) {
            return null;
        }
        return session.getUserId();
    }
}