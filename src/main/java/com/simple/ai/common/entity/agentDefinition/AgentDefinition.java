package com.simple.ai.common.entity.agentDefinition;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.simple.common.mp.common.enums.DeleteState;
import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * 智能体定义(agent_definition)实体类
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
@TableName(value = "agent_definition", autoResultMap = true)
@Schema(title = "智能体定义(agent_definition)实体类")
public class AgentDefinition {

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 定义描述
     */
    @TableField(value = "definition_desc")
    private String definitionDesc;

    /**
     * 第一铁律
     */
    @TableField(value = "first_principle")
    private String firstPrinciple;

    /**
     * 第二规则
     */
    @TableField(value = "second_rule")
    private String secondRule;

    /**
     * 第三技能
     */
    @TableField(value = "third_skill")
    private String thirdSkill;

    /**
     * 模型
     */
    @TableField(value = "model")
    private String model;

    /**
     * 默认模型主键。
     *
     * <p>运行时仅使用此字段作为智能体级模型选择，不使用历史 model 字段回退。</p>
     */
    @TableField(value = "default_model_id")
    private String defaultModelId;

    /**
     * 创建人
     */
    @TableField(value = "create_by")
    private String createBy;

    /**
     * 修改人
     */
    @TableField(value = "update_by")
    private String updateBy;

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
    @TableField(value = "reserver")
    private String reserver;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

}

