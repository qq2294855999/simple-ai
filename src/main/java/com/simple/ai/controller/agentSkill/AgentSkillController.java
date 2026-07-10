package com.simple.ai.controller.agentSkill;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.agentSkill.CreateAgentSkillRequest;
import com.simple.ai.common.dto.agentSkill.InfoAgentSkillResponse;
import com.simple.ai.common.dto.agentSkill.PageAgentSkillRequest;
import com.simple.ai.common.dto.agentSkill.PageAgentSkillResponse;
import com.simple.ai.common.dto.agentSkill.PageAggregateAgentSkillRequest;
import com.simple.ai.common.dto.agentSkill.PageAggregateAgentSkillResponse;
import com.simple.ai.common.dto.agentSkill.UpdateAgentSkillRequest;
import com.simple.ai.common.service.agentSkill.AgentSkillService;
import com.simple.common.auth.client.common.annotation.HasAuthority;
import com.simple.common.core.response.R;
import com.simple.common.core.utils.AssertUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    /**
     * 分页查询智能体技能。
     *
     * @param request 分页请求
     * @return 分页数据
     */
    @GetMapping("list")
    @Operation(summary = "分页查询智能体技能")
    @HasAuthority("sys:agent-skill:list")
    public R<IPage<PageAgentSkillResponse>> list(@ParameterObject PageAgentSkillRequest request) {
        return R.ok(agentSkillService.findAll(request));
    }

    /**
     * 聚合分页查询智能体技能。
     *
     * @param request 聚合分页请求
     * @return 聚合分页数据
     */
    @GetMapping("aggregate-list")
    @Operation(summary = "聚合分页查询智能体技能")
    @HasAuthority("sys:agent-skill:aggregate-list")
    public R<IPage<PageAggregateAgentSkillResponse>> aggregateList(@ParameterObject PageAggregateAgentSkillRequest request) {
        return R.ok(agentSkillService.findAggregateAll(request));
    }

    /**
     * 查询单个智能体技能。
     *
     * @param id 主键
     * @return 详情数据
     */
    @GetMapping("find/{id}")
    @Operation(summary = "查询单个智能体技能")
    @HasAuthority("sys:agent-skill:find")
    public R<InfoAgentSkillResponse> findOne(@PathVariable String id) {
        AssertUtils.notEmpty(id, "主键不能为空");
        return R.ok(agentSkillService.findById(id));
    }

    /**
     * 创建智能体技能。
     *
     * @param createRequest 创建请求
     * @return 主键
     */
    @PostMapping("create")
    @Operation(summary = "创建智能体技能")
    @HasAuthority("sys:agent-skill:create")
    public R<String> create(@RequestBody @Validated CreateAgentSkillRequest createRequest) {
        return R.ok(agentSkillService.save(createRequest));
    }

    /**
     * 更新单个智能体技能。
     *
     * @param id 主键
     * @param updateRequest 更新请求
     * @return 空响应
     */
    @PutMapping("update/{id}")
    @Operation(summary = "更新单个智能体技能")
    @HasAuthority("sys:agent-skill:update")
    public R<Object> update(@PathVariable String id, @RequestBody @Validated UpdateAgentSkillRequest updateRequest) {
        AssertUtils.isTrue(updateRequest.getId().equals(id), "请求内容的ID与路径ID不同");
        agentSkillService.updateById(updateRequest);
        return R.ok();
    }

    /**
     * 删除智能体技能。
     *
     * @param ids 主键列表
     * @return 空响应
     */
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


