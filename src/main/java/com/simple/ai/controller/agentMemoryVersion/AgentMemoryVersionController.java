package com.simple.ai.controller.agentMemoryVersion;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.agentMemoryVersion.*;
import com.simple.ai.common.service.agentMemoryVersion.AgentMemoryVersionService;
import com.simple.common.core.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 记忆版本管理控制器。
 *
 * @author qty
 */
@Slf4j
@Tag(name = "记忆版本管理")
@RequestMapping("agent/memory-version")
@RestController
public class AgentMemoryVersionController {

    @Autowired
    private AgentMemoryVersionService agentMemoryVersionService;

    @Operation(summary = "分页查询记忆版本列表")
    @GetMapping("page")
    public R<IPage<PageAgentMemoryVersionResponse>> findAll(@Validated PageAgentMemoryVersionRequest pageRequest) {
        return R.ok(agentMemoryVersionService.findAll(pageRequest));
    }

    @Operation(summary = "根据ID查询记忆版本详情")
    @GetMapping("{id}")
    public R<InfoAgentMemoryVersionResponse> findById(@PathVariable String id) {
        return R.ok(agentMemoryVersionService.findById(id));
    }

    @Operation(summary = "新增记忆版本")
    @PostMapping
    public R<String> save(@RequestBody @Validated CreateAgentMemoryVersionRequest request) {
        return R.ok(agentMemoryVersionService.save(request));
    }

    @Operation(summary = "更新记忆版本")
    @PutMapping
    public R<Object> updateById(@RequestBody @Validated UpdateAgentMemoryVersionRequest request) {
        agentMemoryVersionService.updateById(request);
        return R.ok();
    }

    @Operation(summary = "删除记忆版本")
    @DeleteMapping("{id}")
    public R<Object> deleteById(@PathVariable String id) {
        agentMemoryVersionService.deleteByIds(List.of(id));
        return R.ok();
    }

    @Operation(summary = "发布记忆版本（草稿→已发布）")
    @PutMapping("publish/{id}")
    public R<Object> publish(@PathVariable String id) {
        agentMemoryVersionService.publish(id);
        return R.ok();
    }

    @Operation(summary = "废弃记忆版本（已发布→已退役）")
    @PutMapping("retire/{id}")
    public R<Object> retire(@PathVariable String id) {
        agentMemoryVersionService.retire(id);
        return R.ok();
    }
}
