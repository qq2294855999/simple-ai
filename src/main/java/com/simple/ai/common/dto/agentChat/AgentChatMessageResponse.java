package com.simple.ai.common.dto.agentChat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 智能体聊天消息响应。
 *
 * @author qty
 */
@Data
@Schema(title = "智能体聊天消息响应")
public class AgentChatMessageResponse {

    /** 消息主键 */
    private String id;

    /** 调度任务主键 */
    private String taskId;

    /** 消息角色 */
    private String role;

    /** 消息内容 */
    private String content;

    /** 内容格式 */
    private String contentFormat;

    /** 会话内序号 */
    private Long sequenceNo;

    /** 运行供应商名称快照 */
    private String providerName;

    /** 运行模型编码快照 */
    private String modelCode;

    /** 创建时间 */
    private Date createTime;
}
