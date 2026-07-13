package com.simple.ai.service.aiModelProvider;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * AI 模型供应商密钥密文保护器安全测试。
 *
 * @author qty
 */
class AiModelProviderApiKeyCipherTest {

    /**
     * 验证缺少密钥时加密直接失败。
     */
    @Test
    void encryptShouldFailWhenKeyMissing() {
        AiModelProviderApiKeyCipher cipher = new AiModelProviderApiKeyCipher();

        // 不注入密钥

        assertThrows(RuntimeException.class,
                () -> cipher.encrypt("some-key"));
    }

    /**
     * 验证缺少密钥时解密直接失败。
     */
    @Test
    void decryptShouldFailWhenKeyMissing() {
        AiModelProviderApiKeyCipher cipher = new AiModelProviderApiKeyCipher();

        // 不注入密钥

        assertThrows(RuntimeException.class,
                () -> cipher.decrypt("some-ciphertext"));
    }

    /**
     * 验证空输入被拒绝。
     */
    @Test
    void encryptShouldRejectEmptyInput() {
        AiModelProviderApiKeyCipher cipher = new AiModelProviderApiKeyCipher();
        ReflectionTestUtils.setField(cipher, "encryptionKey",
                "YWJjZGVmZ2hpamtsbW5vcDEyMzQ1Ng==");

        assertThrows(RuntimeException.class, () -> cipher.encrypt(""));
    }

    /**
     * 验证解密空输入被拒绝。
     */
    @Test
    void decryptShouldRejectEmptyInput() {
        AiModelProviderApiKeyCipher cipher = new AiModelProviderApiKeyCipher();
        ReflectionTestUtils.setField(cipher, "encryptionKey",
                "YWJjZGVmZ2hpamtsbW5vcDEyMzQ1Ng==");

        assertThrows(RuntimeException.class, () -> cipher.decrypt(""));
    }

    /**
     * 验证缺少密钥时空字符串也被拒绝（密钥检查先于内容检查）。
     */
    @Test
    void encryptShouldFailWhenKeyMissingEvenWithEmptyInput() {
        AiModelProviderApiKeyCipher cipher = new AiModelProviderApiKeyCipher();

        assertThrows(RuntimeException.class,
                () -> cipher.encrypt(""));
    }

    /**
     * 验证解密缺少密钥时空输入也被拒绝。
     */
    @Test
    void decryptShouldFailWhenKeyMissingEvenWithEmptyInput() {
        AiModelProviderApiKeyCipher cipher = new AiModelProviderApiKeyCipher();

        assertThrows(RuntimeException.class,
                () -> cipher.decrypt(""));
    }
}
