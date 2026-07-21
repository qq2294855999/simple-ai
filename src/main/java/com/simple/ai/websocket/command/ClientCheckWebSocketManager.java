package com.simple.ai.websocket.command;

import com.simple.common.core.utils.CryptoUtil;
import com.simple.common.websocket.manager.DefaultCheckWebSocketManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Agent 客户端 WebSocket 鉴权管理器。
 * <p>继承 {@link DefaultCheckWebSocketManager}，仅拦截 type="agent-executor" 的连接进行业务鉴权。
 * 鉴权流程：查询 agent_client 表 → 校验状态 → 校验过期时间 → BCrypt密码比对 → 更新最后连接时间。</p>
 * <p>非 agent-executor 类型的连接委托父类默认放行，保持对其他 WebSocket 端点兼容。</p>
 *
 * @author qty
 */
@Slf4j
@Component
public class ClientCheckWebSocketManager extends DefaultCheckWebSocketManager {

    /**
     * Spring JDBC 模板，用于直接查询 agent_client 表。
     * <p>在 AgentClientView 等完整基础设施建成之前先通过 JdbcTemplate 完成鉴权逻辑，
     * 后续可平滑替换为 View 层调用。</p>
     */
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * WebSocket 握手鉴权校验。
     * <p>对 type="agent-executor" 的连接执行 agent_client 表校验，
     * 其他类型委托父类默认通过。</p>
     *
     * @param token  客户端密钥明文（WebSocket 连接时传递的 token）
     * @param type   客户端类型
     * @param cliKey 客户端唯一标识，此处为 agent_client 表的 id（即 clientId）
     * @return true 鉴权通过，false 拒绝连接
     */
    @Override
    public boolean checkToken(String token, String type, String cliKey) {

        // 仅处理 agent-executor 类型的连接，其他类型委托父类默认放行
        if (!"agent-executor".equals(type)) {
            return super.checkToken(token, type, cliKey);
        }

        // 校验客户端标识和密钥不为空
        if (isBlank(cliKey) || isBlank(token)) {
            log.warn("agent-executor 连接缺少 cliKey 或 token");
            return false;
        }

        // 查询客户端记录
        Map<String, Object> client = queryClientById(cliKey);
        if (client == null) {
            log.warn("agent-executor 客户端不存在，cliKey={}", cliKey);
            return false;
        }

        // 校验客户端状态
        if (!isClientActive(client, cliKey)) {
            return false;
        }

        // 校验密钥（BCrypt 比对）
        if (!isSecretMatched(token, client, cliKey)) {
            return false;
        }

        // 校验过期时间
        if (isClientExpired(client, cliKey)) {
            return false;
        }

        // 鉴权通过，更新最后连接时间
        updateLastConnectedAt(cliKey);
        log.info("agent-executor 客户端鉴权通过，cliKey={}", cliKey);
        return true;
    }

    /**
     * 查询客户端记录。
     *
     * @param cliKey 客户端ID
     * @return 客户端记录，不存在时返回 null
     */
    private Map<String, Object> queryClientById(String cliKey) {

        // 使用 FOR UPDATE 行锁防止并发鉴权冲突
        String sql = "SELECT id, client_secret_hash, status, expire_time FROM agent_client WHERE id = ? FOR UPDATE";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, cliKey);
        if (result.isEmpty()) {
            return null;
        }
        return result.get(0);
    }

    /**
     * 校验客户端状态是否为 ACTIVE。
     *
     * @param client 客户端记录
     * @param cliKey 客户端ID
     * @return true 状态为 ACTIVE
     */
    private boolean isClientActive(Map<String, Object> client, String cliKey) {
        String status = (String) client.get("status");

        // 非 ACTIVE 状态不允许连接
        if (!"ACTIVE".equals(status)) {
            log.warn("agent-executor 客户端状态异常，cliKey={}，status={}", cliKey, status);
            return false;
        }
        return true;
    }

    /**
     * 校验客户端密钥是否匹配。
     * <p>使用 BCrypt 将明文 token 与数据库存储的 secret_hash 进行比对。</p>
     *
     * @param token  客户端传入的密钥明文
     * @param client 客户端记录
     * @param cliKey 客户端ID
     * @return true 密钥匹配
     */
    private boolean isSecretMatched(String token, Map<String, Object> client, String cliKey) {
        String secretHash = (String) client.get("client_secret_hash");

        // 数据库中密钥哈希为空时拒绝连接
        if (secretHash == null || secretHash.isBlank()) {
            log.warn("agent-executor 客户端密钥哈希为空，cliKey={}", cliKey);
            return false;
        }

        // BCrypt 密码比对
        if (!CryptoUtil.checkPassword(token, secretHash)) {
            log.warn("agent-executor 客户端密钥校验失败，cliKey={}", cliKey);
            return false;
        }
        return true;
    }

    /**
     * 校验客户端是否已过期。
     * <p>过期时自动将客户端状态更新为 EXPIRED。</p>
     *
     * @param client 客户端记录
     * @param cliKey 客户端ID
     * @return true 已过期
     */
    private boolean isClientExpired(Map<String, Object> client, String cliKey) {
        Date expireTime = (Date) client.get("expire_time");

        // 未设置过期时间则永不过期
        if (expireTime == null) {
            return false;
        }

        // 当前时间已超过过期时间
        if (expireTime.before(new Date())) {

            // 自动将过期客户端状态标记为 EXPIRED
            String updateSql = "UPDATE agent_client SET status = 'EXPIRED' WHERE id = ?";
            jdbcTemplate.update(updateSql, cliKey);
            log.warn("agent-executor 客户端已过期，已自动标记为 EXPIRED，cliKey={}", cliKey);
            return true;
        }
        return false;
    }

    /**
     * 更新最后连接时间。
     *
     * @param cliKey 客户端ID
     */
    private void updateLastConnectedAt(String cliKey) {
        String sql = "UPDATE agent_client SET last_connected_at = ? WHERE id = ?";
        jdbcTemplate.update(sql, new Date(), cliKey);
    }

    /**
     * 判断字符串是否为空。
     *
     * @param text 文本
     * @return true 为空或仅含空白字符
     */
    private static boolean isBlank(String text) {
        return text == null || text.isBlank();
    }
}
