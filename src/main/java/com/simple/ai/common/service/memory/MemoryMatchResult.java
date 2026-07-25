package com.simple.ai.common.service.memory;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

/**
 * 记忆匹配结果。
 * <p>包含匹配到的记忆ID和从用户输入中提取的参数值。
 * 当用户通过自然语言发起命令时，AI 不仅识别匹配的记忆，
 * 还从用户输入中提取 params_definition 定义的参数值，
 * 供 MemoryExecutor 替换步骤模板中的占位符。</p>
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
public class MemoryMatchResult {

    /**
     * 匹配的记忆ID，未匹配时为 null
     */
    private String memoryId;

    /**
     * 从用户输入中提取的参数值，key 对应 params_definition 中的参数名
     */
    private Map<String, Object> extractedParams;

    /**
     * 判断是否匹配成功。
     *
     * @return 是否匹配成功
     */
    public boolean isMatched() {
        return memoryId != null && !memoryId.isBlank();
    }
}