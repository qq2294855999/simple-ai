package com.simple.ai.common.entity.agentMemoryStep;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.simple.ai.common.enums.AgentStepTypeProcess;
import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 智能体记忆步骤(agent_memory_step)实体类。
 * <p>记忆步骤是记忆的有序执行序列，每步对应一个原子命令调用。
 * args_template 支持与记忆 params_definition 对应的 {param} 占位符，
 * 执行时由 MemoryExecutor 替换为实际参数值。</p>
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
     * 步骤序号（从1开始）
     */
    @TableField(value = "step_no")
    private Integer stepNo;

    /**
     * 步骤名称
     */
    @TableField(value = "step_name")
    private String stepName;

    /**
     * 原子命令ID
     */
    @TableField(value = "atomic_command_id")
    private String atomicCommandId;

    /**
     * 参数模板JSON，支持{param}占位符
     */
    @TableField(value = "args_template")
    private String argsTemplate;

    /**
     * 步骤类型
     */
    @TableField(value = "step_type")
    private AgentStepTypeProcess stepType;

    /**
     * 父步骤ID（用于分支/循环嵌套）
     */
    @TableField(value = "parent_step_id")
    private String parentStepId;

    /**
     * 下一步骤ID
     */
    @TableField(value = "next_step_id")
    private String nextStepId;

    /**
     * 分支条件表达式
     */
    @TableField(value = "branch_condition")
    private String branchCondition;

    /**
     * 分支路由标识
     */
    @TableField(value = "branch_route")
    private String branchRoute;

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