package com.simple.ai.service.session;

import com.simple.ai.common.service.session.AgentSessionService;
import com.simple.common.core.utils.AssertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis 智能体会话服务实现。
 *
 * @author qty
 */
@Service
class RedisAgentSessionService implements AgentSessionService {

    /**
     * 会话摘要缓存前缀
     */
    private static final String SUMMARY_KEY_PREFIX = "simple-ai:agent:session:summary:";

    /**
     * 会话消息缓存前缀
     */
    private static final String MESSAGE_KEY_PREFIX = "simple-ai:agent:session:message:";

    /**
     * 会话缓存过期时间
     */
    private static final Duration SESSION_CACHE_TIME = Duration.ofHours(24);

    /**
     * 会话摘要列表缓存前缀
     */
    private static final String SUMMARY_LIST_KEY_PREFIX = "simple-ai:agent:session:summary-list:";

    /**
     * 会话摘要列表最大保留轮数
     */
    private static final long MAX_SUMMARY_SIZE = 20L;

    /**
     * 会话消息最大保留数量
     */
    private static final long MAX_MESSAGE_SIZE = 100L;

    /**
     * Redis 字符串操作模板
     */
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public String findSummary(String sessionId) {
        
        // 参数校验：会话ID不能为空
        AssertUtils.notEmpty(sessionId, "会话ID不能为空");

        // 查询 Redis 中保存的会话摘要
        String summaryKey = buildSummaryKey(sessionId);
        return stringRedisTemplate.opsForValue().get(summaryKey);
    }

    @Override
    public void saveSummary(String sessionId, String summary) {
        
        // 参数校验：会话ID不能为空
        AssertUtils.notEmpty(sessionId, "会话ID不能为空");

        // 保留原有覆盖式摘要键，兼容现有读取逻辑
        String summaryKey = buildSummaryKey(sessionId);
        stringRedisTemplate.opsForValue().set(summaryKey, summary, SESSION_CACHE_TIME);

        // 同时追加到摘要列表，保留最近N轮摘要用于拼接多轮上下文
        String summaryListKey = buildSummaryListKey(sessionId);
        stringRedisTemplate.opsForList().rightPush(summaryListKey, summary);
        stringRedisTemplate.opsForList().trim(summaryListKey, -MAX_SUMMARY_SIZE, -1);
        stringRedisTemplate.expire(summaryListKey, SESSION_CACHE_TIME);
    }

    @Override
    public void appendMessage(String sessionId, String message) {
        
        // 参数校验：会话ID不能为空
        AssertUtils.notEmpty(sessionId, "会话ID不能为空");

        // 参数校验：会话消息不能为空
        AssertUtils.notEmpty(message, "会话消息不能为空");

        // 追加会话消息并刷新过期时间
        String messageKey = buildMessageKey(sessionId);
        stringRedisTemplate.opsForList().rightPush(messageKey, message);
        stringRedisTemplate.opsForList().trim(messageKey, -MAX_MESSAGE_SIZE, -1);
        stringRedisTemplate.expire(messageKey, SESSION_CACHE_TIME);
    }

    @Override
    public void deleteBySessionId(String sessionId) {

        // 参数校验：会话ID不能为空
        AssertUtils.notEmpty(sessionId, "会话ID不能为空");

        // 删除 Redis 中的会话摘要缓存
        String summaryKey = buildSummaryKey(sessionId);
        stringRedisTemplate.delete(summaryKey);

        // 删除 Redis 中的会话摘要列表缓存
        String summaryListKey = buildSummaryListKey(sessionId);
        stringRedisTemplate.delete(summaryListKey);

        // 删除 Redis 中的会话消息缓存
        String messageKey = buildMessageKey(sessionId);
        stringRedisTemplate.delete(messageKey);
    }

    /**
     * 构建会话摘要缓存键。
     *
     * @param sessionId 会话ID
     * @return 会话摘要缓存键
     */
    private String buildSummaryKey(String sessionId) {
        return SUMMARY_KEY_PREFIX + sessionId;
    }

    /**
     * 构建会话摘要列表缓存键。
     *
     * @param sessionId 会话ID
     * @return 会话摘要列表缓存键
     */
    private String buildSummaryListKey(String sessionId) {
        return SUMMARY_LIST_KEY_PREFIX + sessionId;
    }

    /**
     * 构建会话消息缓存键。
     *
     * @param sessionId 会话ID
     * @return 会话消息缓存键
     */
    private String buildMessageKey(String sessionId) {
        return MESSAGE_KEY_PREFIX + sessionId;
    }
}
