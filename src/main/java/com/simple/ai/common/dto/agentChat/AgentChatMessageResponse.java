package com.simple.ai.common.dto.agentChat;

import com.simple.ai.common.enums.AgentChatMessageFormatProcess;
import com.simple.ai.common.enums.AgentChatMessageRoleProcess;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 智能体聊天消息响应。
 *
 * @author qty
 */
@Data
@Schema(title = "智能体聊天消息响应")
public class AgentChatMessageResponse {

    /**
     * 消息主键
     */
    private String id;

    /**
     * 调度任务主键
     */
    private String taskId;

    /**
     * 对话轮次主键
     */
    private String turnId;

    /**
     * 消息角色
     */
    private AgentChatMessageRoleProcess role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 内容格式
     */
    private AgentChatMessageFormatProcess contentFormat;

    /**
     * 会话内序号
     */
    private Long sequenceNo;

    /**
     * 运行供应商名称快照
     */
    private String providerName;

    /**
     * 运行模型编码快照
     */
    private String modelCode;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 该消息关联的执行事件列表，用于前端内嵌折叠轨迹展示
     */
    private List<AgentChatExecutionEventDto> executionEvents;
}
