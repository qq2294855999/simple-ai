package com.simple.ai.common.service.taskDetail;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.taskDetail.PageTaskDetailResponse;
import com.simple.ai.common.dto.taskDetail.InfoTaskDetailResponse;
import com.simple.ai.common.dto.taskDetail.CreateTaskDetailRequest;
import com.simple.ai.common.dto.taskDetail.UpdateTaskDetailRequest;
import com.simple.ai.common.dto.taskDetail.PageTaskDetailRequest;

/**
 * 任务详情(task_detail)接口
 *
 * @author qty
 */
public interface TaskDetailService {

    /**
     * 分页列表
     *
     * @param pageRequest 请求参数
     * @return 分页数据
     */
    IPage<PageTaskDetailResponse> findAll(PageTaskDetailRequest pageRequest);

    /**
     * 获取单条数据
     *
     * @param id 主键
     * @return TaskDetailFullInfoResponse  任务详情 详细数据
     */
    InfoTaskDetailResponse findById(String id);

    /**
     * 新增
     *
     * @param createRequest 任务详情 请求对象
     */
    String save(CreateTaskDetailRequest createRequest);

    /**
     * 根据主键修改
     *
     * @param updateRequest 任务详情 请求对象
     */
    String updateById(UpdateTaskDetailRequest updateRequest);

    /**
     * 删除
     *
     * @param ids 主键
     */
    void deleteByIds(List<String> ids);
}

