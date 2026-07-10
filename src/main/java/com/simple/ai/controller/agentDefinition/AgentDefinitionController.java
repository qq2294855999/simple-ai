package com.simple.ai.controller.agentDefinition;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.agentDefinition.CreateAgentDefinitionRequest;
import com.simple.ai.common.dto.agentDefinition.DeleteCascadeAgentDefinitionResponse;
import com.simple.ai.common.dto.agentDefinition.InfoAgentDefinitionResponse;
import com.simple.ai.common.dto.agentDefinition.InfoAggregateAgentDefinitionResponse;
import com.simple.ai.common.dto.agentDefinition.PageAgentDefinitionRequest;
import com.simple.ai.common.dto.agentDefinition.PageAgentDefinitionResponse;
import com.simple.ai.common.dto.agentDefinition.PageAggregateAgentDefinitionRequest;
import com.simple.ai.common.dto.agentDefinition.PageAggregateAgentDefinitionResponse;
import com.simple.ai.common.dto.agentDefinition.UpdateAgentDefinitionRequest;
import com.simple.ai.common.service.agentDefinition.AgentDefinitionService;
import com.simple.common.auth.client.common.annotation.HasAuthority;
import com.simple.common.core.response.R;
import com.simple.common.core.utils.AssertUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
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
 * 智能体定义(agent_definition)控制层
 *
 * @author qty
 */
@Tag(name = "智能体定义")
@RequestMapping("sys/agent-definition")
@RestController
public class AgentDefinitionController {

    @Autowired
    private AgentDefinitionService agentDefinitionService;

    /**
     * 分页查询智能体定义。
     *
     * @param request 分页请求
     * @return 智能体定义分页数据
     */
    @GetMapping("list")
    @Operation(summary = "分页查询智能体定义")
    @HasAuthority("sys:agent-definition:list")
    public R<IPage<PageAgentDefinitionResponse>> list(@ParameterObject PageAgentDefinitionRequest request) {
        return R.ok(agentDefinitionService.findAll(request));
    }

    /**
     * 聚合分页查询智能体定义。
     *
     * @param request 聚合分页请求
     * @return 智能体定义聚合分页数据
     */
    @GetMapping("aggregate-list")
    @Operation(summary = "聚合分页查询智能体定义")
    @HasAuthority("sys:agent-definition:aggregate-list")
    public R<IPage<PageAggregateAgentDefinitionResponse>> aggregateList(@ParameterObject PageAggregateAgentDefinitionRequest request) {
        return R.ok(agentDefinitionService.findAggregateAll(request));
    }

    /**
     * 查询单个智能体定义。
     *
     * @param id 主键
     * @return 智能体定义详情
     */
    @GetMapping("find/{id}")
    @Operation(summary = "查询单个智能体定义")
    @HasAuthority("sys:agent-definition:find")
    public R<InfoAgentDefinitionResponse> findOne(@PathVariable String id) {
        AssertUtils.notEmpty(id, "主键不能为空");
        return R.ok(agentDefinitionService.findById(id));
    }

    /**
     * 查询智能体聚合详情。
     *
     * @param id 主键
     * @return 智能体聚合详情
     */
    @GetMapping("aggregate-find/{id}")
    @Operation(summary = "查询智能体聚合详情")
    @HasAuthority("sys:agent-definition:aggregate-find")
    public R<InfoAggregateAgentDefinitionResponse> aggregateFindOne(@PathVariable String id) {
        AssertUtils.notEmpty(id, "主键不能为空");
        return R.ok(agentDefinitionService.findAggregateById(id));
    }

    /**
     * 创建智能体定义。
     *
     * @param createRequest 创建请求
     * @return 主键
     */
    @PostMapping("create")
    @Operation(summary = "创建智能体定义")
    @HasAuthority("sys:agent-definition:create")
    public R<String> create(@RequestBody @Validated CreateAgentDefinitionRequest createRequest) {
        return R.ok(agentDefinitionService.save(createRequest));
    }

    /**
     * 更新单个智能体定义。
     *
     * @param id 主键
     * @param updateRequest 更新请求
     * @return 空响应
     */
    @PutMapping("update/{id}")
    @Operation(summary = "更新单个智能体定义")
    @HasAuthority("sys:agent-definition:update")
    public R<Object> update(@PathVariable String id, @RequestBody @Validated UpdateAgentDefinitionRequest updateRequest) {
        AssertUtils.isTrue(updateRequest.getId().equals(id), "请求内容的ID与路径ID不同");
        agentDefinitionService.updateById(updateRequest);
        return R.ok();
    }

    /**
     * 删除智能体定义。
     *
     * @param ids 主键列表
     * @return 空响应
     */
    @DeleteMapping("deletes")
    @Operation(summary = "删除智能体定义")
    @HasAuthority("sys:agent-definition:deletes")
    public R<Object> deleteByIds(@RequestBody List<String> ids) {
        AssertUtils.notEmpty(ids, "主键不能为空");
        agentDefinitionService.deleteByIds(ids);
        return R.ok();
    }

    /**
     * 级联删除智能体定义。
     *
     * @param ids 主键列表
     * @return 删除影响统计
     */
    @DeleteMapping("cascade-deletes")
    @Operation(summary = "级联删除智能体定义")
    @HasAuthority("sys:agent-definition:cascade-deletes")
    public R<DeleteCascadeAgentDefinitionResponse> cascadeDeleteByIds(@RequestBody List<String> ids) {
        AssertUtils.notEmpty(ids, "主键不能为空");
        return R.ok(agentDefinitionService.deleteCascadeByIds(ids));
    }
}
