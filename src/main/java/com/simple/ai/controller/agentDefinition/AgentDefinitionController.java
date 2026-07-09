package com.simple.ai.controller.agentDefinition;

import java.util.Date;

import com.simple.common.core.response.R;

import java.util.Arrays;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springdoc.core.annotations.ParameterObject;
import com.simple.common.auth.client.common.annotation.HasAuthority;
import com.simple.ai.common.dto.agentDefinition.PageAgentDefinitionResponse;
import com.simple.ai.common.dto.agentDefinition.InfoAgentDefinitionResponse;
import com.simple.ai.common.dto.agentDefinition.CreateAgentDefinitionRequest;
import com.simple.ai.common.dto.agentDefinition.UpdateAgentDefinitionRequest;
import com.simple.ai.common.dto.agentDefinition.PageAgentDefinitionRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import com.simple.common.core.utils.AssertUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import com.simple.ai.common.service.agentDefinition.AgentDefinitionService;
import cn.hutool.core.util.ObjUtil;

/**
 * 智能体定义(agent_definition)控制层
 *
 * @author qty
 */
@Slf4j
@Tag(name = "智能体定义")
@RequestMapping("sys/agent-definition")
@RestController
public class AgentDefinitionController {

    @Autowired
    private AgentDefinitionService agentDefinitionService;

    @GetMapping("list")
    @Operation(summary = "分页查询智能体定义")
    @HasAuthority("sys:agent-definition:list")
    public R<IPage<PageAgentDefinitionResponse>> list(@ParameterObject PageAgentDefinitionRequest request) {
        return R.ok(agentDefinitionService.findAll(request));
    }

    @GetMapping("find/{id}")
    @Operation(summary = "查询单个智能体定义")
    @HasAuthority("sys:agent-definition:find")
    public R<InfoAgentDefinitionResponse> findOne(@PathVariable String id) {
        AssertUtils.notEmpty(id, "主键不能为空");
        return R.ok(agentDefinitionService.findById(id));
    }

    @PostMapping("create")
    @Operation(summary = "创建智能体定义")
    @HasAuthority("sys:agent-definition:create")
    public R<String> create(@RequestBody @Validated CreateAgentDefinitionRequest createRequest) {
        return R.ok(agentDefinitionService.save(createRequest));
    }

    @PutMapping("update/{id}")
    @Operation(summary = "更新单个智能体定义")
    @HasAuthority("sys:agent-definition:update")
    public R<Object> update(@PathVariable String id, @RequestBody @Validated UpdateAgentDefinitionRequest updateRequest) {
        AssertUtils.isTrue(updateRequest.getId().equals(id), "请求内容的ID与路径ID不同");
        agentDefinitionService.updateById(updateRequest);
        return R.ok();
    }

    @DeleteMapping("deletes")
    @Transactional
    @Operation(summary = "删除智能体定义")
    @HasAuthority("sys:agent-definition:deletes")
    public R<Object> deleteByIds(@RequestBody List<String> ids) {
        AssertUtils.notEmpty(ids, "主键不能为空");
        agentDefinitionService.deleteByIds(ids);
        return R.ok();
    }

}

