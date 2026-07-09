package com.simple.ai.controller.agentMemory;

import java.util.Date;

import com.simple.common.core.response.R;

import java.util.Arrays;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springdoc.core.annotations.ParameterObject;
import com.simple.common.auth.client.common.annotation.HasAuthority;
import com.simple.ai.common.dto.agentMemory.PageAgentMemoryResponse;
import com.simple.ai.common.dto.agentMemory.InfoAgentMemoryResponse;
import com.simple.ai.common.dto.agentMemory.CreateAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemory.UpdateAgentMemoryRequest;
import com.simple.ai.common.dto.agentMemory.PageAgentMemoryRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import com.simple.common.core.utils.AssertUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import com.simple.ai.common.service.agentMemory.AgentMemoryService;
import cn.hutool.core.util.ObjUtil;

/**
 * 智能体记忆(agent_memory)控制层
 *
 * @author qty
 */
@Slf4j
@Tag(name = "智能体记忆")
@RequestMapping("sys/agent-memory")
@RestController
public class AgentMemoryController {

    @Autowired
    private AgentMemoryService agentMemoryService;

    @GetMapping("list")
    @Operation(summary = "分页查询智能体记忆")
    @HasAuthority("sys:agent-memory:list")
    public R<IPage<PageAgentMemoryResponse>> list(@ParameterObject PageAgentMemoryRequest request) {
        return R.ok(agentMemoryService.findAll(request));
    }

    @GetMapping("find/{id}")
    @Operation(summary = "查询单个智能体记忆")
    @HasAuthority("sys:agent-memory:find")
    public R<InfoAgentMemoryResponse> findOne(@PathVariable String id) {
        AssertUtils.notEmpty(id, "主键不能为空");
        return R.ok(agentMemoryService.findById(id));
    }

    @PostMapping("create")
    @Operation(summary = "创建智能体记忆")
    @HasAuthority("sys:agent-memory:create")
    public R<String> create(@RequestBody @Validated CreateAgentMemoryRequest createRequest) {
        return R.ok(agentMemoryService.save(createRequest));
    }

    @PutMapping("update/{id}")
    @Operation(summary = "更新单个智能体记忆")
    @HasAuthority("sys:agent-memory:update")
    public R<Object> update(@PathVariable String id, @RequestBody @Validated UpdateAgentMemoryRequest updateRequest) {
        AssertUtils.isTrue(updateRequest.getId().equals(id), "请求内容的ID与路径ID不同");
        agentMemoryService.updateById(updateRequest);
        return R.ok();
    }

    @DeleteMapping("deletes")
    @Transactional
    @Operation(summary = "删除智能体记忆")
    @HasAuthority("sys:agent-memory:deletes")
    public R<Object> deleteByIds(@RequestBody List<String> ids) {
        AssertUtils.notEmpty(ids, "主键不能为空");
        agentMemoryService.deleteByIds(ids);
        return R.ok();
    }

}

