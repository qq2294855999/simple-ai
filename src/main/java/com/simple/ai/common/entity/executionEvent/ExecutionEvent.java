package com.simple.ai.common.entity.executionEvent;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 执行事件(execution_event)实体类
 * 注解@JSONField(serialize = false)，表示不返回这个字段
 * 注解@TableField，用于标志属性
 * value = "数据库字段"，用于标志数据库对应字段
 * exist = false，表示数据库没有这个字段
 * typeHandler = JacksonTypeHandler.class，表示对象JSON转化为实例，需要类上开启@TableName(autoResultMap = true)
 * fill = FieldFill.INSERT，表示添加操作时要做的事情
 * 注解@TableLogic添加在属性上，结合配置文件，可设置逻辑删除
 * 注解@EqualsAndHashCode是为类生成Equals和HashCode方法
 * callSuper = false 代表方法不调用父类继承的属性，只匹配子类本身是否相同
 * callSuper = true 代表方法需要调用父类继承的属性，同时匹配本身和父类的属性
 *
 * @author qty
 */
@Data //提供读写属性, 此外还提供了 equals()、hashCode()、toString() 方法
@JsonIgnoreProperties(ignoreUnknown = true) //json转换时，字段少了也可以转换
@Accessors(chain = true) //开启链式调用
@TableName(value = "execution_event", autoResultMap = true)
@Schema(title = "执行事件(execution_event)实体类")
public class ExecutionEvent {

    /**
     * 事件主键，UUID
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 轮次主键，关联 chat_turn.id
     */
    @TableField(value = "turn_id")
    private String turnId;

    /**
     * 调度任务主键，关联 task.id
     */
    @TableField(value = "task_id")
    private String taskId;

    /**
     * 任务详情主键，关联 task_detail.id
     */
    @TableField(value = "task_detail_id")
    private String taskDetailId;

    /**
     * 事件类型: CONTEXT_ASSEMBLING/CONTEXT_ASSEMBLED/MEMORY_MATCHING/MEMORY_MATCHED/MEMORY_MISSED/ATOMIC_COMMAND_START/ATOMIC_COMMAND_COMPLETE/ATOMIC_COMMAND_FAILED/AI_STARTED/AI_COMPLETED/SUB_AGENT_STARTED/SUB_AGENT_COMPLETED/TURN_COMPLETED/TASK_FAILED
     */
    @TableField(value = "event_type")
    private String eventType;

    /**
     * 步骤名称（展示用）
     */
    @TableField(value = "step_name")
    private String stepName;

    /**
     * 原子命令名称
     */
    @TableField(value = "command_name")
    private String commandName;

    /**
     * 原子命令请求内容（截断500字符）
     */
    @TableField(value = "command_content")
    private String commandContent;

    /**
     * 原子命令响应内容（截断500字符，完整内容在 task_detail.return_params）
     */
    @TableField(value = "response_content")
    private String responseContent;

    /**
     * 失败原因
     */
    @TableField(value = "failure_reason")
    private String failureReason;

    /**
     * 轮次内事件序号，从1递增
     */
    @TableField(value = "sequence_no")
    private Integer sequenceNo;

    /**
     * 开始时间
     */
    @TableField(value = "started_at")
    private Date startedAt;

    /**
     * 结束时间
     */
    @TableField(value = "finished_at")
    private Date finishedAt;

    /**
     * 原子命令主键
     */
    @TableField(value = "atomic_command_id")
    private String atomicCommandId;

    /**
     * 原子命令编码
     */
    @TableField(value = "atomic_command_code")
    private String atomicCommandCode;

    /**
     * 运行供应商主键快照
     */
    @TableField(value = "provider_id")
    private String providerId;

    /**
     * 运行供应商名称快照
     */
    @TableField(value = "provider_name")
    private String providerName;

    /**
     * 运行模型主键快照
     */
    @TableField(value = "model_id")
    private String modelId;

    /**
     * 运行模型编码快照
     */
    @TableField(value = "model_code")
    private String modelCode;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 状态: ON/DISABLE
     */
    @TableField(value = "status")
    private Status status;

}

