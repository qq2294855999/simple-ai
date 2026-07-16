package com.simple.ai.controller.agentRule;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.agentRule.CreateAgentRuleRequest;
import com.simple.ai.common.dto.agentRule.InfoAgentRuleResponse;
import com.simple.ai.common.dto.agentRule.PageAgentRuleRequest;
import com.simple.ai.common.dto.agentRule.PageAgentRuleResponse;
import com.simple.ai.common.dto.agentRule.PageAggregateAgentRuleRequest;
import com.simple.ai.common.dto.agentRule.PageAggregateAgentRuleResponse;
import com.simple.ai.common.dto.agentRule.UpdateAgentRuleRequest;
import com.simple.ai.common.service.agentRule.AgentRuleService;
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

    /**
     * 分页查询智能体规则。
     *
     * @param request 分页请求
     * @return 分页数据
     */
    @GetMapping("list")
    @Operation(summary = "分页查询智能体规则")
    @HasAuthority("sys:agent-rule:list")
    public R<IPage<PageAgentRuleResponse>> list(@ParameterObject PageAgentRuleRequest request) {
        return R.ok(agentRuleService.findAll(request));
    }

    /**
     * 聚合分页查询智能体规则。
     *
     * @param request 聚合分页请求
     * @return 聚合分页数据
     */
    @GetMapping("aggregate-list")
    @Operation(summary = "聚合分页查询智能体规则")
    @HasAuthority("sys:agent-rule:aggregate-list")
    public R<IPage<PageAggregateAgentRuleResponse>> aggregateList(@ParameterObject PageAggregateAgentRuleRequest request) {
        return R.ok(agentRuleService.findAggregateAll(request));
    }

    /**
     * 查询单个智能体规则。
     *
     * @param id 主键
     * @return 详情数据
     */
    @GetMapping("find/{id}")
    @Operation(summary = "查询单个智能体规则")
    @HasAuthority("sys:agent-rule:find")
    public R<InfoAgentRuleResponse> findOne(@PathVariable String id) {
        AssertUtils.notEmpty(id, "主键不能为空");
        return R.ok(agentRuleService.findById(id));
    }

    /**
     * 创建智能体规则。
     *
     * @param createRequest 创建请求
     * @return 主键
     */
    @PostMapping("create")
    @Operation(summary = "创建智能体规则")
    @HasAuthority("sys:agent-rule:create")
    public R<String> create(@RequestBody @Validated CreateAgentRuleRequest createRequest) {
        return R.ok(agentRuleService.save(createRequest));
    }

    /**
     * 更新单个智能体规则。
     *
     * @param id 主键
     * @param updateRequest 更新请求
     * @return 空响应
     */
    @PutMapping("update/{id}")
    @Operation(summary = "更新单个智能体规则")
    @HasAuthority("sys:agent-rule:update")
    public R<Object> update(@PathVariable String id, @RequestBody @Validated UpdateAgentRuleRequest updateRequest) {
        AssertUtils.isTrue(updateRequest.getId().equals(id), "请求内容的ID与路径ID不同");
        agentRuleService.updateById(updateRequest);
        return R.ok();
    }

    /**
     * 删除智能体规则。
     *
     * @param ids 主键列表
     * @return 空响应
     */
    @DeleteMapping("deletes")
    @Transactional
    @Operation(summary = "删除智能体规则")
    @HasAuthority("sys:agent-rule:deletes")
    public R<Object> deleteByIds(@RequestBody List<String> ids) {
        AssertUtils.notEmpty(ids, "主键不能为空");
        agentRuleService.deleteByIds(ids);
        return R.ok();
    }

    /**
     * 启用智能体规则。
     *
     * @param id 主键
     * @return 空响应
     */
    @PutMapping("enable/{id}")
    @Operation(summary = "启用智能体规则")
    @HasAuthority("sys:agent-rule:enable")
    public R<Object> enable(@PathVariable String id) {
        AssertUtils.notEmpty(id, "主键不能为空");
        agentRuleService.enableStatus(id);
        return R.ok();
    }

    /**
     * 禁用智能体规则。
     *
     * @param id 主键
     * @return 空响应
     */
    @PutMapping("disable/{id}")
    @Operation(summary = "禁用智能体规则")
    @HasAuthority("sys:agent-rule:disable")
    public R<Object> disable(@PathVariable String id) {
        AssertUtils.notEmpty(id, "主键不能为空");
        agentRuleService.disableStatus(id);
        return R.ok();
    }
}


