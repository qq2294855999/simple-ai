package com.simple.ai.common.entity.aiModelProvider;

import com.baomidou.mybatisplus.annotation.*;
import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * AI 模型供应商运行配置实体。
 *
 * @author qty
 */
@Data
@TableName("ai_model_provider")
@Schema(title = "AI 模型供应商运行配置")
public class AiModelProvider {

    /** 供应商主键 */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /** 供应商编码 */
    @TableField("provider_code")
    private String providerCode;

    /** 供应商名称 */
    @TableField("provider_name")
    private String providerName;

    /** 协议类型 */
    @TableField("protocol_type")
    private String protocolType;

    /** OpenAI 兼容服务根地址 */
    @TableField("base_url")
    private String baseUrl;

    /** API Key AES-GCM 密文，绝不对外输出 */
    @TableField("api_key_ciphertext")
    private String apiKeyCiphertext;

    /** 调用超时毫秒数 */
    @TableField("timeout_millis")
    private Integer timeoutMillis;

    /** 是否系统默认供应商 */
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
