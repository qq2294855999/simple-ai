package com.simple.ai.common.service.protocol;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.protocol.*;

import java.util.List;

/**
 * 执行器协议(agent_protocol)服务接口。
 *
 * @author qty
 */
public interface ProtocolService {

    /**
     * 分页查询执行器协议列表。
     *
     * @param pageRequest 分页查询请求
     * @return 分页结果
     */
    IPage<PageProtocolResponse> findAll(PageProtocolRequest pageRequest);

    /**
     * 根据主键查询执行器协议详情。
     *
     * @param id 主键
     * @return 执行器协议详情
     */
    InfoProtocolResponse findById(String id);

    /**
     * 新增执行器协议。
     *
     * @param createRequest 创建请求
     * @return 主键
     */
    String save(CreateProtocolRequest createRequest);

    /**
     * 更新执行器协议。
     *
     * @param updateRequest 更新请求
     */
    void updateById(UpdateProtocolRequest updateRequest);

    /**
     * 删除执行器协议。
     *
     * @param ids 主键列表
     */
    void deleteByIds(List<String> ids);

    /**
     * 切换执行器协议启用/停用状态。
     * <p>ENABLE 切换为 DISABLE，DISABLE 切换为 ENABLE。</p>
     *
     * @param id 主键
     * @return 切换后的状态
     */
    String toggleStatus(String id);
}