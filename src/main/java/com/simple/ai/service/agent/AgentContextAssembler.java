package com.simple.ai.service.agent;

import com.simple.ai.common.constant.AgentIronRuleConstant;
import com.simple.ai.common.dto.agent.AgentContext;
import com.simple.ai.common.dto.agent.AgentContextSnapshot;
import com.simple.ai.common.dto.agentMemory.FindAllAgentMemoryRequest;
import com.simple.ai.common.dto.agentRule.FindAllAgentRuleRequest;
import com.simple.ai.common.dto.agentSkill.FindAllAgentSkillRequest;
import com.simple.ai.common.dto.atomicCommand.FindAllAtomicCommandRequest;
import com.simple.ai.common.dto.command.CapabilityResultDto;
import com.simple.ai.common.dto.command.CommandDispatchRequest;
import com.simple.ai.common.dto.subAgentRelation.FindAllSubAgentRelationRequest;
import com.simple.ai.common.entity.agentClient.AgentClient;
import com.simple.ai.common.entity.agentDefinition.AgentDefinition;
import com.simple.ai.common.entity.agentExecutor.AgentExecutor;
import com.simple.ai.common.entity.agentMemory.AgentMemory;
import com.simple.ai.common.entity.agentRule.AgentRule;
import com.simple.ai.common.entity.agentSkill.AgentSkill;
import com.simple.ai.common.entity.atomicCommand.AtomicCommand;
import com.simple.ai.common.entity.protocol.Protocol;
import com.simple.ai.common.entity.subAgentRelation.SubAgentRelation;
import com.simple.ai.common.enums.AgentMemoryVersionStatusProcess;
import com.simple.ai.common.view.agentClient.AgentClientView;
import com.simple.ai.common.view.agentDefinition.AgentDefinitionView;
import com.simple.ai.common.view.agentExecutor.AgentExecutorView;
import com.simple.ai.common.view.agentMemory.AgentMemoryView;
import com.simple.ai.common.view.agentRule.AgentRuleView;
import com.simple.ai.common.view.agentSkill.AgentSkillView;
import com.simple.ai.common.view.atomicCommand.AtomicCommandView;
import com.simple.ai.common.view.protocol.ProtocolView;
import com.simple.ai.common.view.subAgentRelation.SubAgentRelationView;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.core.utils.JsonUtils;
import com.simple.common.mp.common.enums.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 智能体上下文组装器。
 *
 * <p>按 userId 过滤所有资产，确保多用户数据隔离。
 * 通过会话绑定的客户端解析执行器信息，将当前会话上下文写入提示词供 AI 直接感知。
 * 开发阶段直接查询 DB，不做缓存，保持数据一致性和架构简洁。</p>
 *
 * <p>各加载步骤通过 {@link ProgressConsumerHolder} 发布语义化进度事件，
 * 使前端能实时展示"正在加载规则"、"正在加载技能"等具体状态。</p>
 *
 * @author qty
 */
@Component
public class AgentContextAssembler {

    /**
     * 当前快照版本号，用于版本兼容与刷新规则判定。
     * <p>版本规则：</p>
     * <ul>
     *   <li>"1.0"：初始版本，含 agent 资产 + 客户端 + 执行器 + 协议 + 本地原子命令</li>
     *   <li>"1.0"：次版本未变，当前只通过次版本号标识数据刷新</li>
     * </ul>
     */
    public static final String SNAPSHOT_VERSION = "1.0";

    /**
     * 智能体定义视图
     */
    @Autowired
    private AgentDefinitionView agentDefinitionView;

    /**
     * 智能体规则视图
     */
    @Autowired
    private AgentRuleView agentRuleView;

    /**
     * 智能体技能视图
     */
    @Autowired
    private AgentSkillView agentSkillView;

    /**
     * 子智能体关系视图
     */
    @Autowired
    private SubAgentRelationView subAgentRelationView;

    /**
     * 智能体记忆视图
     */
    @Autowired
    private AgentMemoryView agentMemoryView;

    /**
     * 客户端实例视图，用于按 clientId 解析客户端完整对象
     */
    @Autowired
    private AgentClientView agentClientView;

    /**
     * 执行器类型视图，用于按 executorId 解析执行器完整对象
     */
    @Autowired
    private AgentExecutorView agentExecutorView;

    /**
     * 协议视图，用于从 protocolId 解析协议完整对象
     */
    @Autowired
    private ProtocolView protocolView;

    /**
     * 原子命令视图，加载已启用的原子命令供 AI 调用
     */
    @Autowired
    private AtomicCommandView atomicCommandView;

    /**
     * 组装智能体上下文。
     *
     * <p>直接查询 DB 组装 agent 维度数据（定义、规则、技能、子智能体、记忆），
     * 再叠加会话级信息（userId/sessionId/client/executor/protocol）和提示词。</p>
     *
     * @param request 命令调度请求
     * @return 智能体上下文
     */
    public AgentContext assemble(CommandDispatchRequest request) {

        // 参数校验：智能体ID不能为空
        AssertUtils.notEmpty(request.getAgentId(), "智能体ID不能为空");

        // 发布进度：开始组装智能体上下文
        publishProgressSafe("正在加载智能体定义");

        // 直接查询 DB 组装 agent 维度数据
        AgentContext context = doAssemble(request.getAgentId());

        // 覆盖会话级信息：每次对话的 userId/sessionId 不同
        context.setUserId(request.getUserId());
        context.setSessionId(request.getSessionId() != null ? request.getSessionId() : "");

        // 解析当前会话的客户端、执行器、协议完整对象
        resolveSessionContext(context, request.getClientId());

        // 重建提示词（含会话级信息）
        context.setPromptContent(buildPromptContent(context));
        return context;
    }

    /**
     * 按 agentId 加载 AgentContext（供缓存回源使用）。
     * <p>仅加载 agent 维度数据（定义、规则、技能、子智能体、记忆），
     * 不包含会话级信息（client/executor/protocol）。</p>
     * <p>每步加载前通过 ProgressConsumerHolder 发布进度事件。</p>
     *
     * @param agentId 智能体ID
     * @return AgentContext（含 agent 维度数据，不含会话信息）
     */
    public AgentContext doAssemble(String agentId) {

        // 查询智能体定义并校验启用状态
        publishProgressSafe("正在加载智能体定义");
        AgentDefinition agentDefinition = loadAgentDefinition(agentId);

        // 查询智能体直属启用规则
        publishProgressSafe("正在加载规则");
        List<AgentRule> rules = loadRules(agentId);

        // 查询智能体直属启用技能
        publishProgressSafe("正在加载技能");
        List<AgentSkill> skills = loadSkills(agentId);

        // 查询已启用的原子命令列表（供 AI 通过 executeAtomicCommand 调用，依赖技能列表）
        publishProgressSafe("正在查找原子命令");
        List<AtomicCommand> atomicCommands = loadAtomicCommands(skills);

        // 查询主智能体可用子智能体关系
        publishProgressSafe("正在加载子智能体");
        List<SubAgentRelation> subAgentRelations = loadSubAgentRelations(agentId);

        // 查询智能体启用候选记忆（仅已发布版本）
        publishProgressSafe("正在加载记忆");
        List<AgentMemory> memories = loadMemories(agentId);

        // 构建上下文对象（不含会话信息）
        AgentContext context = new AgentContext();
        context.setAgentDefinition(agentDefinition);
        context.setSystemIronRule(AgentIronRuleConstant.SYSTEM_IRON_RULE);
        context.setRules(rules);
        context.setSkills(skills);
        context.setSubAgentRelations(subAgentRelations);
        context.setMemories(memories);
        context.setAtomicCommands(atomicCommands);
        return context;
    }

    /**
     * 查询智能体定义。
     *
     * @param agentId 智能体ID
     * @return 智能体定义
     */
    private AgentDefinition loadAgentDefinition(String agentId) {
        AgentDefinition agentDefinition = agentDefinitionView.findById(agentId);
        AssertUtils.notEmpty(agentDefinition, "智能体[{}]不存在", agentId);
        AssertUtils.isTrue(Status.ON.equals(agentDefinition.getStatus()), "智能体[{}]未启用", agentId);
        return agentDefinition;
    }

    /**
     * 查询智能体直属规则。
     *
     * @param agentId 智能体ID
     * @return 智能体规则列表
     */
    private List<AgentRule> loadRules(String agentId) {
        FindAllAgentRuleRequest request = new FindAllAgentRuleRequest();
        request.setAgentId(agentId);
        request.setStatus(Status.ON);
        return agentRuleView.findAll(request);
    }

    /**
     * 查询智能体直属技能。
     *
     * @param agentId 智能体ID
     * @return 智能体技能列表
     */
    private List<AgentSkill> loadSkills(String agentId) {
        FindAllAgentSkillRequest request = new FindAllAgentSkillRequest();
        request.setAgentId(agentId);
        request.setStatus(Status.ON);
        return agentSkillView.findAll(request);
    }

    /**
     * 查询主智能体可用子智能体关系。
     *
     * @param agentId 智能体ID
     * @return 子智能体关系列表
     */
    private List<SubAgentRelation> loadSubAgentRelations(String agentId) {
        FindAllSubAgentRelationRequest request = new FindAllSubAgentRelationRequest();
        request.setMainAgentId(agentId);
        request.setStatus(Status.ON);
        return subAgentRelationView.findAll(request);
    }

    /**
     * 查询智能体候选记忆（仅已发布版本）。
     *
     * @param agentId 智能体ID
     * @return 智能体记忆列表
     */
    private List<AgentMemory> loadMemories(String agentId) {
        FindAllAgentMemoryRequest request = new FindAllAgentMemoryRequest();
        request.setAgentId(agentId);
        request.setStatus(Status.ON);

        // 仅加载已发布版本的记忆供AI意图识别匹配
        request.setVersionStatus(AgentMemoryVersionStatusProcess.PUBLISHED);
        return agentMemoryView.findAll(request);
    }

    /**
     * 解析当前会话的客户端、执行器、协议完整对象并设置到上下文。
     *
     * <p>通过 clientId 查询客户端实例完整对象，
     * 再通过客户端关联的 executorId 查询执行器完整对象，
     * 最后通过执行器关联的 protocolId 查询协议完整对象。
     * 所有对象直接存入 AgentContext，提示词构建时从中取名称/编码/状态。</p>
     *
     * @param context  智能体上下文（待填充会话信息）
     * @param clientId 客户端ID
     */
    private void resolveSessionContext(AgentContext context, String clientId) {

        // 客户端ID为空时跳过，不阻塞主流程
        if (clientId == null || clientId.isBlank()) {
            return;
        }

        // 查询客户端实例完整对象
        publishProgressSafe("正在查找客户端信息");
        AgentClient client = agentClientView.findById(clientId);
        if (client == null) {
            return;
        }
        context.setClient(client);

        // 通过客户端关联的执行器类型ID查询执行器完整对象
        String executorId = client.getExecutorId();
        if (executorId == null || executorId.isBlank()) {
            return;
        }
        publishProgressSafe("正在加载执行器信息");
        AgentExecutor executor = agentExecutorView.findById(executorId);
        if (executor == null) {
            return;
        }
        context.setExecutor(executor);

        // 通过执行器关联的协议ID查询协议完整对象
        String protocolId = executor.getProtocolId();
        if (protocolId == null || protocolId.isBlank()) {
            return;
        }
        Protocol protocol = protocolView.findById(protocolId);
        if (protocol != null) {
            context.setProtocol(protocol);
        }
    }

    /**
     * 安全发布进度事件，已废弃（运行时上下文已显式传递）。
     *
     * @param message 进度消息
     */
    private void publishProgressSafe(String message) {
        // 已废弃：进度事件现在通过运行时上下文显式传递
    }

    /**
     * 加载已启用的原子命令列表。
     *
     * <p>按技能ID列表批量查询已启用的原子命令，包含全局通用命令和技能专属命令。</p>
     *
     * @param skills 技能列表
     * @return 原子命令列表
     */
    private List<AtomicCommand> loadAtomicCommands(List<AgentSkill> skills) {
        List<AtomicCommand> result = new java.util.ArrayList<>();

        // 收集技能ID列表
        java.util.List<String> skillIds = new java.util.ArrayList<>();
        for (AgentSkill skill : skills) {
            if (skill.getId() != null && !skill.getId().isBlank()) {
                skillIds.add(skill.getId());
            }
        }

        // 按技能ID批量查询已启用原子命令
        if (!skillIds.isEmpty()) {
            FindAllAtomicCommandRequest batchRequest = new FindAllAtomicCommandRequest();
            batchRequest.setSkillIds(skillIds);
            batchRequest.setStatus(Status.ON);
            result.addAll(atomicCommandView.findAll(batchRequest));
        }

        // 查询 skill_id 为空的全局通用命令
        FindAllAtomicCommandRequest globalRequest = new FindAllAtomicCommandRequest();
        globalRequest.setSkillId("");
        globalRequest.setStatus(Status.ON);
        result.addAll(atomicCommandView.findAll(globalRequest));
        return result;
    }

    /**
     * 构建提示词内容。
     *
     * @param context 智能体上下文（含完整会话信息）
     * @return 提示词内容
     */
    private String buildPromptContent(AgentContext context) {
        StringBuilder builder = new StringBuilder();
        appendAgentDefinition(builder, context.getAgentDefinition());
        appendCurrentSession(builder, context);
        appendRules(builder, context.getRules());
        appendSkills(builder, context.getSkills());
        appendSubAgentRelations(builder, context.getSubAgentRelations());
        appendAtomicCommands(builder, context.getAtomicCommands());
        appendMemories(builder, context.getMemories());
        return builder.toString();
    }

    /**
     * 追加当前会话上下文提示词。
     *
     * <p>从完整对象中取名称、编码、状态写入提示词，
     * 使 AI 无需工具调用即可感知会话上下文。
     * 不暴露任何数据库 ID，仅保留人类可读的名称、编码和状态。</p>
     *
     * @param builder 提示词构建器
     * @param context 智能体上下文
     */
    private void appendCurrentSession(StringBuilder builder, AgentContext context) {
        builder.append("<current_session>\n");

        // 客户端信息：从完整对象取名称和在线状态，不暴露 clientId
        AgentClient client = context.getClient();
        if (client != null) {
            builder.append("  <client name=\"").append(safeStr(client.getClientName()));
            builder.append("\" online=\"").append(client.getIsOnline() != null && client.getIsOnline()).append("\" />\n");
        }

        // 执行器信息：从完整对象取编码、名称、描述，不暴露 executorId
        AgentExecutor executor = context.getExecutor();
        if (executor != null) {
            builder.append("  <executor code=\"").append(safeStr(executor.getExecutorCode()));
            builder.append("\" name=\"").append(safeStr(executor.getExecutorName()));
            builder.append("\" desc=\"").append(safeStr(executor.getDescription())).append("\" />\n");
        }

        // 协议信息：从完整对象取名称，不暴露 protocolId
        Protocol protocol = context.getProtocol();
        if (protocol != null && protocol.getProtocolName() != null && !protocol.getProtocolName().isBlank()) {
            builder.append("  <protocol name=\"").append(protocol.getProtocolName()).append("\" />\n");
        }

        builder.append("</current_session>\n\n");
    }

    /**
     * 安全转字符串，null 返回空串。
     *
     * @param value 原始值
     * @return 非null字符串
     */
    private String safeStr(String value) {
        return value != null ? value : "";
    }

    /**
     * 追加智能体定义提示词。
     *
     * <p>告知 AI 自己的身份信息，不暴露 agentId（AI 无需直接操作ID）。</p>
     *
     * @param builder 提示词构建器
     * @param agentDefinition 智能体定义
     */
    private void appendAgentDefinition(StringBuilder builder, AgentDefinition agentDefinition) {
        builder.append("<system_iron_rule>\n");
        builder.append(AgentIronRuleConstant.SYSTEM_IRON_RULE);
        builder.append("\n</system_iron_rule>\n\n");

        builder.append("<agent>\n");
        builder.append("  <name>").append(agentDefinition.getName()).append("</name>\n");
        builder.append("  <definition>").append(agentDefinition.getDefinitionDesc()).append("</definition>\n");
        builder.append("  <first_principle>").append(agentDefinition.getFirstPrinciple()).append("</first_principle>\n");
        builder.append("  <second_rule>").append(agentDefinition.getSecondRule()).append("</second_rule>\n");
        builder.append("  <third_skill>").append(agentDefinition.getThirdSkill()).append("</third_skill>\n");
        builder.append("</agent>\n\n");
    }

    /**
     * 追加规则提示词。
     *
     * @param builder 提示词构建器
     * @param rules 规则列表
     */
    private void appendRules(StringBuilder builder, List<AgentRule> rules) {
        if (rules.isEmpty()) {
            return;
        }
        builder.append("<rules>\n");

        // 遍历直属规则，将定义描述、触发条件和触发动作写入提示词
        for (AgentRule rule : rules) {
            builder.append("  <rule>\n");
            builder.append("    <desc>").append(rule.getDefinitionDesc()).append("</desc>\n");
            builder.append("    <condition>").append(rule.getTriggerCondition()).append("</condition>\n");
            builder.append("    <action>").append(rule.getTriggerAction()).append("</action>\n");
            builder.append("  </rule>\n");
        }
        builder.append("</rules>\n\n");
    }

    /**
     * 追加技能提示词。
     *
     * @param builder 提示词构建器
     * @param skills 技能列表
     */
    private void appendSkills(StringBuilder builder, List<AgentSkill> skills) {
        if (skills.isEmpty()) {
            return;
        }
        builder.append("<skills>\n");

        // 遍历直属技能，将定义描述、执行内容和返回格式写入提示词
        for (AgentSkill skill : skills) {
            builder.append("  <skill>\n");
            builder.append("    <desc>").append(skill.getDefinitionDesc()).append("</desc>\n");
            builder.append("    <content>").append(skill.getExecContent()).append("</content>\n");
            builder.append("    <format>").append(skill.getReturnDataFormat()).append("</format>\n");
            builder.append("  </skill>\n");
        }
        builder.append("</skills>\n\n");
    }

    /**
     * 追加子智能体关系提示词。
     *
     * <p>仅告知 AI 存在子智能体协作关系及数量，不暴露 ID。</p>
     *
     * @param builder 提示词构建器
     * @param subAgentRelations 子智能体关系列表
     */
    private void appendSubAgentRelations(StringBuilder builder, List<SubAgentRelation> subAgentRelations) {
        if (subAgentRelations.isEmpty()) {
            return;
        }
        builder.append("<sub_agents count=\"").append(subAgentRelations.size()).append("\">\n");
        builder.append("  <!-- 存在子智能体协作关系，可通过 queryAgentDefinition 按名称查询详情 -->\n");
        builder.append("</sub_agents>\n\n");
    }

    /**
     * 追加原子命令提示词。
     *
     * <p>告知 AI 可用的原子命令列表及所需参数，AI 通过 executeAtomicCommand 工具调用。</p>
     *
     * @param builder 提示词构建器
     * @param atomicCommands 原子命令列表
     */
    private void appendAtomicCommands(StringBuilder builder, List<AtomicCommand> atomicCommands) {
        if (atomicCommands == null || atomicCommands.isEmpty()) {
            return;
        }
        builder.append("<atomic_commands>\n");

        // 告知 AI 使用 executeAtomicCommand 工具来执行这些命令
        builder.append("  <instruction>");
        builder.append("你可以通过 executeAtomicCommand 工具直接向执行器下发以下命令。");
        builder.append("调用时 commandContent 填命令编码（如 system.capability、app.ensure），");
        builder.append("requestParams 填命令所需的参数（JSON 格式的键值对）。</instruction>\n");

        // 遍历原子命令，列出名称、编码和作用
        for (AtomicCommand cmd : atomicCommands) {
            builder.append("  <command>\n");
            builder.append("    <code>").append(safeStr(cmd.getCommand())).append("</code>\n");
            builder.append("    <name>").append(safeStr(cmd.getName())).append("</name>\n");
            builder.append("    <role>").append(safeStr(cmd.getRole())).append("</role>\n");
            builder.append("  </command>\n");
        }
        builder.append("</atomic_commands>\n\n");
    }

    /**
     * 追加候选记忆提示词。
     *
     * <p>不暴露记忆 ID，仅保留名称、摘要和参数定义。</p>
     *
     * @param builder 提示词构建器
     * @param memories 候选记忆列表
     */
    private void appendMemories(StringBuilder builder, List<AgentMemory> memories) {
        if (memories.isEmpty()) {
            return;
        }
        builder.append("<memories>\n");

        // 遍历已发布记忆，将记忆名称、摘要和参数定义写入提示词供 AI 意图识别
        for (AgentMemory memory : memories) {
            builder.append("  <memory>\n");
            builder.append("    <name>").append(memory.getMemoryName()).append("</name>\n");
            builder.append("    <summary>").append(memory.getSummary() != null ? memory.getSummary() : "").append("</summary>\n");

            // 补充参数定义，供AI判断用户输入是否提供了足够参数
            if (memory.getParamsDefinition() != null && !memory.getParamsDefinition().isBlank()) {
                builder.append("    <params_definition>").append(memory.getParamsDefinition()).append("</params_definition>\n");
            }

            builder.append("  </memory>\n");
        }
        builder.append("</memories>\n\n");
    }

    /**
     * 创建期装配快照：查询全部资产并序列化为 JSON 字符串。
     * <p>用于会话创建时将完整的智能体上下文持久化到 reserve 字段，
     * 避免聊天期重复查询资产表。</p>
     *
     * @param agentId  智能体ID
     * @param clientId 客户端ID
     * @return JSON 格式的快照字符串
     */
    public String assembleForSnapshot(String agentId, String clientId) {
        AssertUtils.notEmpty(agentId, "智能体ID不能为空");

        // 加载 agent 维度数据（定义、规则、技能、子智能体、记忆、原子命令）
        AgentContext context = doAssemble(agentId);

        // 解析会话级信息（客户端、执行器、协议）
        resolveSessionContext(context, clientId);

        // 构建快照 DTO
        AgentContextSnapshot snapshot = new AgentContextSnapshot();
        snapshot.setAgentDefinition(context.getAgentDefinition());
        snapshot.setSystemIronRule(context.getSystemIronRule());
        snapshot.setRules(context.getRules());
        snapshot.setSkills(context.getSkills());
        snapshot.setSubAgentRelations(context.getSubAgentRelations());
        snapshot.setMemories(context.getMemories());
        snapshot.setClient(context.getClient());
        snapshot.setExecutor(context.getExecutor());
        snapshot.setProtocol(context.getProtocol());
        snapshot.setAtomicCommands(context.getAtomicCommands());
        snapshot.setCreatedAt(System.currentTimeMillis());

        // 序列化为 JSON
        return JsonUtils.toJsonStr(snapshot);
    }

    /**
     * 聊天期从快照恢复上下文。
     * <p>从 reserve 字段反序列化快照，补充 userId/sessionId 和提示词后返回，
     * 不再查询任何资产表。</p>
     *
     * @param reserveJson reserve 字段的 JSON 字符串
     * @param userId      当前用户ID
     * @param sessionId   当前会话ID
     * @return 恢复后的智能体上下文
     */
    public AgentContext restoreFromSnapshot(String reserveJson, String userId, String sessionId) {
        AssertUtils.notEmpty(reserveJson, "会话快照不能为空");

        // 反序列化快照
        AgentContextSnapshot snapshot = JsonUtils.toJsonObj(reserveJson, AgentContextSnapshot.class);
        AssertUtils.notEmpty(snapshot, "会话快照解析失败");

        // 构建 AgentContext
        AgentContext context = new AgentContext();
        context.setAgentDefinition(snapshot.getAgentDefinition());
        context.setSystemIronRule(snapshot.getSystemIronRule());
        context.setRules(snapshot.getRules());
        context.setSkills(snapshot.getSkills());
        context.setSubAgentRelations(snapshot.getSubAgentRelations());
        context.setMemories(snapshot.getMemories());
        context.setClient(snapshot.getClient());
        context.setExecutor(snapshot.getExecutor());
        context.setProtocol(snapshot.getProtocol());
        context.setAtomicCommands(snapshot.getAtomicCommands());

        // 覆盖会话级信息
        context.setUserId(userId);
        context.setSessionId(sessionId != null ? sessionId : "");

        // 重建提示词
        context.setPromptContent(buildPromptContent(context));
        return context;
    }

    /**
     * 将执行器能力命令列表写入已有快照 JSON。
     * <p>在创建会话时，先调用 {@link #assembleForSnapshot} 获取基础快照，
     * 再调用 system.capability 获取执行器实时能力，最后通过本方法将能力列表
     * 合并到快照中，刷新版本号后返回。</p>
     *
     * @param snapshotJson 基础快照 JSON
     * @param capabilities 执行器能力命令列表
     * @return 合并后的快照 JSON
     */
    public String enrichSnapshotWithCapabilities(String snapshotJson, List<CapabilityResultDto.CommandItem> capabilities) {
        AssertUtils.notEmpty(snapshotJson, "快照 JSON 不能为空");

        AgentContextSnapshot snapshot = JsonUtils.toJsonObj(snapshotJson, AgentContextSnapshot.class);
        AssertUtils.notEmpty(snapshot, "快照 JSON 解析失败");

        // 写入执行器能力命令列表
        snapshot.setCommandCapabilities(capabilities);

        // 更新快照版本号（次版本递增，表示数据内容刷新）
        snapshot.setVersion(SNAPSHOT_VERSION);
        snapshot.setCreatedAt(System.currentTimeMillis());

        return JsonUtils.toJsonStr(snapshot);
    }

    /**
     * 刷新快照：重新查询全部资产并序列化。
     * <p>用于存量会话显式"刷新上下文"场景，重新从数据库读取最新资产配置，
     * 生成新快照。调用方需自行处理 system.capability 等实时能力注入。</p>
     * <p>刷新规则：</p>
     * <ul>
     *   <li>新会话自动使用最新配置（创建时调用 assembleForSnapshot）</li>
     *   <li>存量会话仅通过显式"刷新上下文"更新快照</li>
     *   <li>刷新后快照版本号更新为当前最新版本</li>
     * </ul>
     *
     * @param agentId  智能体ID
     * @param clientId 客户端ID
     * @return JSON 格式的刷新后快照字符串
     */
    public String refreshSnapshot(String agentId, String clientId) {
        return assembleForSnapshot(agentId, clientId);
    }
}