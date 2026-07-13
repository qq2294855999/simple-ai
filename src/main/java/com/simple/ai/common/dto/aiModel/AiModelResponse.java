package com.simple.ai.common.dto.aiModel;

import lombok.Data;

import java.util.Date;

/**
 * AI 模型管理与选择响应。
 *
 * @author qty
 */
@Data
public class AiModelResponse {

    /** 模型主键 */
    private String id;

    /** 供应商主键 */
    private String providerId;

    /** 供应商名称 */
    private String providerName;

    /** 协议类型 */
    private String protocolType;

    /** 模型编码 */
    private String modelCode;

    /** 模型名称 */
    private String modelName;

    /** 能力配置 */
    private String capabilityConfig;

    /** 上下文窗口 */
    private Integer contextWindow;

    /** 是否供应商默认模型 */
    private Boolean providerDefault;

    /** 是否系统默认模型 */
    private Boolean systemDefault;

    /** 启停状态 */
    private Integer status;

    /** 备注 */
    private String remark;

    /** 最后修改时间 */
    private Date updateTime;
}
