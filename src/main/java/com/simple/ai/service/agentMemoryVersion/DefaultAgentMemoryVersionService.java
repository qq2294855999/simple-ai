package com.simple.ai.service.agentMemoryVersion;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.copy.agentMemoryVersion.AgentMemoryVersionCopyMapper;
import com.simple.ai.common.dto.agentMemoryVersion.*;
import com.simple.ai.common.entity.agentMemoryVersion.AgentMemoryVersion;
import com.simple.ai.common.enums.AgentMemoryVersionStatusProcess;
import com.simple.ai.common.service.agentMemoryVersion.AgentMemoryVersionService;
import com.simple.ai.common.view.agentMemoryVersion.AgentMemoryVersionView;
import com.simple.common.core.utils.AssertUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 记忆版本(agent_memory_version)服务默认实现。
 * <p>管理记忆版本状态流转：草稿→已发布→已退役。</p>
 *
 * @author qty
 */
@Slf4j
@Service
@Transactional
class DefaultAgentMemoryVersionService implements AgentMemoryVersionService {

    @Autowired
    private AgentMemoryVersionView agentMemoryVersionView;

    @Autowired
    private AgentMemoryVersionCopyMapper copy;

    @Override
    public IPage<PageAgentMemoryVersionResponse> findAll(PageAgentMemoryVersionRequest pageRequest) {

        // 构建分页对象
        Page<PageAgentMemoryVersionResponse> page = pageRequest.getPage(PageAgentMemoryVersionResponse.class);

        // 执行分页查询
        List<PageAgentMemoryVersionResponse> records = agentMemoryVersionView.findAll(pageRequest, page);
        page.setRecords(records);

        return page;
    }

    @Override
    public InfoAgentMemoryVersionResponse findById(String id) {

        // 查询并校验记忆版本存在
        AgentMemoryVersion entity = agentMemoryVersionView.findById(id);
        AssertUtils.notEmpty(entity, "记忆版本[{}]不存在", id);

        return copy.toInfoResponse(entity);
    }

    @Override
    public String save(CreateAgentMemoryVersionRequest createRequest) {

        // 参数校验：记忆ID不能为空
        AssertUtils.notEmpty(createRequest.getMemoryId(), "记忆ID不能为空");

        // 参数校验：版本号不能为空
        AssertUtils.notNull(createRequest.getVersionNo(), "版本号不能为空");

        // 构建实体并保存
        AgentMemoryVersion entity = copy.toEntity(createRequest);
        entity.setVersionStatus(AgentMemoryVersionStatusProcess.DRAFT);
        agentMemoryVersionView.save(entity);
        log.info("记忆版本创建成功，id={}", entity.getId());

        return entity.getId();
    }

    @Override
    public void updateById(UpdateAgentMemoryVersionRequest updateRequest) {

        // 查询并校验记忆版本存在
        AgentMemoryVersion entity = agentMemoryVersionView.findById(updateRequest.getId());
        AssertUtils.notEmpty(entity, "记忆版本[{}]不存在", updateRequest.getId());

        // 不允许修改已发布或已退役的版本
        AssertUtils.isTrue(entity.getVersionStatus() == AgentMemoryVersionStatusProcess.DRAFT, "仅草稿状态可修改，当前状态[{}]", entity.getVersionStatus().getLabel());

        // 更新记忆版本
        AgentMemoryVersion updatedEntity = copy.toEntity(updateRequest);
        agentMemoryVersionView.updateById(updatedEntity);
        log.info("记忆版本更新成功，id={}", updateRequest.getId());
    }

    @Override
    public void deleteByIds(List<String> ids) {

        // 参数校验：主键列表不能为空
        AssertUtils.notEmpty(ids, "主键列表不能为空");

        // 批量删除
        for (String id : ids) {
            agentMemoryVersionView.deleteById(id);
        }
        log.info("记忆版本删除成功，ids={}", ids);
    }

    @Override
    public void publish(String id) {

        // 查询并校验记忆版本存在
        AgentMemoryVersion entity = agentMemoryVersionView.findById(id);
        AssertUtils.notEmpty(entity, "记忆版本[{}]不存在", id);

        // 校验当前状态为草稿
        AssertUtils.isTrue(entity.getVersionStatus() == AgentMemoryVersionStatusProcess.DRAFT, "仅草稿状态可发布，当前状态[{}]", entity.getVersionStatus().getLabel());

        // 变更为已发布
        entity.setVersionStatus(AgentMemoryVersionStatusProcess.PUBLISHED);
        agentMemoryVersionView.updateById(entity);
        log.info("记忆版本发布成功，id={}", id);
    }

    @Override
    public void retire(String id) {

        // 查询并校验记忆版本存在
        AgentMemoryVersion entity = agentMemoryVersionView.findById(id);
        AssertUtils.notEmpty(entity, "记忆版本[{}]不存在", id);

        // 校验当前状态为已发布
        AssertUtils.isTrue(entity.getVersionStatus() == AgentMemoryVersionStatusProcess.PUBLISHED, "仅已发布状态可退役，当前状态[{}]", entity.getVersionStatus().getLabel());

        // 变更为已退役
        entity.setVersionStatus(AgentMemoryVersionStatusProcess.RETIRED);
        agentMemoryVersionView.updateById(entity);
        log.info("记忆版本退役成功，id={}", id);
    }
}
