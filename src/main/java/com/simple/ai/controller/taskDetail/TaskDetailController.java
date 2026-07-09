package com.simple.ai.controller.taskDetail;

import java.util.Date;

import com.simple.common.core.response.R;

import java.util.Arrays;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springdoc.core.annotations.ParameterObject;
import com.simple.common.auth.client.common.annotation.HasAuthority;
import com.simple.ai.common.dto.taskDetail.PageTaskDetailResponse;
import com.simple.ai.common.dto.taskDetail.InfoTaskDetailResponse;
import com.simple.ai.common.dto.taskDetail.CreateTaskDetailRequest;
import com.simple.ai.common.dto.taskDetail.UpdateTaskDetailRequest;
import com.simple.ai.common.dto.taskDetail.PageTaskDetailRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import com.simple.common.core.utils.AssertUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import com.simple.ai.common.service.taskDetail.TaskDetailService;
import cn.hutool.core.util.ObjUtil;

/**
 * 任务详情(task_detail)控制层
 *
 * @author qty
 */
@Slf4j
@Tag(name = "任务详情")
@RequestMapping("sys/task-detail")
@RestController
public class TaskDetailController {

    @Autowired
    private TaskDetailService taskDetailService;

    @GetMapping("list")
    @Operation(summary = "分页查询任务详情")
    @HasAuthority("sys:task-detail:list")
    public R<IPage<PageTaskDetailResponse>> list(@ParameterObject PageTaskDetailRequest request) {
        return R.ok(taskDetailService.findAll(request));
    }

    @GetMapping("find/{id}")
    @Operation(summary = "查询单个任务详情")
    @HasAuthority("sys:task-detail:find")
    public R<InfoTaskDetailResponse> findOne(@PathVariable String id) {
        AssertUtils.notEmpty(id, "主键不能为空");
        return R.ok(taskDetailService.findById(id));
    }

    @PostMapping("create")
    @Operation(summary = "创建任务详情")
    @HasAuthority("sys:task-detail:create")
    public R<String> create(@RequestBody @Validated CreateTaskDetailRequest createRequest) {
        return R.ok(taskDetailService.save(createRequest));
    }

    @PutMapping("update/{id}")
    @Operation(summary = "更新单个任务详情")
    @HasAuthority("sys:task-detail:update")
    public R<Object> update(@PathVariable String id, @RequestBody @Validated UpdateTaskDetailRequest updateRequest) {
        AssertUtils.isTrue(updateRequest.getId().equals(id), "请求内容的ID与路径ID不同");
        taskDetailService.updateById(updateRequest);
        return R.ok();
    }

    @DeleteMapping("deletes")
    @Transactional
    @Operation(summary = "删除任务详情")
    @HasAuthority("sys:task-detail:deletes")
    public R<Object> deleteByIds(@RequestBody List<String> ids) {
        AssertUtils.notEmpty(ids, "主键不能为空");
        taskDetailService.deleteByIds(ids);
        return R.ok();
    }

}

