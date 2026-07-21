package com.simple.ai.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI 模型供应商协议类型。
 *
 * <p>数据库存储枚举 name()，MyBatis-Plus 默认行为。</p>
 *
 * @author qty
 */
@Getter
@AllArgsConstructor
public enum AiModelProviderProtocolProcess {

    /** OpenAI 兼容协议 */
    OPENAI_COMPATIBLE("OpenAI兼容");

    /**
     * 中文说明
     */
    private final String label;
}
