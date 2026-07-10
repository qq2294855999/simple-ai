package com.simple.ai.controller.atomicCommand;

import java.util.Date;

import com.simple.common.core.response.R;

import java.util.Arrays;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springdoc.core.annotations.ParameterObject;
import com.simple.common.auth.client.common.annotation.HasAuthority;
import com.simple.ai.common.dto.atomicCommand.PageAtomicCommandResponse;
import com.simple.ai.common.dto.atomicCommand.InfoAtomicCommandResponse;
import com.simple.ai.common.dto.atomicCommand.CreateAtomicCommandRequest;
import com.simple.ai.common.dto.atomicCommand.UpdateAtomicCommandRequest;
import com.simple.ai.common.dto.atomicCommand.PageAtomicCommandRequest;
import com.simple.ai.common.dto.atomicCommand.PageAggregateAtomicCommandRequest;
import com.simple.ai.common.dto.atomicCommand.PageAggregateAtomicCommandResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import com.simple.common.core.utils.AssertUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import com.simple.ai.common.service.atomicCommand.AtomicCommandService;
import cn.hutool.core.util.ObjUtil;

/**
 * 原子命令(atomic_command)控制层
 *
 * @author qty
 */
@Slf4j
@Tag(name = "原子命令")
@RequestMapping("sys/atomic-command")
@RestController
public class AtomicCommandController {

    @Autowired
    private AtomicCommandService atomicCommandService;

    @GetMapping("list")
    @Operation(summary = "分页查询原子命令")
    @HasAuthority("sys:atomic-command:list")
    public R<IPage<PageAtomicCommandResponse>> list(@ParameterObject PageAtomicCommandRequest request) {
        return R.ok(atomicCommandService.findAll(request));
    }

    /**
     * 聚合分页查询原子命令。
     *
     * @param request 聚合分页请求
     * @return 聚合分页数据
     */
    @GetMapping("aggregate-list")
    @Operation(summary = "聚合分页查询原子命令")
    @HasAuthority("sys:atomic-command:aggregate-list")
    public R<IPage<PageAggregateAtomicCommandResponse>> aggregateList(@ParameterObject PageAggregateAtomicCommandRequest request) {
        return R.ok(atomicCommandService.findAggregateAll(request));
    }

    @GetMapping("find/{id}")
    @Operation(summary = "查询单个原子命令")
    @HasAuthority("sys:atomic-command:find")
    public R<InfoAtomicCommandResponse> findOne(@PathVariable String id) {
        AssertUtils.notEmpty(id, "主键不能为空");
        return R.ok(atomicCommandService.findById(id));
    }

    @PostMapping("create")
    @Operation(summary = "创建原子命令")
    @HasAuthority("sys:atomic-command:create")
    public R<String> create(@RequestBody @Validated CreateAtomicCommandRequest createRequest) {
        return R.ok(atomicCommandService.save(createRequest));
    }

    @PutMapping("update/{id}")
    @Operation(summary = "更新单个原子命令")
    @HasAuthority("sys:atomic-command:update")
    public R<Object> update(@PathVariable String id, @RequestBody @Validated UpdateAtomicCommandRequest updateRequest) {
        AssertUtils.isTrue(updateRequest.getId().equals(id), "请求内容的ID与路径ID不同");
        atomicCommandService.updateById(updateRequest);
        return R.ok();
    }

    @DeleteMapping("deletes")
    @Transactional
    @Operation(summary = "删除原子命令")
    @HasAuthority("sys:atomic-command:deletes")
    public R<Object> deleteByIds(@RequestBody List<String> ids) {
        AssertUtils.notEmpty(ids, "主键不能为空");
        atomicCommandService.deleteByIds(ids);
        return R.ok();
    }

}

