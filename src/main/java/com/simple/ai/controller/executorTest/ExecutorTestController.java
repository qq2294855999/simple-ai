package com.simple.ai.controller.executorTest;

import cn.hutool.json.JSONUtil;
import com.simple.ai.common.dto.command.ExecutorCommandBatchRequest;
import com.simple.ai.common.dto.command.ExecutorCommandItem;
import com.simple.ai.common.dto.command.SepMessage;
import com.simple.common.core.response.R;
import com.simple.common.core.utils.IdUtils;
import com.simple.common.core.utils.JsonUtils;
import com.simple.common.websocket.utils.WebSocketUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 执行器 WebSocket 通信测试控制层。
 * <p>用于手动向指定执行器客户端发送 SEP v1.0 协议消息（fire-and-forget），
 * 发送和接收的原始 JSON 均通过 SLF4J 日志打印。</p>
 *
 * @author qty
 */
@Tag(name = "执行器通信测试")
@RequestMapping("sys/executor-test")
@RestController
public class ExecutorTestController {

    private static final Logger log = LoggerFactory.getLogger(ExecutorTestController.class);

    /**
     * 执行器 WebSocket 通道类型
     */
    private static final String EXECUTOR_CHANNEL_TYPE = "agent-executor";

    /**
     * 向指定客户端发送 system.capability 命令。
     *
     * @param clientId 客户端主键（cliKey）
     * @return 发送结果
     */
    @PostMapping("capability/{clientId}")
    @Operation(summary = "向执行器发送 system.capability")
    public R<Map<String, Object>> sendCapability(@PathVariable String clientId) {
        return sendCommand(clientId, "system.capability", "{}");
    }

    /**
     * 向指定客户端发送任意原子命令（fire-and-forget）。
     *
     * @param clientId    客户端主键（cliKey）
     * @param commandCode 原子命令编码
     * @param argsJson    命令参数 JSON
     * @return 发送结果
     */
    @PostMapping("send/{clientId}")
    @Operation(summary = "向执行器发送任意原子命令（fire-and-forget，日志打印收发 JSON）")
    public R<Map<String, Object>> sendCommand(@PathVariable String clientId, @RequestParam(defaultValue = "system.capability") String commandCode,
                                              @RequestParam(required = false, defaultValue = "{}") String argsJson) {

        if (clientId == null || clientId.isBlank()) {
            return R.error("ERROR", "客户端ID不能为空");
        }
        if (commandCode == null || commandCode.isBlank()) {
            return R.error("ERROR", "命令编码不能为空");
        }

        // 解析 args JSON
        Map<String, Object> argsMap = Collections.emptyMap();
        if (argsJson != null && !argsJson.isBlank() && !"{}".equals(argsJson.trim())) {
            try {
                argsMap = JSONUtil.parseObj(argsJson);
            } catch (Exception e) {
                return R.error("ERROR", "argsJson 解析失败: " + e.getMessage());
            }
        }

        // 检查在线状态
        boolean online = WebSocketUtils.isOnline(EXECUTOR_CHANNEL_TYPE, clientId);

        // 构建 SEP 协议消息
        String commandId = IdUtils.getSnowflakeNextIdStr();
        String dispatchId = IdUtils.getSnowflakeNextIdStr();
        ExecutorCommandItem item = new ExecutorCommandItem().setCommandId(commandId).setSequenceNo(10).setAtomicCommandCode(commandCode).setArgs(argsMap).setTimeoutMs(30000);
        ExecutorCommandBatchRequest batchRequest = new ExecutorCommandBatchRequest().setDispatchId(dispatchId)
                                                                                    .setTaskId(UUID.randomUUID().toString())
                                                                                    .setClientId(clientId)
                                                                                    .setStopOnFailure(true)
                                                                                    .setCommands(Collections.singletonList(item));
        SepMessage<ExecutorCommandBatchRequest> message = new SepMessage<>();
        message.setMessageType("COMMAND_BATCH");
        message.setPayload(batchRequest);
        String sentJson = JsonUtils.toJsonStr(message);

        // 打印发送的原始 JSON
        log.info("=== 发送命令 [{}] -> clientId={} ===\n{}", commandCode, clientId, sentJson);

        // 点对点发送
        WebSocketUtils.sendMsg(EXECUTOR_CHANNEL_TYPE, clientId, sentJson);

        // 构建响应
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("online", online);
        result.put("commandCode", commandCode);
        result.put("sentJson", sentJson);
        result.put("note", online ? "命令已发送（fire-and-forget），发送和接收的原始 JSON 通过日志输出" : "客户端当前离线，命令未能投递");
        return R.ok(result);
    }
}
