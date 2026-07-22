package com.simple.ai.common.entity.agentMemoryDetail;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.simple.ai.common.enums.AgentStepTypeProcess;
import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 智能体记忆详情(agent_memory_detail)实体类
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
@TableName(value = "agent_memory_detail", autoResultMap = true)
@Schema(title = "智能体记忆详情(agent_memory_detail)实体类")
public class AgentMemoryDetail {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 智能体记忆ID
     */
    @TableField(value = "agent_memory_id")
    private String agentMemoryId;

    /**
     * 步骤名称
     */
    @TableField(value = "step_name")
    private String stepName;

    /**
     * 步骤类型：智能体记忆步骤类型
     */
    @TableField(value = "step_type")
    private AgentStepTypeProcess stepType;

    /**
     * 执行内容
     */
    @TableField(value = "exec_content")
    private String execContent;

    /**
     * 返回的数据格式
     */
    @TableField(value = "return_data_format")
    private String returnDataFormat;

    /**
     * 父步骤ID
     */
    @TableField(value = "parent_step_id")
    private String parentStepId;

    /**
     * 下一个步骤ID
     */
    @TableField(value = "next_step_id")
    private String nextStepId;

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
     * 模型
     */
    @TableField(value = "model")
    private String model;

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

