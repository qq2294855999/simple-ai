package com.simple.ai.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 智能体聊天消息角色。
 *
 * <p>数据库存储枚举 name()，MyBatis-Plus 默认行为。</p>
 *
 * @author qty
 */
@Getter
@AllArgsConstructor
public enum AgentChatMessageRoleProcess {

    USER("用户"),
    ASSISTANT("助手"),
    SYSTEM_ERROR("系统错误");

    /**
     * 中文说明
     */
    private final String label;
}
