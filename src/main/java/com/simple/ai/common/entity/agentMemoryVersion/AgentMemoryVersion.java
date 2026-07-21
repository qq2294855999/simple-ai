package com.simple.ai.common.entity.agentMemoryVersion;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.simple.ai.common.enums.AgentMemoryVersionStatusProcess;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 记忆版本(agent_memory_version)实体类。
 *
 * @author qty
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@TableName(value = "agent_memory_version", autoResultMap = true)
@Schema(title = "记忆版本(agent_memory_version)实体类")
public class AgentMemoryVersion {

    /**
     * 主键。
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 记忆ID。
     */
    @TableField(value = "memory_id")
    private String memoryId;

    /**
     * 版本号。
     */
    @TableField(value = "version_no")
    private Integer versionNo;

    /**
     * 版本状态。
     */
    @TableField(value = "version_status")
    private AgentMemoryVersionStatusProcess versionStatus;

    /**
     * 来源任务ID。
     */
    @TableField(value = "source_task_id")
    private String sourceTaskId;

    /**
     * 成功判定规则。
     */
    @TableField(value = "success_assertion")
    private String successAssertion;

    /**
     * 版本摘要。
     */
    @TableField(value = "summary")
    private String summary;

    /**
     * 创建原因。
     */
    @TableField(value = "create_reason")
    private String createReason;

    /**
     * 创建人用户ID。
     */
    @TableField(value = "create_user_id")
    private String createUserId;

    /**
     * 创建时间。
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 修改时间。
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
