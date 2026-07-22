package com.simple.ai.common.entity.chatTurn;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.Map;

/**
 * 对话轮次(chat_turn)实体类
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
@TableName(value = "chat_turn", autoResultMap = true)
@Schema(title = "对话轮次(chat_turn)实体类")
public class ChatTurn {

    /**
     * 轮次主键，UUID
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 会话主键，关联 agent_chat_session.id
     */
    @TableField(value = "session_id")
    private String sessionId;

    /**
     * 会话内轮次序号，从1递增
     */
    @TableField(value = "turn_number")
    private Integer turnNumber;

    /**
     * 该轮用户消息ID，关联 agent_chat_message.id
     */
    @TableField(value = "user_message_id")
    private String userMessageId;

    /**
     * 该轮AI回复消息ID，关联 agent_chat_message.id（AI回复完成前为NULL）
     */
    @TableField(value = "assistant_message_id")
    private String assistantMessageId;

    /**
     * 关联的调度任务ID（冗余便于查询）
     */
    @TableField(value = "task_id")
    private String taskId;

    /**
     * 受控推理摘要: {"intent":"...","actions":[...],"outcome":"..."}（不包含模型原始思维链）
     */
    @TableField(value = "reasoning_summary")
    private String reasoningSummary;

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
     * 状态: ON/DISABLE
     */
    @TableField(value = "status")
    private Status status;

    /**
     * 扩展字段，JSON格式
     */
    @TableField(value = "reserve", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> reserve;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

}

