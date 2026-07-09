package com.simple.ai.controller.agentRule;

import java.util.Date;

import com.simple.common.core.response.R;

import java.util.Arrays;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springdoc.core.annotations.ParameterObject;
import com.simple.common.auth.client.common.annotation.HasAuthority;
import com.simple.ai.common.dto.agentRule.PageAgentRuleResponse;
import com.simple.ai.common.dto.agentRule.InfoAgentRuleResponse;
import com.simple.ai.common.dto.agentRule.CreateAgentRuleRequest;
import com.simple.ai.common.dto.agentRule.UpdateAgentRuleRequest;
import com.simple.ai.common.dto.agentRule.PageAgentRuleRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import com.simple.common.core.utils.AssertUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import com.simple.ai.common.service.agentRule.AgentRuleService;
import cn.hutool.core.util.ObjUtil;

/**
 * 智能体规则(agent_rule)控制层
 *
 * @author qty
 */
@Slf4j
@Tag(name = "智能体规则")
@RequestMapping("sys/agent-rule")
@RestController
public class AgentRuleController {

    @Autowired
    private AgentRuleService agentRuleService;

    @GetMapping("list")
    @Operation(summary = "分页查询智能体规则")
    @HasAuthority("sys:agent-rule:list")
    public R<IPage<PageAgentRuleResponse>> list(@ParameterObject PageAgentRuleRequest request) {
        return R.ok(agentRuleService.findAll(request));
    }

    @GetMapping("find/{id}")
    @Operation(summary = "查询单个智能体规则")
    @HasAuthority("sys:agent-rule:find")
    public R<InfoAgentRuleResponse> findOne(@PathVariable String id) {
        AssertUtils.notEmpty(id, "主键不能为空");
        return R.ok(agentRuleService.findById(id));
    }

    @PostMapping("create")
    @Operation(summary = "创建智能体规则")
    @HasAuthority("sys:agent-rule:create")
    public R<String> create(@RequestBody @Validated CreateAgentRuleRequest createRequest) {
        return R.ok(agentRuleService.save(createRequest));
    }

    @PutMapping("update/{id}")
    @Operation(summary = "更新单个智能体规则")
    @HasAuthority("sys:agent-rule:update")
    public R<Object> update(@PathVariable String id, @RequestBody @Validated UpdateAgentRuleRequest updateRequest) {
        AssertUtils.isTrue(updateRequest.getId().equals(id), "请求内容的ID与路径ID不同");
        agentRuleService.updateById(updateRequest);
        return R.ok();
    }

    @DeleteMapping("deletes")
    @Transactional
    @Operation(summary = "删除智能体规则")
    @HasAuthority("sys:agent-rule:deletes")
    public R<Object> deleteByIds(@RequestBody List<String> ids) {
        AssertUtils.notEmpty(ids, "主键不能为空");
        agentRuleService.deleteByIds(ids);
        return R.ok();
    }

}

