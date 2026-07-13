package com.simple.ai.common.dto.aiModel;

import lombok.Data;

/**
 * 已解析的 AI 模型运行时配置。
 *
 * <p>该对象仅限服务端调用链局部使用，禁止作为接口响应、日志内容或审计数据序列化。</p>
 *
 * @author qty
 */
@Data
public class AiModelRuntimeConfig {

    /** 供应商主键快照 */
    private String providerId;

    /** 供应商名称快照 */
    private String providerName;

    /** 模型主键快照 */
    private String modelId;

    /** 模型编码快照 */
    private String modelCode;

    /** OpenAI-compatible 服务地址 */
    private String baseUrl;

    /** 调用超时毫秒数 */
    private Integer timeoutMillis;

    /** 仅供当前调用使用的 API Key 明文 */
    private String apiKey;
}
