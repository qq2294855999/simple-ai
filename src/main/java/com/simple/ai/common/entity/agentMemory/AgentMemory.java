package com.simple.ai.common.entity.agentMemory;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 智能体记忆(agent_memory)实体类。
 *
 * <p>记忆是智能体探索成功路径的沉淀，包含参数化名称模板和步骤序列。
 * 用户再次发起相同意图时，可直接按记忆步骤执行而无需 AI 探索。</p>
 *
 * @author qty
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(chain = true)
@TableName(value = "agent_memory", autoResultMap = true)
@Schema(title = "智能体记忆(agent_memory)实体类")
public class AgentMemory {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 智能体ID
     */
    @TableField(value = "agent_id")
    private String agentId;

    /**
     * 记忆名称模板，支持{param}占位符
     */
    @TableField(value = "memory_name")
    private String memoryName;

    /**
     * 参数定义JSON，描述每个占位符的类型和含义
     */
    @TableField(value = "params_definition", typeHandler = JacksonTypeHandler.class)
    private String paramsDefinition;

    /**
     * 当前版本号
     */
    @TableField(value = "version_no")
    private Integer versionNo;

    /**
     * 版本状态：1=DRAFT, 2=PUBLISHED
     */
    @TableField(value = "version_status")
    private Integer versionStatus;

    /**
     * 来源任务ID（首次沉淀时的任务）
     */
    @TableField(value = "source_task_id")
    private String sourceTaskId;

    /**
     * 记忆摘要
     */
    @TableField(value = "summary")
    private String summary;

    /**
     * 创建原因：MANUAL/AUTO_EXPLORE/AUTO_FIX
     */
    @TableField(value = "create_reason")
    private String createReason;

    /**
     * 客户端ID
     */
    @TableField(value = "client_id")
    private String clientId;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    private String userId;

    /**
     * 创建人ID
     */
    @TableField(value = "create_user_id")
    private String createUserId;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 状态
     */
    @TableField(value = "status")
    private Status status;

    /**
     * 预留字段
     */
    @TableField(value = "reserve")
    private String reserve;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;
}