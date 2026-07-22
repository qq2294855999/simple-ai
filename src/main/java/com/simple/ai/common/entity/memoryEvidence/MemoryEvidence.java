package com.simple.ai.common.entity.memoryEvidence;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 记忆证据(memory_evidence)实体类
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
@TableName(value = "memory_evidence", autoResultMap = true)
@Schema(title = "记忆证据(memory_evidence)实体类")
public class MemoryEvidence {

    /**
     * 证据主键，UUID
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 轮次主键，关联 chat_turn.id
     */
    @TableField(value = "turn_id")
    private String turnId;

    /**
     * 记忆版本主键，关联 agent_memory_version.id
     */
    @TableField(value = "memory_version_id")
    private String memoryVersionId;

    /**
     * 证据类型: EXECUTION_TRACE/REASONING_SUMMARY
     */
    @TableField(value = "evidence_type")
    private String evidenceType;

    /**
     * 证据内容: 原子命令调用链+结果摘要
     */
    @TableField(value = "evidence_content")
    private String evidenceContent;

    /**
     * 质量评分 0.00-1.00
     */
    @TableField(value = "quality_score")
    private Double qualityScore;

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

