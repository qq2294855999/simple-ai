package com.simple.ai.common.entity.aiModel;

import com.baomidou.mybatisplus.annotation.*;
import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * AI 模型配置实体。
 *
 * @author qty
 */
@Data
@TableName("ai_model")
@Schema(title = "AI 模型配置")
public class AiModel {

    /** 模型主键 */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /** 供应商主键 */
    @TableField("provider_id")
    private String providerId;

    /** 供应商模型编码 */
    @TableField("model_code")
    private String modelCode;

    /** 模型展示名称 */
    @TableField("model_name")
    private String modelName;

    /** 扩展能力配置 */
    @TableField("capability_config")
    private String capabilityConfig;

    /** 上下文窗口 */
    @TableField("context_window")
    private Integer contextWindow;

    /** 是否供应商默认模型 */
    @TableField("provider_default")
    private Integer providerDefault;

    /** 是否系统默认模型 */
    @TableField("system_default")
    private Integer systemDefault;

    /** 创建人 */
    @TableField("create_by")
    private String createBy;

    /** 修改人 */
    @TableField("update_by")
    private String updateBy;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /** 修改时间 */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /** 启停状态 */
    @TableField("status")
    private Status status;

    /** 扩展字段 */
    @TableField("reserve")
    private String reserve;

    /** 备注 */
    @TableField("remark")
    private String remark;
}
