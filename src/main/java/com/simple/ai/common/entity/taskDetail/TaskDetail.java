package com.simple.ai.common.entity.taskDetail;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.simple.ai.common.enums.AgentExecutionStatusProcess;
import com.simple.ai.common.enums.AgentStepTypeProcess;
import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 任务详情(task_detail)实体类
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
@TableName(value = "task_detail", autoResultMap = true)
@Schema(title = "任务详情(task_detail)实体类")
public class TaskDetail {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 任务主键
     */
    @TableField(value = "task_id")
    private String taskId;

    /**
     * 任务名称
     */
    @TableField(value = "task_name")
    private String taskName;

    /**
     * 父任务ID
     */
    @TableField(value = "parent_task_id")
    private String parentTaskId;

    /**
     * 下一个任务ID
     */
    @TableField(value = "next_task_id")
    private String nextTaskId;

    /**
     * 步骤类型：智能体步骤类型
     */
    @TableField(value = "step_type")
    private AgentStepTypeProcess stepType;

    /**
     * 分支条件
     */
    @TableField(value = "branch_condition")
    private String branchCondition;

    /**
     * 分支路由
     */
    @TableField(value = "branch_route")
    private String branchRoute;

    /**
     * 请求参数
     */
    @TableField(value = "request_params")
    private String requestParams;

    /**
     * 返回参数
     */
    @TableField(value = "return_params")
    private String returnParams;

    /**
     * 执行状态
     */
    @TableField(value = "exec_status")
    private AgentExecutionStatusProcess execStatus;

    /** 运行供应商主键快照 */
    @TableField(value = "provider_id")
    private String providerId;

    /** 运行供应商名称快照 */
    @TableField(value = "provider_name")
    private String providerName;

    /** 运行模型主键快照 */
    @TableField(value = "model_id")
    private String modelId;

    /** 运行模型编码快照 */
    @TableField(value = "model_code")
    private String modelCode;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 状态
     */
    @TableField(value = "status")
    private Status status;

    /**
     * 扩展
     */
    @TableField(value = "reserve")
    private String reserve;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

}

