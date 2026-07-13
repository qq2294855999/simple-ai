package com.simple.ai.service.aiModel;

import com.simple.ai.common.entity.agentDefinition.AgentDefinition;
import com.simple.ai.common.entity.aiModel.AiModel;
import com.simple.ai.common.entity.aiModelProvider.AiModelProvider;
import com.simple.ai.common.view.agentDefinition.AgentDefinitionView;
import com.simple.ai.common.view.aiModel.AiModelView;
import com.simple.ai.common.view.aiModelProvider.AiModelProviderView;
import com.simple.ai.service.aiModelProvider.AiModelProviderApiKeyCipher;
import com.simple.common.mp.common.enums.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AI 模型路由服务单元测试。
 *
 * @author qty
 */
@ExtendWith(MockitoExtension.class)
class DefaultAiModelRoutingServiceTest {

    @InjectMocks
    private DefaultAiModelRoutingService routingService;

    @Mock
    private AiModelView aiModelView;

    @Mock
    private AiModelProviderView aiModelProviderView;

    @Mock
    private AgentDefinitionView agentDefinitionView;

    @Mock
    private AiModelProviderApiKeyCipher apiKeyCipher;

    /** 系统默认模型 */
    private AiModel systemDefaultModel;

    /** 智能体默认模型 */
    private AiModel agentDefaultModel;

    /** 显式模型 */
    private AiModel explicitModel;

    /** 已启用供应商 */
    private AiModelProvider enabledProvider;

    /**
     * 初始化测试替身。
     */
    @BeforeEach
    void setUp() {

        // 构建已启用供应商
        enabledProvider = new AiModelProvider();
        enabledProvider.setId("provider-1");
        enabledProvider.setProviderName("测试供应商");
        enabledProvider.setProtocolType("OPENAI_COMPATIBLE");
        enabledProvider.setBaseUrl("https://test.example.com");
        enabledProvider.setTimeoutMillis(30000);
        enabledProvider.setApiKeyCiphertext("encrypted-key");
        enabledProvider.setStatus(Status.ON);

        // 构建系统默认模型
        systemDefaultModel = buildModel("model-sys-default", "sys-model",
                "provider-1", Status.ON, null, 1);

        // 构建智能体默认模型
        agentDefaultModel = buildModel("model-agent-default", "agent-model",
                "provider-1", Status.ON, null, 0);

        // 构建显式模型
        explicitModel = buildModel("model-explicit", "explicit-model",
                "provider-1", Status.ON, null, 0);
    }

    /**
     * 验证显式模型优先级最高。
     */
    @Test
    void resolveShouldUseExplicitModelWhenProvided() {
        when(aiModelView.findById("model-explicit")).thenReturn(explicitModel);
        when(aiModelProviderView.findById("provider-1")).thenReturn(enabledProvider);
        when(apiKeyCipher.decrypt(anyString())).thenReturn("plain-text-key");

        routingService.resolve("model-explicit", "agent-1");

        // 显式模型传入时不应查询智能体默认模型
        verify(agentDefinitionView, never()).findById(anyString());

        // 不应查询系统默认模型
        verify(aiModelView, never()).findSystemDefault();
    }

    /**
     * 验证智能体默认模型在无显式模型时被使用。
     */
    @Test
    void resolveShouldUseAgentDefaultWhenNoExplicitModel() {
        AgentDefinition agent = new AgentDefinition();
        agent.setId("agent-1");
        agent.setDefaultModelId("model-agent-default");

        when(agentDefinitionView.findById("agent-1")).thenReturn(agent);
        when(aiModelView.findById("model-agent-default")).thenReturn(agentDefaultModel);
        when(aiModelProviderView.findById("provider-1")).thenReturn(enabledProvider);
        when(apiKeyCipher.decrypt(anyString())).thenReturn("plain-text-key");

        routingService.resolve(null, "agent-1");

        // 不应查询系统默认模型
        verify(aiModelView, never()).findSystemDefault();
    }

    /**
     * 验证系统默认模型兜底。
     */
    @Test
    void resolveShouldUseSystemDefaultWhenNoAgentDefault() {
        AgentDefinition agent = new AgentDefinition();
        agent.setId("agent-1");

        // 智能体无默认模型
        agent.setDefaultModelId(null);

        when(agentDefinitionView.findById("agent-1")).thenReturn(agent);
        when(aiModelView.findSystemDefault()).thenReturn(systemDefaultModel);
        when(aiModelView.findById("model-sys-default")).thenReturn(systemDefaultModel);
        when(aiModelProviderView.findById("provider-1")).thenReturn(enabledProvider);
        when(apiKeyCipher.decrypt(anyString())).thenReturn("plain-text-key");

        routingService.resolve(null, "agent-1");
    }

    /**
     * 验证无任何模型时直接失败。
     */
    @Test
    void resolveShouldFailWhenNoModelAvailable() {
        AgentDefinition agent = new AgentDefinition();
        agent.setId("agent-1");
        agent.setDefaultModelId(null);

        when(agentDefinitionView.findById("agent-1")).thenReturn(agent);
        when(aiModelView.findSystemDefault()).thenReturn(null);

        assertThrows(RuntimeException.class, () -> routingService.resolve(null, "agent-1"));
    }

    /**
     * 验证已停用模型被拒绝。
     */
    @Test
    void resolveShouldRejectDisabledModel() {
        AiModel disabledModel = buildModel("model-disabled", "disabled-model",
                "provider-1", Status.OFF, null, 0);

        when(aiModelView.findById("model-disabled")).thenReturn(disabledModel);

        assertThrows(RuntimeException.class, () -> routingService.resolve("model-disabled", "agent-1"));
    }

    /**
     * 验证已停用供应商被拒绝。
     */
    @Test
    void resolveShouldRejectDisabledProvider() {
        AiModelProvider disabledProvider = new AiModelProvider();
        disabledProvider.setId("provider-1");
        disabledProvider.setProviderName("已停用供应商");
        disabledProvider.setProtocolType("OPENAI_COMPATIBLE");
        disabledProvider.setStatus(Status.OFF);

        when(aiModelView.findById("model-explicit")).thenReturn(explicitModel);
        when(aiModelProviderView.findById("provider-1")).thenReturn(disabledProvider);

        assertThrows(RuntimeException.class, () -> routingService.resolve("model-explicit", "agent-1"));
    }

    /**
     * 验证非 OpenAI 协议被拒绝。
     */
    @Test
    void resolveShouldRejectNonOpenAiProtocol() {
        AiModelProvider nonCompatibleProvider = new AiModelProvider();
        nonCompatibleProvider.setId("provider-1");
        nonCompatibleProvider.setProviderName("非兼容供应商");
        nonCompatibleProvider.setProtocolType("CUSTOM");
        nonCompatibleProvider.setStatus(Status.ON);

        when(aiModelView.findById("model-explicit")).thenReturn(explicitModel);
        when(aiModelProviderView.findById("provider-1")).thenReturn(nonCompatibleProvider);

        assertThrows(RuntimeException.class, () -> routingService.resolve("model-explicit", "agent-1"));
    }

    /**
     * 构建测试模型。
     *
     * @param id 模型主键
     * @param modelCode 模型编码
     * @param providerId 供应商主键
     * @param status 启停状态
     * @param providerDefault 是否供应商默认
     * @param systemDefault 是否系统默认
     * @return 模型实体
     */
    private AiModel buildModel(String id, String modelCode, String providerId,
                               Status status, Integer providerDefault, Integer systemDefault) {
        AiModel model = new AiModel();
        model.setId(id);
        model.setModelCode(modelCode);
        model.setModelName("模型" + modelCode);
        model.setProviderId(providerId);
        model.setStatus(status);
        model.setProviderDefault(providerDefault);
        model.setSystemDefault(systemDefault);
        return model;
    }
}
