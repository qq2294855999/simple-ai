package com.simple.ai.service.agent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 智能体会话上下文持有者。
 * <p>通过 Redis 存储 sessionId → 会话上下文（userId、agentId）映射，
 * 供 ToolCallback 在异步线程（boundedElastic）中获取会话上下文，
 * 避免 ThreadLocal 跨线程丢失。</p>
 * <p>存储格式：使用 ":::" 分隔 userId 和 agentId。
 * 键名：agent:session:context:{sessionId} → userId:::agentId</p>
 *
 * @author qty
 */
@Component
public class AgentSessionContextHolder {

    /**
     * 会话上下文字段分隔符
     */
    private static final String CONTEXT_SEPARATOR = ":::";

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * Redis 中会话上下文 key 前缀。
     */
    private static final String SESSION_CONTEXT_KEY_PREFIX = "agent:session:context:";

    /**
     * 会话上下文缓存过期时间（分钟）。
     */
    private static final Duration SESSION_CONTEXT_TTL = Duration.ofMinutes(30);

    /**
     * 会话上下文内部类，包含工具回调所需的会话级参数。
     *
     * @author qty
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SessionContext {

        /**
         * 用户ID
         */
        private String userId;

        /**
         * 智能体定义ID
         */
        private String agentId;
    }

    /**
     * 存储会话上下文（完整上下文，包含 userId 和 agentId）。
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     * @param agentId   智能体定义ID
     */
    public void putContext(String sessionId, String userId, String agentId) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }

        // 使用分隔符拼接 userId 和 agentId，存储为单个 Redis 字符串值
        String safeUserId = userId != null ? userId : "";
        String safeAgentId = agentId != null ? agentId : "";
        String value = safeUserId + CONTEXT_SEPARATOR + safeAgentId;
        String redisKey = SESSION_CONTEXT_KEY_PREFIX + sessionId;
        stringRedisTemplate.opsForValue().set(redisKey, value, SESSION_CONTEXT_TTL);
    }

    /**
     * 获取完整会话上下文。
     *
     * @param sessionId 会话ID
     * @return 会话上下文，未命中时返回 null
     */
    public SessionContext getContext(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return null;
        }
        String redisKey = SESSION_CONTEXT_KEY_PREFIX + sessionId;
        String value = stringRedisTemplate.opsForValue().get(redisKey);

        // Redis 未命中时返回 null
        if (value == null || value.isBlank()) {
            return null;
        }

        // 按分隔符解析 userId 和 agentId
        String[] parts = value.split(CONTEXT_SEPARATOR, 2);
        String userId = parts.length > 0 && !parts[0].isBlank() ? parts[0] : null;
        String agentId = parts.length > 1 && !parts[1].isBlank() ? parts[1] : null;
        return new SessionContext(userId, agentId);
    }

    /**
     * 存储会话上下文（仅 userId，保持向后兼容）。
     *
     * @param sessionId 会话ID
     * @param userId    用户ID
     * @deprecated 使用 {@link #putContext(String, String, String)} 替代
     */
    @Deprecated
    public void put(String sessionId, String userId) {
        if (sessionId == null || sessionId.isBlank() || userId == null || userId.isBlank()) {
            return;
        }
        putContext(sessionId, userId, "");
    }

    /**
     * 获取会话上下文中的用户ID。
     *
     * @param sessionId 会话ID
     * @return 用户ID
     * @deprecated 使用 {@link #getContext(String)} 替代
     */
    @Deprecated
    public String getUserId(String sessionId) {
        SessionContext context = getContext(sessionId);
        return context != null ? context.getUserId() : null;
    }

    /**
     * 清除会话上下文。
     *
     * @param sessionId 会话ID
     */
    public void remove(String sessionId) {
        if (sessionId == null) {
            return;
        }
        String redisKey = SESSION_CONTEXT_KEY_PREFIX + sessionId;
        stringRedisTemplate.delete(redisKey);
    }
}