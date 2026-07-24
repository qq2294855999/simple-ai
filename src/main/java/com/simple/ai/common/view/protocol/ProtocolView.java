package com.simple.ai.common.view.protocol;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.dto.protocol.PageProtocolRequest;
import com.simple.ai.common.dto.protocol.PageProtocolResponse;
import com.simple.ai.common.entity.protocol.Protocol;

import java.util.List;

/**
 * 执行器协议(agent_protocol)数据库视图接口。
 *
 * @author qty
 */
public interface ProtocolView {

    /**
     * 分页查询执行器协议列表。
     *
     * @param pageRequest 分页查询请求
     * @param page        MyBatis-Plus 分页对象
     * @return 分页结果
     */
    List<PageProtocolResponse> findAll(PageProtocolRequest pageRequest, Page<PageProtocolResponse> page);

    /**
     * 按主键查询执行器协议。
     *
     * @param id 主键
     * @return 执行器协议实体，不存在返回 null
     */
    Protocol findById(String id);

    /**
     * 保存执行器协议。
     *
     * @param entity 执行器协议实体
     */
    void save(Protocol entity);

    /**
     * 按主键更新执行器协议。
     *
     * @param entity 执行器协议实体
     */
    void updateById(Protocol entity);

    /**
     * 按主键删除执行器协议。
     *
     * @param id 主键
     */
    void deleteById(String id);
}