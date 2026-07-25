package com.simple.ai.controller.agentMemory;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.agentMemory.*;
import com.simple.ai.common.service.agentMemory.AgentMemoryService;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * 分页查询智能体记忆
     *
     * @param request 分页请求
     * @return 分页数据
     */
    @GetMapping("list")
    @Operation(summary = "分页查询智能体记忆")
    @HasAuthority("sys:agent-memory:list")
    public R<IPage<PageAgentMemoryResponse>> list(@ParameterObject PageAgentMemoryRequest request) {
        return R.ok(agentMemoryService.findAll(request));
    }

    /**
     * 查询单个智能体记忆
     *
     * @param id 主键
     * @return 记忆详情
     */
    @GetMapping("find/{id}")
    @Operation(summary = "查询单个智能体记忆")
    @HasAuthority("sys:agent-memory:find")
    public R<InfoAgentMemoryResponse> findOne(@PathVariable String id) {
        AssertUtils.notEmpty(id, "主键不能为空");
        return R.ok(agentMemoryService.findById(id));
    }

    /**
     * 创建智能体记忆
     *
     * @param createRequest 创建请求
     * @return 新增记录主键
     */
    @PostMapping("create")
    @Operation(summary = "创建智能体记忆")
    @HasAuthority("sys:agent-memory:create")
    public R<String> create(@RequestBody @Validated CreateAgentMemoryRequest createRequest) {
        return R.ok(agentMemoryService.save(createRequest));
    }

    /**
     * 更新智能体记忆
     *
     * @param id            主键
     * @param updateRequest 修改请求
     * @return 修改记录主键
     */
    @PutMapping("update/{id}")
    @Operation(summary = "更新智能体记忆")
    @HasAuthority("sys:agent-memory:update")
    public R<String> update(@PathVariable String id, @RequestBody @Validated UpdateAgentMemoryRequest updateRequest) {
        AssertUtils.isTrue(updateRequest.getId().equals(id), "请求内容的ID与路径ID不同");
        return R.ok(agentMemoryService.updateById(updateRequest));
    }

    /**
     * 删除智能体记忆
     *
     * @param ids 主键列表
     * @return 空响应
     */
    @DeleteMapping("deletes")
    @Transactional
    @Operation(summary = "删除智能体记忆")
    @HasAuthority("sys:agent-memory:deletes")
    public R<Object> deleteByIds(@RequestBody List<String> ids) {
        AssertUtils.notEmpty(ids, "主键不能为空");
        agentMemoryService.deleteByIds(ids);
        return R.ok();
    }

    /**
     * 发布记忆版本
     *
     * @param id 记忆ID
     * @return 空响应
     */
    @PutMapping("publish/{id}")
    @Operation(summary = "发布记忆版本")
    @HasAuthority("sys:agent-memory:publish")
    public R<Object> publish(@PathVariable String id) {
        AssertUtils.notEmpty(id, "主键不能为空");
        agentMemoryService.publish(id);
        return R.ok();
    }

    /**
     * 退役记忆版本
     *
     * @param id 记忆ID
     * @return 空响应
     */
    @PutMapping("retire/{id}")
    @Operation(summary = "退役记忆版本")
    @HasAuthority("sys:agent-memory:retire")
    public R<Object> retire(@PathVariable String id) {
        AssertUtils.notEmpty(id, "主键不能为空");
        agentMemoryService.retire(id);
        return R.ok();
    }

    /**
     * 获取记忆的参数定义。
     * <p>返回参数定义JSON和步骤列表，用于前端动态生成执行表单。</p>
     *
     * @param id 记忆ID
     * @return 参数定义响应
     */
    @GetMapping("{id}/params-definition")
    @Operation(summary = "获取记忆参数定义")
    @HasAuthority("sys:agent-memory:find")
    public R<ParamsDefinitionResponse> getParamsDefinition(@PathVariable String id) {
        AssertUtils.notEmpty(id, "主键不能为空");
        return R.ok(agentMemoryService.getParamsDefinition(id));
    }

    /**
     * 执行记忆。
     * <p>根据用户传入的参数替换步骤模板中的占位符，创建任务并逐步骤执行。</p>
     *
     * @param id      记忆ID
     * @param request 执行请求（含参数和客户端ID）
     * @return 执行响应
     */
    @PostMapping("{id}/execute")
    @Operation(summary = "执行记忆")
    @HasAuthority("sys:agent-memory:execute")
    public R<ExecuteMemoryResponse> execute(@PathVariable String id, @RequestBody ExecuteMemoryRequest request) {
        AssertUtils.notEmpty(id, "主键不能为空");
        return R.ok(agentMemoryService.execute(id, request));
    }

    /**
     * 查询记忆版本历史。
     * <p>沿 parentMemoryId 链路追溯，返回该记忆的完整版本演进链。</p>
     *
     * @param id 记忆ID
     * @return 版本历史列表
     */
    @GetMapping("{id}/version-history")
    @Operation(summary = "查询记忆版本历史")
    @HasAuthority("sys:agent-memory:find")
    public R<java.util.List<MemoryVersionHistoryResponse>> findVersionHistory(@PathVariable String id) {
        AssertUtils.notEmpty(id, "主键不能为空");
        return R.ok(agentMemoryService.findVersionHistory(id));
    }
}