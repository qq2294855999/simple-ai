package com.simple.ai.controller.subAgentRelation;

import java.util.Date;

import com.simple.common.core.response.R;

import java.util.Arrays;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springdoc.core.annotations.ParameterObject;
import com.simple.common.auth.client.common.annotation.HasAuthority;
import com.simple.ai.common.dto.subAgentRelation.PageSubAgentRelationResponse;
import com.simple.ai.common.dto.subAgentRelation.InfoSubAgentRelationResponse;
import com.simple.ai.common.dto.subAgentRelation.CreateSubAgentRelationRequest;
import com.simple.ai.common.dto.subAgentRelation.UpdateSubAgentRelationRequest;
import com.simple.ai.common.dto.subAgentRelation.PageSubAgentRelationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import com.simple.common.core.utils.AssertUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import com.simple.ai.common.service.subAgentRelation.SubAgentRelationService;
import cn.hutool.core.util.ObjUtil;

/**
 * 子智能体关联(sub_agent_relation)控制层
 *
 * @author qty
 */
@Slf4j
@Tag(name = "子智能体关联")
@RequestMapping("sys/sub-agent-relation")
@RestController
public class SubAgentRelationController {

    @Autowired
    private SubAgentRelationService subAgentRelationService;

    @GetMapping("list")
    @Operation(summary = "分页查询子智能体关联")
    @HasAuthority("sys:sub-agent-relation:list")
    public R<IPage<PageSubAgentRelationResponse>> list(@ParameterObject PageSubAgentRelationRequest request) {
        return R.ok(subAgentRelationService.findAll(request));
    }

    @GetMapping("find/{id}")
    @Operation(summary = "查询单个子智能体关联")
    @HasAuthority("sys:sub-agent-relation:find")
    public R<InfoSubAgentRelationResponse> findOne(@PathVariable String id) {
        AssertUtils.notEmpty(id, "主键不能为空");
        return R.ok(subAgentRelationService.findById(id));
    }

    @PostMapping("create")
    @Operation(summary = "创建子智能体关联")
    @HasAuthority("sys:sub-agent-relation:create")
    public R<String> create(@RequestBody @Validated CreateSubAgentRelationRequest createRequest) {
        return R.ok(subAgentRelationService.save(createRequest));
    }

    @PutMapping("update/{id}")
    @Operation(summary = "更新单个子智能体关联")
    @HasAuthority("sys:sub-agent-relation:update")
    public R<Object> update(@PathVariable String id, @RequestBody @Validated UpdateSubAgentRelationRequest updateRequest) {
        AssertUtils.isTrue(updateRequest.getId().equals(id), "请求内容的ID与路径ID不同");
        subAgentRelationService.updateById(updateRequest);
        return R.ok();
    }

    @DeleteMapping("deletes")
    @Transactional
    @Operation(summary = "删除子智能体关联")
    @HasAuthority("sys:sub-agent-relation:deletes")
    public R<Object> deleteByIds(@RequestBody List<String> ids) {
        AssertUtils.notEmpty(ids, "主键不能为空");
        subAgentRelationService.deleteByIds(ids);
        return R.ok();
    }

}

