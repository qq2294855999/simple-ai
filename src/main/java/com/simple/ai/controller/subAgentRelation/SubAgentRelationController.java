package com.simple.ai.controller.subAgentRelation;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.subAgentRelation.CreateSubAgentRelationRequest;
import com.simple.ai.common.dto.subAgentRelation.InfoSubAgentRelationResponse;
import com.simple.ai.common.dto.subAgentRelation.PageAggregateSubAgentRelationRequest;
import com.simple.ai.common.dto.subAgentRelation.PageAggregateSubAgentRelationResponse;
import com.simple.ai.common.dto.subAgentRelation.PageSubAgentRelationRequest;
import com.simple.ai.common.dto.subAgentRelation.PageSubAgentRelationResponse;
import com.simple.ai.common.dto.subAgentRelation.UpdateSubAgentRelationRequest;
import com.simple.ai.common.service.subAgentRelation.SubAgentRelationService;
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

    /**
     * 分页查询子智能体关联。
     *
     * @param request 分页请求
     * @return 分页数据
     */
    @GetMapping("list")
    @Operation(summary = "分页查询子智能体关联")
    @HasAuthority("sys:sub-agent-relation:list")
    public R<IPage<PageSubAgentRelationResponse>> list(@ParameterObject PageSubAgentRelationRequest request) {
        return R.ok(subAgentRelationService.findAll(request));
    }

    /**
     * 聚合分页查询子智能体关联。
     *
     * @param request 聚合分页请求
     * @return 聚合分页数据
     */
    @GetMapping("aggregate-list")
    @Operation(summary = "聚合分页查询子智能体关联")
    @HasAuthority("sys:sub-agent-relation:aggregate-list")
    public R<IPage<PageAggregateSubAgentRelationResponse>> aggregateList(@ParameterObject PageAggregateSubAgentRelationRequest request) {
        return R.ok(subAgentRelationService.findAggregateAll(request));
    }

    /**
     * 查询单个子智能体关联。
     *
     * @param id 主键
     * @return 详情数据
     */
    @GetMapping("find/{id}")
    @Operation(summary = "查询单个子智能体关联")
    @HasAuthority("sys:sub-agent-relation:find")
    public R<InfoSubAgentRelationResponse> findOne(@PathVariable String id) {
        AssertUtils.notEmpty(id, "主键不能为空");
        return R.ok(subAgentRelationService.findById(id));
    }

    /**
     * 创建子智能体关联。
     *
     * @param createRequest 创建请求
     * @return 主键
     */
    @PostMapping("create")
    @Operation(summary = "创建子智能体关联")
    @HasAuthority("sys:sub-agent-relation:create")
    public R<String> create(@RequestBody @Validated CreateSubAgentRelationRequest createRequest) {
        return R.ok(subAgentRelationService.save(createRequest));
    }

    /**
     * 更新单个子智能体关联。
     *
     * @param id 主键
     * @param updateRequest 更新请求
     * @return 空响应
     */
    @PutMapping("update/{id}")
    @Operation(summary = "更新单个子智能体关联")
    @HasAuthority("sys:sub-agent-relation:update")
    public R<Object> update(@PathVariable String id, @RequestBody @Validated UpdateSubAgentRelationRequest updateRequest) {
        AssertUtils.isTrue(updateRequest.getId().equals(id), "请求内容的ID与路径ID不同");
        subAgentRelationService.updateById(updateRequest);
        return R.ok();
    }

    /**
     * 删除子智能体关联。
     *
     * @param ids 主键列表
     * @return 空响应
     */
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


