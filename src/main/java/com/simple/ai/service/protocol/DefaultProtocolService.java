package com.simple.ai.service.protocol;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.copy.protocol.ProtocolCopyMapper;
import com.simple.ai.common.dto.protocol.*;
import com.simple.ai.common.entity.protocol.Protocol;
import com.simple.ai.common.service.protocol.ProtocolService;
import com.simple.ai.common.view.protocol.ProtocolView;
import com.simple.common.auth.client.util.LoginUserUtils;
import com.simple.common.core.utils.AssertUtils;
import com.simple.common.mp.common.enums.Status;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 执行器协议(agent_protocol)服务默认实现。
 *
 * @author qty
 */
@Slf4j
@Service
@Transactional
class DefaultProtocolService implements ProtocolService {

    @Autowired
    private ProtocolView protocolView;

    @Autowired
    private ProtocolCopyMapper copy;

    @Override
    public IPage<PageProtocolResponse> findAll(PageProtocolRequest pageRequest) {

        // 构建分页对象
        Page<PageProtocolResponse> page = pageRequest.getPage(PageProtocolResponse.class);

        // 执行分页查询
        List<PageProtocolResponse> records = protocolView.findAll(pageRequest, page);
        page.setRecords(records);

        return page;
    }

    @Override
    public InfoProtocolResponse findById(String id) {

        // 查询并校验执行器协议存在
        Protocol entity = protocolView.findById(id);
        AssertUtils.notEmpty(entity, "执行器协议[{}]不存在", id);

        return copy.toInfoResponse(entity);
    }

    @Override
    public String save(CreateProtocolRequest createRequest) {

        // 构建实体并保存
        Protocol entity = copy.toEntity(createRequest);
        entity.setCreateUserId(LoginUserUtils.getUserTemporary().getUserId());
        entity.setStatus(Status.ON);
        protocolView.save(entity);
        log.info("执行器协议创建成功，id={}", entity.getId());

        return entity.getId();
    }

    @Override
    public void updateById(UpdateProtocolRequest updateRequest) {

        // 查询并校验执行器协议存在
        Protocol entity = protocolView.findById(updateRequest.getId());
        AssertUtils.notEmpty(entity, "执行器协议[{}]不存在", updateRequest.getId());

        // 更新执行器协议
        Protocol updatedEntity = copy.toEntity(updateRequest);
        protocolView.updateById(updatedEntity);
        log.info("执行器协议更新成功，id={}", updateRequest.getId());
    }

    @Override
    public void deleteByIds(List<String> ids) {

        // 参数校验：主键列表不能为空
        AssertUtils.notEmpty(ids, "主键列表不能为空");

        // 批量删除
        for (String id : ids) {
            protocolView.deleteById(id);
        }
        log.info("执行器协议删除成功，ids={}", ids);
    }

    @Override
    public String toggleStatus(String id) {

        // 查询并校验执行器协议存在
        Protocol entity = protocolView.findById(id);
        AssertUtils.notEmpty(entity, "执行器协议[{}]不存在", id);

        // 根据当前状态切换：ON ↔ OFF
        Status currentStatus = entity.getStatus();
        Status newStatus = Status.ON.equals(currentStatus) ? Status.OFF : Status.ON;
        entity.setStatus(newStatus);
        protocolView.updateById(entity);
        log.info("执行器协议状态切换成功，id={}，新状态={}", id, newStatus);

        return newStatus.name();
    }
}