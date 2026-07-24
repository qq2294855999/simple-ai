package com.simple.ai.controller.agentExecutor;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.agentExecutor.*;
import com.simple.ai.common.service.agentExecutor.AgentExecutorService;
import com.simple.common.core.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 执行器类型管理控制器。
 *
 * @author qty
 */
@Slf4j
@Tag(name = "执行器管理")
@RequestMapping("agent/executor")
@RestController
public class AgentExecutorController {

    @Autowired
    private AgentExecutorService agentExecutorService;

    @Operation(summary = "分页查询执行器类型列表")
    @GetMapping("page")
    public R<IPage<PageAgentExecutorResponse>> findAll(@Validated PageAgentExecutorRequest pageRequest) {
        return R.ok(agentExecutorService.findAll(pageRequest));
    }

    @Operation(summary = "根据ID查询执行器类型详情")
    @GetMapping("{id}")
    public R<InfoAgentExecutorResponse> findById(@PathVariable String id) {
        return R.ok(agentExecutorService.findById(id));
    }

    @Operation(summary = "新增执行器类型")
    @PostMapping
    public R<String> save(@RequestBody @Validated CreateAgentExecutorRequest request) {
        return R.ok(agentExecutorService.save(request));
    }

    @Operation(summary = "更新执行器类型")
    @PutMapping
    public R<Object> updateById(@RequestBody @Validated UpdateAgentExecutorRequest request) {
        agentExecutorService.updateById(request);
        return R.ok();
    }

    @Operation(summary = "获取 SEP v1.0 协议说明")
    @GetMapping("protocol")
    public R<AgentExecutorProtocolResponse> getProtocol() {
        return R.ok(agentExecutorService.getProtocol());
    }

    @Operation(summary = "切换执行器类型启用/停用状态")
    @PutMapping("{id}/toggle-status")
    public R<String> toggleStatus(@PathVariable String id) {
        return R.ok(agentExecutorService.toggleStatus(id));
    }

    @Operation(summary = "删除执行器类型")
    @DeleteMapping("{id}")
    public R<Object> deleteById(@PathVariable String id) {
        agentExecutorService.deleteByIds(List.of(id));
        return R.ok();
    }

    @Operation(summary = "获取执行器关联的协议内容")
    @GetMapping("{executorId}/protocol")
    public R<String> getExecutorProtocol(@PathVariable String executorId) {
        return R.ok(agentExecutorService.getExecutorProtocol(executorId));
    }
}