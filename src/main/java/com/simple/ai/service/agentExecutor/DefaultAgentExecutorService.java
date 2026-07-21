package com.simple.ai.service.agentExecutor;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.copy.agentExecutor.AgentExecutorCopyMapper;
import com.simple.ai.common.dto.agentExecutor.*;
import com.simple.ai.common.dto.agentExecutor.AgentExecutorProtocolResponse.FieldInfo;
import com.simple.ai.common.dto.agentExecutor.AgentExecutorProtocolResponse.MessageStructure;
import com.simple.ai.common.dto.agentExecutor.AgentExecutorProtocolResponse.MessageTypeInfo;
import com.simple.ai.common.dto.agentExecutor.AgentExecutorProtocolResponse.SystemCommandInfo;
import com.simple.ai.common.entity.agentExecutor.AgentExecutor;
import com.simple.ai.common.service.agentExecutor.AgentExecutorService;
import com.simple.ai.common.view.agentExecutor.AgentExecutorView;
import com.simple.common.auth.client.util.LoginUserUtils;
import com.simple.common.core.utils.AssertUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 执行器类型(agent_executor)服务默认实现。
 *
 * @author qty
 */
@Slf4j
@Service
@Transactional
class DefaultAgentExecutorService implements AgentExecutorService {

    @Autowired
    private AgentExecutorView agentExecutorView;

    @Autowired
    private AgentExecutorCopyMapper copy;

    @Override
    public IPage<PageAgentExecutorResponse> findAll(PageAgentExecutorRequest pageRequest) {

        // 构建分页对象
        Page<PageAgentExecutorResponse> page = pageRequest.getPage(PageAgentExecutorResponse.class);

        // 执行分页查询
        List<PageAgentExecutorResponse> records = agentExecutorView.findAll(pageRequest, page);
        page.setRecords(records);

        return page;
    }

    @Override
    public InfoAgentExecutorResponse findById(String id) {

        // 查询并校验执行器类型存在
        AgentExecutor entity = agentExecutorView.findById(id);
        AssertUtils.notEmpty(entity, "执行器类型[{}]不存在", id);

        return copy.toInfoResponse(entity);
    }

    @Override
    public String save(CreateAgentExecutorRequest createRequest) {

        // 构建实体并保存
        AgentExecutor entity = copy.toEntity(createRequest);
        entity.setCreateUserId(LoginUserUtils.getUserTemporary().getUserId());
        agentExecutorView.save(entity);
        log.info("执行器创建成功，id={}", entity.getId());

        return entity.getId();
    }

    @Override
    public void updateById(UpdateAgentExecutorRequest updateRequest) {

        // 查询并校验执行器类型存在
        AgentExecutor entity = agentExecutorView.findById(updateRequest.getId());
        AssertUtils.notEmpty(entity, "执行器类型[{}]不存在", updateRequest.getId());

        // 更新执行器类型
        AgentExecutor updatedEntity = copy.toEntity(updateRequest);
        agentExecutorView.updateById(updatedEntity);
        log.info("执行器类型更新成功，id={}", updateRequest.getId());
    }

    @Override
    public void deleteByIds(List<String> ids) {

        // 参数校验：主键列表不能为空
        AssertUtils.notEmpty(ids, "主键列表不能为空");

        // 批量删除
        for (String id : ids) {
            agentExecutorView.deleteById(id);
        }
        log.info("执行器类型删除成功，ids={}", ids);
    }

    @Override
    public AgentExecutorProtocolResponse getProtocol() {

        // 构建协议名称与版本信息
        AgentExecutorProtocolResponse response = new AgentExecutorProtocolResponse().setProtocolName("Simple Executor Protocol")
                                                                                    .setProtocolVersion("v1.0")
                                                                                    .setDescription("SEP v1.0 是智能体系统与远程执行器之间的标准通信协议。"
                                                                                                    + "基于 WebSocket 全双工通道，采用 JSON 格式的 messageType/payload 双层结构，"
                                                                                                    + "支持批量命令下发、逐项结果回传、心跳保活和内置系统命令。");

        // 构建外层消息结构说明
        MessageStructure outerStructure = new MessageStructure().setDescription(
                                                                                "所有 WebSocket 业务消息均使用统一的外层结构包裹，" + "通过 messageType 字段区分消息类型，payload 字段承载具体业务数据。")
                                                                .setJsonExample("{\n  \"messageType\": \"COMMAND_BATCH\",\n  \"payload\": { ... }\n}")
                                                                .setFields(new ArrayList<>());
        outerStructure.getFields()
                      .add(new FieldInfo().setName("messageType")
                                          .setType("String")
                                          .setRequired(true)
                                          .setDescription("协议消息类型，取值：COMMAND_BATCH、COMMAND_RESULT、HEARTBEAT、HEARTBEAT_ACK"));
        outerStructure.getFields().add(new FieldInfo().setName("payload").setType("Object").setRequired(true).setDescription("消息负载，结构随 messageType 变化"));
        response.setOuterStructure(outerStructure);

        // 构建消息类型列表
        response.setMessageTypes(new ArrayList<>());

        // COMMAND_BATCH 消息类型
        MessageTypeInfo commandBatch = new MessageTypeInfo().setTypeName("COMMAND_BATCH")
                                                            .setDirection("Server → Executor")
                                                            .setDescription("服务端向执行器下发批量命令。执行器收到后按顺序逐条执行，" + "每条命令执行完毕后单独回传 COMMAND_RESULT。")
                                                            .setFields(new ArrayList<>())
                                                            .setJsonExample("{\n  \"dispatchId\": \"1901234567890\",\n  \"taskId\": \"1901234567890\",\n"
                                                                            + "  \"clientId\": \"client_001\",\n  \"stopOnFailure\": true,\n"
                                                                            + "  \"minDelayMs\": 100,\n  \"maxDelayMs\": 500,\n  \"commands\": [{\n"
                                                                            + "    \"commandId\": \"1901234567891\",\n    \"sequenceNo\": 10,\n"
                                                                            + "    \"atomicCommandCode\": \"window.find\",\n    \"args\": {},\n"
                                                                            + "    \"timeoutMs\": 10000,\n    \"idempotencyKey\": \"taskId+seq\"\n  }]\n}");
        commandBatch.getFields().add(new FieldInfo().setName("dispatchId").setType("String").setRequired(true).setDescription("调度ID（雪花ID），唯一标识一次命令调度"));
        commandBatch.getFields().add(new FieldInfo().setName("taskId").setType("String").setRequired(true).setDescription("任务ID，关联的任务主键"));
        commandBatch.getFields().add(new FieldInfo().setName("clientId").setType("String").setRequired(true).setDescription("目标客户端ID，WebSocket 点对点路由依据"));
        commandBatch.getFields().add(new FieldInfo().setName("stopOnFailure").setType("Boolean").setRequired(false).setDescription("是否失败即停止后续命令执行"));
        commandBatch.getFields().add(new FieldInfo().setName("minDelayMs").setType("Integer").setRequired(false).setDescription("执行前最小随机延迟(毫秒)，用于模拟人工操作间隔"));
        commandBatch.getFields().add(new FieldInfo().setName("maxDelayMs").setType("Integer").setRequired(false).setDescription("执行前最大随机延迟(毫秒)"));
        commandBatch.getFields().add(new FieldInfo().setName("commands").setType("Array<ExecutorCommandItem>").setRequired(true).setDescription("命令列表，按顺序执行"));
        // commands 内嵌字段
        commandBatch.getFields().add(new FieldInfo().setName("  └─ commandId").setType("String").setRequired(true).setDescription("命令ID（雪花ID），用于关联回执"));
        commandBatch.getFields().add(new FieldInfo().setName("  └─ sequenceNo").setType("Integer").setRequired(true).setDescription("步骤序号，从10递增"));
        commandBatch.getFields().add(new FieldInfo().setName("  └─ atomicCommandCode").setType("String").setRequired(true).setDescription("原子命令编码，如 window.find、control.click"));
        commandBatch.getFields().add(new FieldInfo().setName("  └─ args").setType("Map<String,Object>").setRequired(false).setDescription("命令参数，键值对形式"));
        commandBatch.getFields().add(new FieldInfo().setName("  └─ timeoutMs").setType("Integer").setRequired(false).setDescription("命令超时时间(毫秒)，超时视为失败"));
        commandBatch.getFields().add(new FieldInfo().setName("  └─ idempotencyKey").setType("String").setRequired(false).setDescription("幂等键，用于去重"));
        response.getMessageTypes().add(commandBatch);

        // COMMAND_RESULT 消息类型
        MessageTypeInfo commandResult = new MessageTypeInfo().setTypeName("COMMAND_RESULT")
                                                             .setDirection("Executor → Server")
                                                             .setDescription("执行器向服务端回传单条命令的执行结果。" + "每条命令独立回传，包含执行状态、返回数据和错误详情。")
                                                             .setFields(new ArrayList<>())
                                                             .setJsonExample("{\n  \"dispatchId\": \"1901234567890\",\n  \"taskId\": \"1901234567890\",\n"
                                                                             + "  \"commandId\": \"1901234567891\",\n  \"sequenceNo\": 10,\n"
                                                                             + "  \"success\": true,\n  \"message\": \"窗口已找到\",\n" + "  \"data\": { \"handle\": \"0x123ABC\" },\n"
                                                                             + "  \"error\": null,\n  \"startedAt\": \"2026-07-21T10:00:00Z\",\n"
                                                                             + "  \"finishedAt\": \"2026-07-21T10:00:01Z\"\n}");
        commandResult.getFields().add(new FieldInfo().setName("dispatchId").setType("String").setRequired(true).setDescription("调度ID，与 COMMAND_BATCH 中的 dispatchId 对应"));
        commandResult.getFields().add(new FieldInfo().setName("taskId").setType("String").setRequired(true).setDescription("任务ID，与 COMMAND_BATCH 中的 taskId 对应"));
        commandResult.getFields().add(new FieldInfo().setName("commandId").setType("String").setRequired(true).setDescription("命令ID，与 ExecutorCommandItem.commandId 对应"));
        commandResult.getFields().add(new FieldInfo().setName("sequenceNo").setType("Integer").setRequired(true).setDescription("步骤序号"));
        commandResult.getFields().add(new FieldInfo().setName("success").setType("Boolean").setRequired(true).setDescription("是否执行成功"));
        commandResult.getFields().add(new FieldInfo().setName("message").setType("String").setRequired(false).setDescription("执行说明，成功时为成功描述，失败时为错误简述"));
        commandResult.getFields().add(new FieldInfo().setName("data").setType("Map<String,Object>").setRequired(false).setDescription("返回数据，包含执行结果的关键信息"));
        commandResult.getFields()
                     .add(new FieldInfo().setName("error")
                                         .setType("ExecutorCommandError")
                                         .setRequired(false)
                                         .setDescription("错误详情，失败时包含 error.code、error.detail、error.recoverable"));
        commandResult.getFields().add(new FieldInfo().setName("startedAt").setType("Instant").setRequired(true).setDescription("开始执行时间（ISO8601 UTC）"));
        commandResult.getFields().add(new FieldInfo().setName("finishedAt").setType("Instant").setRequired(true).setDescription("执行完成时间（ISO8601 UTC）"));
        response.getMessageTypes().add(commandResult);

        // HEARTBEAT 消息类型
        MessageTypeInfo heartbeat = new MessageTypeInfo().setTypeName("HEARTBEAT")
                                                         .setDirection("Server → Executor")
                                                         .setDescription("服务端向执行器发送心跳探测。执行器收到后应立即回复 HEARTBEAT_ACK。"
                                                                         + "心跳用于检测 WebSocket 连接的活跃状态，超时未回复视为断连。")
                                                         .setFields(new ArrayList<>())
                                                         .setJsonExample("{\n  \"messageType\": \"HEARTBEAT\",\n  \"payload\": {}\n}");
        heartbeat.getFields().add(new FieldInfo().setName("payload").setType("Object").setRequired(false).setDescription("心跳消息负载为空对象"));
        response.getMessageTypes().add(heartbeat);

        // HEARTBEAT_ACK 消息类型
        MessageTypeInfo heartbeatAck = new MessageTypeInfo().setTypeName("HEARTBEAT_ACK")
                                                            .setDirection("Executor → Server")
                                                            .setDescription("执行器对心跳探测的确认回复，表示执行器仍在运行且 WebSocket 连接正常。")
                                                            .setFields(new ArrayList<>())
                                                            .setJsonExample("{\n  \"messageType\": \"HEARTBEAT_ACK\",\n  \"payload\": {}\n}");
        heartbeatAck.getFields().add(new FieldInfo().setName("payload").setType("Object").setRequired(false).setDescription("心跳确认消息负载为空对象"));
        response.getMessageTypes().add(heartbeatAck);

        // 构建内置系统命令列表
        response.setSystemCommands(new ArrayList<>());

        // system.capability
        SystemCommandInfo sysCapability = new SystemCommandInfo().setCommandCode("system.capability")
                                                                 .setDescription("返回执行器支持的全部原子命令列表。" + "握手鉴权通过后，服务端自动下发此命令以同步执行器能力清单。")
                                                                 .setArgs(new ArrayList<>())
                                                                 .setResultDescription("返回原子命令列表，每条包含命令 code、name、description、"
                                                                                       + "argsSchema、resultSchema、riskLevel、isIdempotent 等元信息")
                                                                 .setJsonExample("{\n  \"messageType\": \"COMMAND_BATCH\",\n  \"payload\": {\n"
                                                                                 + "    \"dispatchId\": \"system\",\n    \"commands\": [{\n"
                                                                                 + "      \"commandId\": \"sys_cap_001\",\n      \"sequenceNo\": 0,\n"
                                                                                 + "      \"atomicCommandCode\": \"system.capability\",\n      \"args\": {}\n" + "    }]\n  }\n}");
        sysCapability.getArgs().add(new FieldInfo().setName("（无参数）").setType("-").setRequired(false).setDescription("system.capability 不需要额外参数"));
        response.getSystemCommands().add(sysCapability);

        // system.health
        SystemCommandInfo sysHealth = new SystemCommandInfo().setCommandCode("system.health")
                                                             .setDescription("返回执行器的健康状态，包括进程状态、系统资源使用情况和当前检查时间。")
                                                             .setArgs(new ArrayList<>())
                                                             .setResultDescription("返回健康检查结果，包含 status（健康状态）、" + "checkedAt（检查时间）、process（进程信息）等")
                                                             .setJsonExample("{\n  \"messageType\": \"COMMAND_BATCH\",\n  \"payload\": {\n"
                                                                             + "    \"dispatchId\": \"system\",\n    \"commands\": [{\n"
                                                                             + "      \"commandId\": \"sys_health_001\",\n      \"sequenceNo\": 0,\n"
                                                                             + "      \"atomicCommandCode\": \"system.health\",\n      \"args\": {}\n" + "    }]\n  }\n}");
        sysHealth.getArgs().add(new FieldInfo().setName("（无参数）").setType("-").setRequired(false).setDescription("system.health 不需要额外参数"));
        response.getSystemCommands().add(sysHealth);

        // 构建通信流程说明
        response.setCommunicationFlow("1. WebSocket 连接建立，携带 type=agent-executor、cliKey=clientId、token=secret 鉴权参数\n" + "2. 服务端鉴权通过后，自动下发 system.capability 命令\n"
                                      + "3. 执行器执行 system.capability 并返回支持的命令列表\n" + "4. 服务端 upsert 原子命令表，同步执行器能力\n" + "5. 正常运行期间，服务端按需下发 COMMAND_BATCH 批量命令\n"
                                      + "6. 执行器逐条执行命令，每条完成后回传 COMMAND_RESULT\n" + "7. 服务端按 commandId 完成等待器，AI 根据结果继续决策\n"
                                      + "8. 服务端定期发送 HEARTBEAT，执行器回复 HEARTBEAT_ACK\n" + "9. 断连后服务端清理等待器，执行器自动重连后重新握手");

        return response;
    }

    @Override
    public String toggleStatus(String id) {

        // 查询并校验执行器类型存在
        AgentExecutor entity = agentExecutorView.findById(id);
        AssertUtils.notEmpty(entity, "执行器类型[{}]不存在", id);

        // 根据当前状态切换："ENABLE" ↔ "DISABLE"
        String currentStatus = entity.getStatus();
        String newStatus = "ENABLE".equals(currentStatus) ? "DISABLE" : "ENABLE";
        entity.setStatus(newStatus);
        agentExecutorView.updateById(entity);
        log.info("执行器类型状态切换成功，id={}，新状态={}", id, newStatus);

        return newStatus;
    }
}
