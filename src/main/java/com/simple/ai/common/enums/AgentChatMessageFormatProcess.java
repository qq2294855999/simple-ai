package com.simple.ai.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 智能体聊天消息内容格式。
 *
 * <p>数据库存储枚举 name()，MyBatis-Plus 默认行为。</p>
 *
 * @author qty
 */
@Getter
@AllArgsConstructor
public enum AgentChatMessageFormatProcess {

    PLAIN_TEXT("纯文本"),
    RESTRICTED_MARKDOWN("受限Markdown");

    /**
     * 中文说明
     */
    private final String label;
}
