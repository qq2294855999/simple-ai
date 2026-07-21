package com.simple.ai.common.entity.agentMemoryVersionDetail;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 记忆版本步骤(agent_memory_version_detail)实体类。
 *
 * @author qty
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@TableName(value = "agent_memory_version_detail", autoResultMap = true)
@Schema(title = "记忆版本步骤(agent_memory_version_detail)实体类")
public class AgentMemoryVersionDetail {

    /**
     * 主键。
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 记忆版本ID。
     */
    @TableField(value = "version_id")
    private String versionId;

    /**
     * 步骤序号。
     */
    @TableField(value = "sequence_no")
    private Integer sequenceNo;

    /**
     * 原子命令ID。
     */
    @TableField(value = "atomic_command_id")
    private String atomicCommandId;

    /**
     * 原子命令编码。
     */
    @TableField(value = "atomic_command_code")
    private String atomicCommandCode;

    /**
     * 参数模板。
     */
    @TableField(value = "args_template")
    private String argsTemplate;

    /**
     * 执行前随机延迟最小值。
     */
    @TableField(value = "delay_min_ms")
    private Integer delayMinMs;

    /**
     * 执行前随机延迟最大值。
     */
    @TableField(value = "delay_max_ms")
    private Integer delayMaxMs;

    /**
     * 命令超时时间。
     */
    @TableField(value = "timeout_ms")
    private Integer timeoutMs;

    /**
     * 幂等键。
     */
    @TableField(value = "idempotency_key")
    private String idempotencyKey;

    /**
     * 成功断言规则。
     */
    @TableField(value = "success_assertion")
    private String successAssertion;

    /**
     * 失败处理策略。
     */
    @TableField(value = "failure_strategy")
    private String failureStrategy;

    /**
     * 状态。
     */
    @TableField(value = "status")
    private String status;

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
