package com.simple.ai.controller.agentSkill;

import java.util.Date;

import com.simple.common.core.response.R;

import java.util.Arrays;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springdoc.core.annotations.ParameterObject;
import com.simple.common.auth.client.common.annotation.HasAuthority;
import com.simple.ai.common.dto.agentSkill.PageAgentSkillResponse;
import com.simple.ai.common.dto.agentSkill.InfoAgentSkillResponse;
import com.simple.ai.common.dto.agentSkill.CreateAgentSkillRequest;
import com.simple.ai.common.dto.agentSkill.UpdateAgentSkillRequest;
import com.simple.ai.common.dto.agentSkill.PageAgentSkillRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import com.simple.common.core.utils.AssertUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import com.simple.ai.common.service.agentSkill.AgentSkillService;
import cn.hutool.core.util.ObjUtil;

/**
 * 智能体技能(agent_skill)控制层
 *
 * @author qty
 */
@Slf4j
@Tag(name = "智能体技能")
@RequestMapping("sys/agent-skill")
@RestController
public class AgentSkillController {

    @Autowired
    private AgentSkillService agentSkillService;

    @GetMapping("list")
    @Operation(summary = "分页查询智能体技能")
    @HasAuthority("sys:agent-skill:list")
    public R<IPage<PageAgentSkillResponse>> list(@ParameterObject PageAgentSkillRequest request) {
        return R.ok(agentSkillService.findAll(request));
    }

    @GetMapping("find/{id}")
    @Operation(summary = "查询单个智能体技能")
    @HasAuthority("sys:agent-skill:find")
    public R<InfoAgentSkillResponse> findOne(@PathVariable String id) {
        AssertUtils.notEmpty(id, "主键不能为空");
        return R.ok(agentSkillService.findById(id));
    }

    @PostMapping("create")
    @Operation(summary = "创建智能体技能")
    @HasAuthority("sys:agent-skill:create")
    public R<String> create(@RequestBody @Validated CreateAgentSkillRequest createRequest) {
        return R.ok(agentSkillService.save(createRequest));
    }

    @PutMapping("update/{id}")
    @Operation(summary = "更新单个智能体技能")
    @HasAuthority("sys:agent-skill:update")
    public R<Object> update(@PathVariable String id, @RequestBody @Validated UpdateAgentSkillRequest updateRequest) {
        AssertUtils.isTrue(updateRequest.getId().equals(id), "请求内容的ID与路径ID不同");
        agentSkillService.updateById(updateRequest);
        return R.ok();
    }

    @DeleteMapping("deletes")
    @Transactional
    @Operation(summary = "删除智能体技能")
    @HasAuthority("sys:agent-skill:deletes")
    public R<Object> deleteByIds(@RequestBody List<String> ids) {
        AssertUtils.notEmpty(ids, "主键不能为空");
        agentSkillService.deleteByIds(ids);
        return R.ok();
    }

}

