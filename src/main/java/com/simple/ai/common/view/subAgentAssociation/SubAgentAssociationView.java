package com.simple.ai.common.view.subAgentAssociation;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.simple.ai.common.entity.subAgentAssociation.SubAgentAssociation;
import com.simple.ai.common.dto.subAgentAssociation.PageSubAgentAssociationRequest;
import com.simple.ai.common.dto.subAgentAssociation.FindOneSubAgentAssociationRequest;
import com.simple.ai.common.dto.subAgentAssociation.FindAllSubAgentAssociationRequest;
import com.simple.ai.common.dto.subAgentAssociation.DeleteSubAgentAssociationRequest;

/**
 * 子智能体关联(sub_agent_association)数据库视图接口
 *
 * @author qty
 */
public interface SubAgentAssociationView {

    /**
     * 分页列表
     *
     * @param pageRequest 分页参数
     * @return 分页数据
     */
    IPage<SubAgentAssociation> findAll(PageSubAgentAssociationRequest pageRequest);

    /**
     * 获取所有数据
     *
     * @param findAllRequest 查询条件
     * @param neRequest      排除条件
     * @return SubAgentAssociation 原始表数据
     */
    List<SubAgentAssociation> findAll(FindAllSubAgentAssociationRequest findAllRequest, FindAllSubAgentAssociationRequest neRequest);

    /**
     * 获取单条数据
     *
     * @param id 主键
     * @return SubAgentAssociation 原始表数据
     */
    SubAgentAssociation findById(String id);

    /**
     * 获取单条数据
     *
     * @param findOneRequest 查询条件
     * @param neRequest      排除条件
     * @return SubAgentAssociation 原始表数据
     */
    SubAgentAssociation findOne(FindOneSubAgentAssociationRequest findOneRequest, FindOneSubAgentAssociationRequest neRequest);

    /**
     * count
     *
     * @param findOneRequest 查询条件
     * @return Long 数据count和
     */
    Long findCount(FindOneSubAgentAssociationRequest findOneRequest, FindOneSubAgentAssociationRequest neRequest);

    /**
     * 新增
     *
     * @param subAgentAssociation 子智能体关联对象
     */
    void save(SubAgentAssociation subAgentAssociation);

    /**
     * 根据id修改
     *
     * @param subAgentAssociation 子智能体关联对象
     */
    void updateById(SubAgentAssociation subAgentAssociation);

    /**
     * 根据id批量修改
     *
     * @param list 对象
     */
    void updateById(List<SubAgentAssociation> list);

    /**
     * 批量新增
     *
     * @param list 对象
     */
    void saves(List<SubAgentAssociation> list);

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
    void delete(DeleteSubAgentAssociationRequest request);

    /**
     * 获取单条数据
     *
     * @param findOneRequest 查询条件
     * @return SubAgentAssociation 原始表数据
     */
    default SubAgentAssociation findOne(FindOneSubAgentAssociationRequest findOneRequest) {
        return findOne(findOneRequest, new FindOneSubAgentAssociationRequest());
    }

    /**
     * 获取所有数据
     *
     * @param findAllRequest 查询条件
     * @return SubAgentAssociation 原始表数据
     */
    default List<SubAgentAssociation> findAll(FindAllSubAgentAssociationRequest findAllRequest) {
        return findAll(findAllRequest, new FindAllSubAgentAssociationRequest());
    }

    /**
     * count
     *
     * @param findOneRequest 查询条件
     * @return Long 数据count和
     */
    default Long findCount(FindOneSubAgentAssociationRequest findOneRequest) {
        return findCount(findOneRequest, new FindOneSubAgentAssociationRequest());
    }

}

