package com.simple.ai.service.aiModelProvider;

import com.simple.ai.common.dto.aiModelProvider.AiModelProviderResponse;
import com.simple.ai.common.dto.aiModelProvider.AiModelProviderSaveRequest;
import com.simple.ai.common.entity.aiModelProvider.AiModelProvider;
import com.simple.ai.common.enums.AiModelProviderProtocolProcess;
import com.simple.ai.common.service.aiModelProvider.AiModelProviderService;
import com.simple.ai.common.view.aiModel.AiModelView;
import com.simple.ai.common.view.aiModelProvider.AiModelProviderView;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.mp.common.enums.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 模型供应商管理服务默认实现。
 *
 * @author qty
 */
@Service
@Transactional
class DefaultAiModelProviderService implements AiModelProviderService {

    @Autowired
    private AiModelProviderView aiModelProviderView;

    @Autowired
    private AiModelView aiModelView;

    @Autowired
    private AiModelProviderApiKeyCipher apiKeyCipher;

    @Override
    public List<AiModelProviderResponse> findAll() {
        List<AiModelProvider> providers = aiModelProviderView.findAll();
        return buildResponses(providers);
    }

    @Override
    public String save(AiModelProviderSaveRequest request) {
        AssertUtils.notEmpty(request, "供应商保存请求不能为空");
        validateProtocol(request);
        validateTimeout(request);
        validateProviderCodeUnique(request);

        // 创建与编辑按主键分流处理
        if (request.getId() == null || request.getId().isBlank()) {
            return createProvider(request);
        }
        return updateProvider(request);
    }

    @Override
    public void deleteById(String id) {
        AssertUtils.notEmpty(id, "供应商主键不能为空");
        AiModelProvider provider = loadProvider(id);

        // 供应商存在模型时必须保留配置与审计关联
        AssertUtils.isTrue(aiModelView.findAllByProviderId(provider.getId()).isEmpty(), "供应商存在关联模型，请先停用模型");
        aiModelProviderView.deleteById(provider.getId());
    }

    /**
     * 创建供应商配置。
     *
     * @param request 保存请求
     * @return 供应商主键
     */
    private String createProvider(AiModelProviderSaveRequest request) {
        AssertUtils.notEmpty(request.getApiKey(), "创建供应商时API Key不能为空");
        AiModelProvider provider = buildProvider(request);
        provider.setApiKeyCiphertext(apiKeyCipher.encrypt(request.getApiKey()));
        aiModelProviderView.save(provider);
        return provider.getId();
    }

    /**
     * 更新供应商配置。
     *
     * @param request 保存请求
     * @return 供应商主键
     */
    private String updateProvider(AiModelProviderSaveRequest request) {
        AiModelProvider provider = loadProvider(request.getId());
        fillProvider(provider, request);

        // 非空 API Key 才替换数据库密文
        if (request.getApiKey() != null && !request.getApiKey().isBlank()) {
            provider.setApiKeyCiphertext(apiKeyCipher.encrypt(request.getApiKey()));
        }
        aiModelProviderView.updateById(provider);
        return provider.getId();
    }

    /**
     * 校验供应商协议。
     *
     * @param request 保存请求
     */
    private void validateProtocol(AiModelProviderSaveRequest request) {
        AssertUtils.isTrue(AiModelProviderProtocolProcess.OPENAI_COMPATIBLE.name().equals(request.getProtocolType()), "仅支持OPENAI_COMPATIBLE协议");
    }

    /**
     * 校验调用超时。
     *
     * @param request 保存请求
     */
    private void validateTimeout(AiModelProviderSaveRequest request) {
        Integer timeoutMillis = request.getTimeoutMillis();
        AssertUtils.isTrue(timeoutMillis != null && timeoutMillis > 0, "调用超时必须大于0");
    }

    /**
     * 校验供应商编码唯一性。
     *
     * @param request 保存请求
     */
    private void validateProviderCodeUnique(AiModelProviderSaveRequest request) {
        AiModelProvider existsProvider = aiModelProviderView.findByProviderCode(request.getProviderCode());
        if (existsProvider == null) {
            return;
        }
        AssertUtils.isTrue(existsProvider.getId().equals(request.getId()), "供应商编码[{}]已存在", request.getProviderCode());
    }

    /**
     * 加载供应商。
     *
     * @param id 供应商主键
     * @return 供应商配置
     */
    private AiModelProvider loadProvider(String id) {
        AiModelProvider provider = aiModelProviderView.findById(id);
        AssertUtils.notEmpty(provider, "供应商[{}]不存在", id);
        return provider;
    }

    /**
     * 构建供应商实体。
     *
     * @param request 保存请求
     * @return 供应商实体
     */
    private AiModelProvider buildProvider(AiModelProviderSaveRequest request) {
        AiModelProvider provider = new AiModelProvider();
        fillProvider(provider, request);
        return provider;
    }

    /**
     * 回填供应商可编辑字段。
     *
     * @param provider 供应商实体
     * @param request 保存请求
     */
    private void fillProvider(AiModelProvider provider, AiModelProviderSaveRequest request) {
        provider.setProviderCode(request.getProviderCode());
        provider.setProviderName(request.getProviderName());
        provider.setProtocolType(request.getProtocolType());
        provider.setBaseUrl(request.getBaseUrl());
        provider.setTimeoutMillis(request.getTimeoutMillis());
        provider.setSystemDefault(Boolean.TRUE.equals(request.getSystemDefault()) ? 1 : 0);
        provider.setStatus(resolveStatus(request.getStatus()));
        provider.setRemark(request.getRemark());
    }

    /**
     * 转换启停状态。
     *
     * @param status 状态数值
     * @return 框架状态枚举
     */
    private Status resolveStatus(Integer status) {
        if (status == null || status == Status.ON.getCode()) {
            return Status.ON;
        }
        AssertUtils.isTrue(status.equals(Status.OFF.getCode()), "状态值不合法");
        return Status.OFF;
    }

    /**
     * 组装安全供应商响应。
     *
     * @param providers 供应商实体列表
     * @return 安全供应商响应列表
     */
    private List<AiModelProviderResponse> buildResponses(List<AiModelProvider> providers) {
        List<AiModelProviderResponse> responses = new ArrayList<>();

        // 转换供应商列表，避免向外传递密文字段
        for (AiModelProvider provider : providers) {
            responses.add(buildResponse(provider));
        }
        return responses;
    }

    /**
     * 组装单个安全供应商响应。
     *
     * @param provider 供应商实体
     * @return 安全供应商响应
     */
    private AiModelProviderResponse buildResponse(AiModelProvider provider) {
        AiModelProviderResponse response = new AiModelProviderResponse();
        response.setId(provider.getId());
        response.setProviderCode(provider.getProviderCode());
        response.setProviderName(provider.getProviderName());
        response.setProtocolType(provider.getProtocolType());
        response.setBaseUrl(provider.getBaseUrl());
        response.setApiKeyConfigured(provider.getApiKeyCiphertext() != null && !provider.getApiKeyCiphertext().isBlank());
        response.setTimeoutMillis(provider.getTimeoutMillis());
        response.setSystemDefault(provider.getSystemDefault() != null && provider.getSystemDefault() == 1);
        response.setStatus(provider.getStatus().getCode());
        response.setRemark(provider.getRemark());
        response.setUpdateTime(provider.getUpdateTime());
        return response;
    }
}
