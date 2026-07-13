package com.simple.ai.service.command;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 命令调度进度事件单元测试。
 *
 * @author qty
 */
class DefaultCommandDispatchServiceTest {

    /**
     * 验证轨迹事件只保留白名单摘要，绝不投递完整提示词或请求参数。
     */
    @Test
    void resolveSafeProgressPayloadShouldExcludeSensitiveDispatchContent() {
        DefaultCommandDispatchService service = new DefaultCommandDispatchService();
        String sensitiveContent = "完整 prompt 与原始请求参数：private-context";

        // 非白名单事件必须清空原始载荷
        String taskPayload = invokeResolveSafeProgressPayload(service, "TASK_CREATED", sensitiveContent);
        String stepPayload = invokeResolveSafeProgressPayload(service, "STEP_STARTED", sensitiveContent);
        String aiStartedPayload = invokeResolveSafeProgressPayload(service, "AI_STARTED", sensitiveContent);
        assertEquals("", taskPayload);
        assertEquals("", stepPayload);
        assertEquals("", aiStartedPayload);

        // 结构化数量摘要可以安全保留给执行轨迹展示
        String summary = "{\"resourceType\":\"rule\",\"count\":2}";
        String rulePayload = invokeResolveSafeProgressPayload(service, "RULE_LOADED", summary);
        assertEquals(summary, rulePayload);
    }

    /**
     * 验证 AI token 仅保留给聊天消息流，不会转换为结构化轨迹载荷。
     */
    @Test
    void resolveSafeProgressPayloadShouldKeepAiTokenOnlyForMessageFlow() {
        DefaultCommandDispatchService service = new DefaultCommandDispatchService();

        // AI token 的分流由前端 eventType 完成，服务端不将其转换为轨迹摘要
        String token = "本地 token 片段";
        String actual = invokeResolveSafeProgressPayload(service, "AI_TOKEN", token);
        assertEquals(token, actual);
    }

    /**
     * 调用私有安全载荷收敛方法。
     *
     * @param service 调度服务
     * @param eventType 事件类型
     * @param payload 原始载荷
     * @return 可公开载荷
     */
    private String invokeResolveSafeProgressPayload(DefaultCommandDispatchService service, String eventType, String payload) {
        return ReflectionTestUtils.invokeMethod(service, "resolveSafeProgressPayload", eventType, payload);
    }
}
