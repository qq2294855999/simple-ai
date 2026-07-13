package com.simple.ai.common.dto.agentChat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 智能体聊天会话响应。
 *
 * @author qty
 */
@Data
@Schema(title = "智能体聊天会话响应")
public class AgentChatSessionResponse {

    /** 会话主键 */
    private String id;

    /** 智能体主键 */
    private String agentId;

    /** 智能体名称 */
    private String agentName;

    /** 会话名称 */
    private String sessionName;

    /** 最后消息时间 */
    private Date lastMessageAt;
}
