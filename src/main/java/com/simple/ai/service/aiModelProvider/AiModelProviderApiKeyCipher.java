package com.simple.ai.service.aiModelProvider;

import com.simple.common.core.utils.AssertUtils;
import com.simple.common.core.utils.CryptoUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * AI 模型供应商 API Key 密文保护器。
 *
 * @author qty
 */
@Component
public class AiModelProviderApiKeyCipher {

    /** 由环境变量 SIMPLE_AI_MODEL_ENCRYPTION_KEY 注入的 Base64 密钥 */
    @Value("${simple.ai.model-encryption-key:}")
    private String encryptionKey;

    /**
     * 加密 API Key。
     *
     * @param apiKey API Key 明文
     * @return AES-GCM 密文
     */
    public String encrypt(String apiKey) {
        AssertUtils.notEmpty(apiKey, "API Key不能为空");
        return CryptoUtil.encryptStr(CryptoUtil.SymmetricAlgorithmType.AES_GCM, requireEncryptionKey(), apiKey);
    }

    /**
     * 解密 API Key。
     *
     * @param ciphertext AES-GCM 密文
     * @return API Key 明文
     */
    public String decrypt(String ciphertext) {
        AssertUtils.notEmpty(ciphertext, "API Key密文不能为空");
        return CryptoUtil.decryptStr(CryptoUtil.SymmetricAlgorithmType.AES_GCM, requireEncryptionKey(), ciphertext);
    }

    /**
     * 获取运行时密钥。
     *
     * @return Base64 编码的数据密钥
     */
    private String requireEncryptionKey() {
        AssertUtils.notEmpty(encryptionKey, "未配置AI模型供应商加密密钥");
        return encryptionKey;
    }
}
