package com.simple.ai.common.view.agentMemoryDetail;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.entity.agentMemoryDetail.AgentMemoryDetail;
import com.simple.ai.common.dto.agentMemoryDetail.PageAgentMemoryDetailRequest;
import com.simple.ai.common.dto.agentMemoryDetail.FindOneAgentMemoryDetailRequest;
import com.simple.ai.common.dto.agentMemoryDetail.FindAllAgentMemoryDetailRequest;
import com.simple.ai.common.dto.agentMemoryDetail.DeleteAgentMemoryDetailRequest;

/**
 * 智能体记忆详情(agent_memory_detail)数据库视图接口
 *
 * @author qty
 */
public interface AgentMemoryDetailView {

    /**
     * 分页列表
     *
     * @param pageRequest 分页参数
     * @return 分页数据
     */
    IPage<AgentMemoryDetail> findAll(PageAgentMemoryDetailRequest pageRequest);

    /**
     * 获取所有数据
     *
     * @param findAllRequest 查询条件
     * @param neRequest      排除条件
     * @return AgentMemoryDetail 原始表数据
     */
    List<AgentMemoryDetail> findAll(FindAllAgentMemoryDetailRequest findAllRequest, FindAllAgentMemoryDetailRequest neRequest);

    /**
     * 获取单条数据
     *
     * @param id 主键
     * @return AgentMemoryDetail 原始表数据
     */
    AgentMemoryDetail findById(String id);

    /**
     * 获取单条数据
     *
     * @param findOneRequest 查询条件
     * @param neRequest      排除条件
     * @return AgentMemoryDetail 原始表数据
     */
    AgentMemoryDetail findOne(FindOneAgentMemoryDetailRequest findOneRequest, FindOneAgentMemoryDetailRequest neRequest);

    /**
     * count
     *
     * @param findOneRequest 查询条件
     * @return Long 数据count和
     */
    Long findCount(FindOneAgentMemoryDetailRequest findOneRequest, FindOneAgentMemoryDetailRequest neRequest);

    /**
     * 新增
     *
     * @param agentMemoryDetail 智能体记忆详情对象
     */
    void save(AgentMemoryDetail agentMemoryDetail);

    /**
     * 根据id修改
     *
     * @param agentMemoryDetail 智能体记忆详情对象
     */
    void updateById(AgentMemoryDetail agentMemoryDetail);

    /**
     * 根据id批量修改
     *
     * @param list 对象
     */
    void updateById(List<AgentMemoryDetail> list);

    /**
     * 批量新增
     *
     * @param list 对象
     */
    void saves(List<AgentMemoryDetail> list);

    /**
     * 删除
     *
     * @param ids 主键
     */
    void deleteByIds(List<String> ids);

    /**
     * 删除
     *
     * @param request 条件
     */
    void delete(DeleteAgentMemoryDetailRequest request);

    /**
     * 获取单条数据
     *
     * @param findOneRequest 查询条件
     * @return AgentMemoryDetail 原始表数据
     */
    default AgentMemoryDetail findOne(FindOneAgentMemoryDetailRequest findOneRequest) {
        return findOne(findOneRequest, new FindOneAgentMemoryDetailRequest());
    }

    /**
     * 获取所有数据
     *
     * @param findAllRequest 查询条件
     * @return AgentMemoryDetail 原始表数据
     */
    default List<AgentMemoryDetail> findAll(FindAllAgentMemoryDetailRequest findAllRequest) {
        return findAll(findAllRequest, new FindAllAgentMemoryDetailRequest());
    }

    /**
     * count
     *
     * @param findOneRequest 查询条件
     * @return Long 数据count和
     */
    default Long findCount(FindOneAgentMemoryDetailRequest findOneRequest) {
        return findCount(findOneRequest, new FindOneAgentMemoryDetailRequest());
    }

}

