package com.simple.ai.controller.agentMemoryDetail;

import java.util.Date;

import com.simple.common.core.response.R;

import java.util.Arrays;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springdoc.core.annotations.ParameterObject;
import com.simple.common.auth.client.common.annotation.HasAuthority;
import com.simple.ai.common.dto.agentMemoryDetail.PageAgentMemoryDetailResponse;
import com.simple.ai.common.dto.agentMemoryDetail.InfoAgentMemoryDetailResponse;
import com.simple.ai.common.dto.agentMemoryDetail.CreateAgentMemoryDetailRequest;
import com.simple.ai.common.dto.agentMemoryDetail.UpdateAgentMemoryDetailRequest;
import com.simple.ai.common.dto.agentMemoryDetail.PageAgentMemoryDetailRequest;
import com.simple.ai.common.dto.agentMemoryDetail.PageAggregateAgentMemoryDetailRequest;
import com.simple.ai.common.dto.agentMemoryDetail.PageAggregateAgentMemoryDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import com.simple.common.core.utils.AssertUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import com.simple.ai.common.service.agentMemoryDetail.AgentMemoryDetailService;
import cn.hutool.core.util.ObjUtil;

/**
 * 智能体记忆详情(agent_memory_detail)控制层
 *
 * @author qty
 */
@Slf4j
@Tag(name = "智能体记忆详情")
@RequestMapping("sys/agent-memory-detail")
@RestController
public class AgentMemoryDetailController {

    @Autowired
    private AgentMemoryDetailService agentMemoryDetailService;

    @GetMapping("list")
    @Operation(summary = "分页查询智能体记忆详情")
    @HasAuthority("sys:agent-memory-detail:list")
    public R<IPage<PageAgentMemoryDetailResponse>> list(@ParameterObject PageAgentMemoryDetailRequest request) {
        return R.ok(agentMemoryDetailService.findAll(request));
    }

    /**
     * 聚合分页查询智能体记忆详情。
     *
     * @param request 聚合分页请求
     * @return 聚合分页数据
     */
    @GetMapping("aggregate-list")
    @Operation(summary = "聚合分页查询智能体记忆详情")
    @HasAuthority("sys:agent-memory-detail:aggregate-list")
    public R<IPage<PageAggregateAgentMemoryDetailResponse>> aggregateList(@ParameterObject PageAggregateAgentMemoryDetailRequest request) {
        return R.ok(agentMemoryDetailService.findAggregateAll(request));
    }

    @GetMapping("find/{id}")
    @Operation(summary = "查询单个智能体记忆详情")
    @HasAuthority("sys:agent-memory-detail:find")
    public R<InfoAgentMemoryDetailResponse> findOne(@PathVariable String id) {
        AssertUtils.notEmpty(id, "主键不能为空");
        return R.ok(agentMemoryDetailService.findById(id));
    }

    @PostMapping("create")
    @Operation(summary = "创建智能体记忆详情")
    @HasAuthority("sys:agent-memory-detail:create")
    public R<String> create(@RequestBody @Validated CreateAgentMemoryDetailRequest createRequest) {
        return R.ok(agentMemoryDetailService.save(createRequest));
    }

    @PutMapping("update/{id}")
    @Operation(summary = "更新单个智能体记忆详情")
    @HasAuthority("sys:agent-memory-detail:update")
    public R<Object> update(@PathVariable String id, @RequestBody @Validated UpdateAgentMemoryDetailRequest updateRequest) {
        AssertUtils.isTrue(updateRequest.getId().equals(id), "请求内容的ID与路径ID不同");
        agentMemoryDetailService.updateById(updateRequest);
        return R.ok();
    }

    @DeleteMapping("deletes")
    @Transactional
    @Operation(summary = "删除智能体记忆详情")
    @HasAuthority("sys:agent-memory-detail:deletes")
    public R<Object> deleteByIds(@RequestBody List<String> ids) {
        AssertUtils.notEmpty(ids, "主键不能为空");
        agentMemoryDetailService.deleteByIds(ids);
        return R.ok();
    }

}

