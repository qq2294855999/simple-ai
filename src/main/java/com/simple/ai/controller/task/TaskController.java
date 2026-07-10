package com.simple.ai.controller.task;

import java.util.Date;

import com.simple.common.core.response.R;

import java.util.Arrays;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springdoc.core.annotations.ParameterObject;
import com.simple.common.auth.client.common.annotation.HasAuthority;
import com.simple.ai.common.dto.task.PageTaskResponse;
import com.simple.ai.common.dto.task.InfoTaskResponse;
import com.simple.ai.common.dto.task.CreateTaskRequest;
import com.simple.ai.common.dto.task.UpdateTaskRequest;
import com.simple.ai.common.dto.task.PageTaskRequest;
import com.simple.ai.common.dto.task.PageAggregateTaskRequest;
import com.simple.ai.common.dto.task.PageAggregateTaskResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import com.simple.common.core.utils.AssertUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import com.simple.ai.common.service.task.TaskService;
import cn.hutool.core.util.ObjUtil;

/**
 * 任务(task)控制层
 *
 * @author qty
 */
@Slf4j
@Tag(name = "任务")
@RequestMapping("sys/task")
@RestController
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping("list")
    @Operation(summary = "分页查询任务")
    @HasAuthority("sys:task:list")
    public R<IPage<PageTaskResponse>> list(@ParameterObject PageTaskRequest request) {
        return R.ok(taskService.findAll(request));
    }

    /**
     * 聚合分页查询任务。
     *
     * @param request 聚合分页请求
     * @return 聚合分页数据
     */
    @GetMapping("aggregate-list")
    @Operation(summary = "聚合分页查询任务")
    @HasAuthority("sys:task:aggregate-list")
    public R<IPage<PageAggregateTaskResponse>> aggregateList(@ParameterObject PageAggregateTaskRequest request) {
        return R.ok(taskService.findAggregateAll(request));
    }

    @GetMapping("find/{id}")
    @Operation(summary = "查询单个任务")
    @HasAuthority("sys:task:find")
    public R<InfoTaskResponse> findOne(@PathVariable String id) {
        AssertUtils.notEmpty(id, "主键不能为空");
        return R.ok(taskService.findById(id));
    }

    @PostMapping("create")
    @Operation(summary = "创建任务")
    @HasAuthority("sys:task:create")
    public R<String> create(@RequestBody @Validated CreateTaskRequest createRequest) {
        return R.ok(taskService.save(createRequest));
    }

    @PutMapping("update/{id}")
    @Operation(summary = "更新单个任务")
    @HasAuthority("sys:task:update")
    public R<Object> update(@PathVariable String id, @RequestBody @Validated UpdateTaskRequest updateRequest) {
        AssertUtils.isTrue(updateRequest.getId().equals(id), "请求内容的ID与路径ID不同");
        taskService.updateById(updateRequest);
        return R.ok();
    }

    @DeleteMapping("deletes")
    @Transactional
    @Operation(summary = "删除任务")
    @HasAuthority("sys:task:deletes")
    public R<Object> deleteByIds(@RequestBody List<String> ids) {
        AssertUtils.notEmpty(ids, "主键不能为空");
        taskService.deleteByIds(ids);
        return R.ok();
    }

}

