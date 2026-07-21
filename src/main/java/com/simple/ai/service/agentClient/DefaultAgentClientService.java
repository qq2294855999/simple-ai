package com.simple.ai.service.agentClient;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.copy.agentClient.AgentClientCopyMapper;
import com.simple.ai.common.dto.agentClient.*;
import com.simple.ai.common.entity.agentClient.AgentClient;
import com.simple.ai.common.enums.AgentClientStatusProcess;
import com.simple.ai.common.service.agentClient.AgentClientService;
import com.simple.ai.common.view.agentClient.AgentClientView;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.core.utils.CryptoUtil;
import com.simple.common.core.utils.IdUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 客户端实例(agent_client)服务默认实现。
 * <p>创建客户端时自动生成随机密钥（UUID），BCrypt 哈希后落库，明文仅创建时返回一次。
 * 过期时间支持 DAY / WEEK / MONTH / YEAR 四种单位。</p>
 *
 * @author qty
 */
@Slf4j
@Service
@Transactional
class DefaultAgentClientService implements AgentClientService {

    @Autowired
    private AgentClientView agentClientView;

    @Autowired
    private AgentClientCopyMapper copy;

    @Override
    public IPage<PageAgentClientResponse> findAll(PageAgentClientRequest pageRequest) {

        // 构建分页对象
        Page<PageAgentClientResponse> page = pageRequest.getPage(PageAgentClientResponse.class);

        // 执行分页查询
        List<PageAgentClientResponse> records = agentClientView.findAll(pageRequest, page);
        page.setRecords(records);

        return page;
    }

    @Override
    public InfoAgentClientResponse findById(String id) {

        // 查询并校验客户端实例存在
        AgentClient entity = agentClientView.findById(id);
        AssertUtils.notEmpty(entity, "客户端实例[{}]不存在", id);

        return copy.toInfoResponse(entity);
    }

    @Override
    public InfoAgentClientResponse save(CreateAgentClientRequest createRequest, String userId) {

        // 参数校验：客户端名称不能为空
        AssertUtils.notEmpty(createRequest.getClientName(), "客户端名称不能为空");

        // 参数校验：执行器ID不能为空
        AssertUtils.notEmpty(createRequest.getExecutorId(), "执行器ID不能为空");

        // 生成随机密钥明文
        String plainSecret = generateSecret();

        // 计算过期时间
        Date expireTime = calculateExpireTime(createRequest.getExpireDuration(), createRequest.getExpireUnit());

        // 构建客户端实体
        AgentClient entity = buildClientEntity(createRequest, plainSecret, expireTime, userId);

        // 持久化客户端实例
        agentClientView.save(entity);
        log.info("客户端实例创建成功，id={}，userId={}", entity.getId(), userId);

        // 构建响应，包含仅返回一次的明文密钥
        InfoAgentClientResponse response = copy.toInfoResponse(entity);
        response.setClientSecret(plainSecret);

        return response;
    }

    @Override
    public void updateById(UpdateAgentClientRequest updateRequest) {

        // 查询并校验客户端实例存在
        AgentClient entity = agentClientView.findById(updateRequest.getId());
        AssertUtils.notEmpty(entity, "客户端实例[{}]不存在", updateRequest.getId());

        // 更新客户端实例
        AgentClient updatedEntity = copy.toEntity(updateRequest);
        agentClientView.updateById(updatedEntity);
        log.info("客户端实例更新成功，id={}", updateRequest.getId());
    }

    @Override
    public void deleteByIds(List<String> ids) {

        // 参数校验：主键列表不能为空
        AssertUtils.notEmpty(ids, "主键列表不能为空");

        // 批量删除
        for (String id : ids) {
            agentClientView.deleteById(id);
        }
        log.info("客户端实例删除成功，ids={}", ids);
    }

    /**
     * 生成随机密钥。
     * <p>使用 UUID 去横线作为客户端密钥。</p>
     *
     * @return 密钥明文
     */
    private String generateSecret() {
        return IdUtils.getFastSimpleUUID();
    }

    /**
     * 计算过期时间。
     *
     * @param duration 过期时长数字
     * @param unit     过期时间单位（DAY / WEEK / MONTH / YEAR）
     * @return 过期时间，未指定时返回 null
     */
    private Date calculateExpireTime(Integer duration, String unit) {

        // 未指定过期时长时返回永不过期
        if (duration == null || unit == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();

        // 按时间单位累加
        switch (unit.toUpperCase()) {
        case "DAY":
            calendar.add(Calendar.DAY_OF_YEAR, duration);
            break;
        case "WEEK":
            calendar.add(Calendar.WEEK_OF_YEAR, duration);
            break;
        case "MONTH":
            calendar.add(Calendar.MONTH, duration);
            break;
        case "YEAR":
            calendar.add(Calendar.YEAR, duration);
            break;
        default:
            AssertUtils.error("不支持的过期时间单位[{}]", unit);
        }

        return calendar.getTime();
    }

    /**
     * 构建客户端实体。
     *
     * @param request     创建请求
     * @param plainSecret 密钥明文
     * @param expireTime  过期时间
     * @return 客户端实体
     */
    private AgentClient buildClientEntity(CreateAgentClientRequest request, String plainSecret, Date expireTime, String userId) {

        // BCrypt 哈希密钥
        String secretHash = CryptoUtil.hashPassword(plainSecret);

        AgentClient entity = copy.toEntity(request);
        entity.setClientSecretHash(secretHash);
        entity.setStatus(AgentClientStatusProcess.ACTIVE);
        entity.setExpireTime(expireTime);
        entity.setUserId(userId);
        entity.setCreateUserId(userId);

        return entity;
    }
}
