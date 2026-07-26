package com.simple.ai.service.agentClient;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.simple.ai.common.entity.agentClient.AgentClient;
import com.simple.ai.common.enums.AgentClientStatusProcess;
import com.simple.ai.common.view.agentClient.AgentClientView;
import com.simple.ai.view.agentClient.AgentClientRepository;
import com.simple.common.websocket.utils.WebSocketUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

/**
 * 客户端在线状态定时同步服务。
 * <p>每30秒遍历所有 ACTIVE 的客户端，通过 WebSocket ChannelMap 校验实际在线状态，
 * 将已断开的客户端标记为离线并记录最后断开时间。</p>
 * <p>服务启动时批量将所有在线客户端重置为离线，避免因服务重启导致状态残留。</p>
 *
 * @author qty
 */
@Slf4j
@Component
public class ClientOnlineStatusSyncService {

    /**
     * WebSocket 客户端类型标识
     */
    private static final String AGENT_EXECUTOR_TYPE = "agent-executor";

    @Autowired
    private AgentClientView agentClientView;

    @Autowired
    private AgentClientRepository agentClientRepository;

    /**
     * 服务启动时批量将所有在线客户端重置为离线。
     * <p>服务重启后 ChannelMap 已清空，所有客户端实际处于离线状态，需要同步到数据库。</p>
     */
    @PostConstruct
    public void resetAllOnlineOnStartup() {

        // 通过 Repository 查询所有标记为在线的客户端
        List<AgentClient> onlineClients = agentClientRepository.selectList(Wrappers.lambdaQuery(AgentClient.class).eq(AgentClient::getIsOnline, true));

        if (onlineClients.isEmpty()) {
            log.info("启动在线状态重置：无需重置的在线客户端");
            return;
        }

        // 批量将所有在线客户端标记为离线
        int resetCount = 0;
        for (AgentClient client : onlineClients) {
            agentClientView.updateOnlineStatus(client.getId(), false, new Date());
            resetCount++;
        }
        log.info("启动在线状态重置完成：已将 {} 个客户端从在线重置为离线", resetCount);
    }

    /**
     * 定时同步在线状态。
     * <p>每30秒检查所有 ACTIVE 的客户端，将 WebSocket 已断开的客户端标记为离线。</p>
     */
    @Scheduled(fixedDelay = 30000)
    public void syncOnlineStatus() {

        // 通过 Repository 查询所有 ACTIVE 状态的客户端
        List<AgentClient> activeClients = agentClientRepository.selectList(Wrappers.lambdaQuery(AgentClient.class).eq(AgentClient::getStatus, AgentClientStatusProcess.ACTIVE));

        if (activeClients.isEmpty()) {
            return;
        }

        int offlineCount = 0;

        // 逐客户端检查 WebSocket 实际在线状态
        for (AgentClient client : activeClients) {
            String clientId = client.getId();

            // 通过 WebSocket ChannelMap 判断是否真正在线
            boolean actuallyOnline = WebSocketUtils.isOnline(AGENT_EXECUTOR_TYPE, clientId);

            // 数据库记录与 ChannelMap 不一致时需同步
            boolean dbOnline = Boolean.TRUE.equals(client.getIsOnline());

            if (dbOnline && !actuallyOnline) {

                // 已断开但数据库中仍标记在线，更新为离线
                agentClientView.updateOnlineStatus(clientId, false, new Date());
                offlineCount++;
                log.debug("同步离线状态：clientId={}", clientId);

            } else if (!dbOnline && actuallyOnline) {

                // ChannelMap 中有连接但数据库未标记在线，更新为在线
                agentClientView.updateOnlineStatus(clientId, true, null);
                log.debug("同步在线状态：clientId={}", clientId);
            }
        }

        if (offlineCount > 0) {
            log.info("在线状态同步完成：{} 个客户端从在线变为离线", offlineCount);
        }
    }
}
