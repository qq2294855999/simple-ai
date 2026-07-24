package com.simple.ai.view.protocol;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.copy.protocol.ProtocolCopyMapper;
import com.simple.ai.common.dto.protocol.PageProtocolRequest;
import com.simple.ai.common.dto.protocol.PageProtocolResponse;
import com.simple.ai.common.entity.protocol.Protocol;
import com.simple.ai.common.view.protocol.ProtocolView;
import com.simple.common.mp.common.enums.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 执行器协议(agent_protocol)数据库视图实现。
 *
 * @author qty
 */
@Component
class MPProtocolView implements ProtocolView {

    @Autowired
    private ProtocolRepository repository;

    @Autowired
    private ProtocolCopyMapper copyMapper;

    @Override
    public List<PageProtocolResponse> findAll(PageProtocolRequest pageRequest, Page<PageProtocolResponse> page) {

        // 状态字符串转枚举：LambdaQueryWrapper 通过 @EnumValue 自动映射为 int code
        Status statusEnum = StringUtils.hasText(pageRequest.getStatus()) ? Status.valueOf(pageRequest.getStatus()) : null;

        // 构建 LambdaQueryWrapper，使用条件式链式调用
        LambdaQueryWrapper<Protocol> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(pageRequest.getProtocolCode()), Protocol::getProtocolCode, pageRequest.getProtocolCode())
               .like(StringUtils.hasText(pageRequest.getProtocolName()), Protocol::getProtocolName, pageRequest.getProtocolName())
               .like(StringUtils.hasText(pageRequest.getProtocolVersion()), Protocol::getProtocolVersion, pageRequest.getProtocolVersion())
               .eq(statusEnum != null, Protocol::getStatus, statusEnum)
               .orderByDesc(Protocol::getCreateTime);

        // 执行分页查询并转换结果
        Page<Protocol> entityPage = repository.selectPage(new Page<>(page.getCurrent(), page.getSize()), wrapper);
        List<PageProtocolResponse> responseList = copyMapper.toPageResponseList(entityPage.getRecords());

        // 同步分页信息
        page.setRecords(responseList);
        page.setTotal(entityPage.getTotal());

        return responseList;
    }

    @Override
    public Protocol findById(String id) {
        return repository.selectById(id);
    }

    @Override
    public void save(Protocol entity) {
        repository.insert(entity);
    }

    @Override
    public void updateById(Protocol entity) {
        repository.updateById(entity);
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }
}