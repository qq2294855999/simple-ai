package com.simple.ai.common.entity.agentMemoryStep;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 智能体记忆步骤(agent_memory_step)实体类。
 *
 * <p>记忆步骤是记忆的有序执行序列，每步对应一个原子命令调用。
 * sequence_no 决定执行顺序，args_template 支持与记忆 params_definition
 * 对应的 {param} 占位符，执行时由 MemoryExecutor 替换为实际参数值。</p>
 *
 * @author qty
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(chain = true)
@TableName(value = "agent_memory_step", autoResultMap = true)
@Schema(title = "智能体记忆步骤(agent_memory_step)实体类")
public class AgentMemoryStep {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 记忆ID
     */
    @TableField(value = "memory_id")
    private String memoryId;

    /**
     * 步骤序号，从10开始递增，决定执行顺序
     */
    @TableField(value = "sequence_no")
    private Integer sequenceNo;

    /**
     * 原子命令主键，关联 atomic_command.id
     */
    @TableField(value = "atomic_command_id")
    private String atomicCommandId;

    /**
     * 原子命令编码（冗余），如 weixin_search_contact
     */
    @TableField(value = "atomic_command_code")
    private String atomicCommandCode;

    /**
     * 步骤名称，如"搜索联系人"
     */
    @TableField(value = "step_name")
    private String stepName;

    /**
     * 参数模板JSON，支持{param}占位符
     */
    @TableField(value = "args_template")
    private String argsTemplate;

    /**
     * 执行前随机延迟最小值（毫秒）
     */
    @TableField(value = "delay_min_ms")
    private Integer delayMinMs;

    /**
     * 执行前随机延迟最大值（毫秒）
     */
    @TableField(value = "delay_max_ms")
    private Integer delayMaxMs;

    /**
     * 命令超时时间（毫秒）
     */
    @TableField(value = "timeout_ms")
    private Integer timeoutMs;

    /**
     * 成功断言规则，用于判断该步骤是否执行成功
     */
    @TableField(value = "success_assertion")
    private String successAssertion;

    /**
     * 失败处理策略: STOP(停止) / RETRY(重试) / SKIP(跳过)
     */
    @TableField(value = "failure_strategy")
    private String failureStrategy;

    /**
     * 状态：ON(启用) / OFF(停用)
     */
    @TableField(value = "status")
    private String status;

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
}