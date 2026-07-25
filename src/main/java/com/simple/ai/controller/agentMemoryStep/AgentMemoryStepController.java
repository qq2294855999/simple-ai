package com.simple.ai.controller.agentMemoryStep;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.agentMemoryStep.CreateAgentMemoryStepRequest;
import com.simple.ai.common.dto.agentMemoryStep.PageAgentMemoryStepRequest;
import com.simple.ai.common.dto.agentMemoryStep.PageAgentMemoryStepResponse;
import com.simple.ai.common.dto.agentMemoryStep.UpdateAgentMemoryStepRequest;
import com.simple.ai.common.service.agentMemoryStep.AgentMemoryStepService;
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
 * 智能体记忆步骤(agent_memory_step)控制层。
 *
 * @author qty
 */
@Slf4j
@Tag(name = "智能体记忆步骤")
@RequestMapping("sys/agent-memory-step")
@RestController
public class AgentMemoryStepController {

    @Autowired
    private AgentMemoryStepService agentMemoryStepService;

    /**
     * 分页查询智能体记忆步骤。
     *
     * @param request 分页请求
     * @return 分页数据
     */
    @GetMapping("list")
    @Operation(summary = "分页查询智能体记忆步骤")
    @HasAuthority("sys:agent-memory-step:list")
    public R<IPage<PageAgentMemoryStepResponse>> list(@ParameterObject PageAgentMemoryStepRequest request) {
        return R.ok(agentMemoryStepService.findAll(request));
    }

    /**
     * 创建智能体记忆步骤。
     *
     * @param createRequest 创建请求
     * @return 主键
     */
    @PostMapping("create")
    @Operation(summary = "创建智能体记忆步骤")
    @HasAuthority("sys:agent-memory-step:create")
    public R<String> create(@RequestBody @Validated CreateAgentMemoryStepRequest createRequest) {
        return R.ok(agentMemoryStepService.save(createRequest));
    }

    /**
     * 更新单个智能体记忆步骤。
     *
     * @param id            主键
     * @param updateRequest 更新请求
     * @return 空响应
     */
    @PutMapping("update/{id}")
    @Operation(summary = "更新单个智能体记忆步骤")
    @HasAuthority("sys:agent-memory-step:update")
    public R<Object> update(@PathVariable String id, @RequestBody @Validated UpdateAgentMemoryStepRequest updateRequest) {
        AssertUtils.isTrue(updateRequest.getId().equals(id), "请求内容的ID与路径ID不同");
        agentMemoryStepService.updateById(updateRequest);
        return R.ok();
    }

    /**
     * 删除智能体记忆步骤。
     *
     * @param ids 主键列表
     * @return 空响应
     */
    @DeleteMapping("deletes")
    @Transactional
    @Operation(summary = "删除智能体记忆步骤")
    @HasAuthority("sys:agent-memory-step:deletes")
    public R<Object> deleteByIds(@RequestBody List<String> ids) {
        AssertUtils.notEmpty(ids, "主键不能为空");
        agentMemoryStepService.deleteByIds(ids);
        return R.ok();
    }
}