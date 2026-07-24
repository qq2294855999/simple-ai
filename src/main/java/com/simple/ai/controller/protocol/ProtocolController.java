package com.simple.ai.controller.protocol;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.protocol.*;
import com.simple.ai.common.service.protocol.ProtocolService;
import com.simple.common.core.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 执行器协议管理控制器。
 *
 * @author qty
 */
@Slf4j
@Tag(name = "执行器协议管理")
@RequestMapping("agent/protocol")
@RestController
public class ProtocolController {

    @Autowired
    private ProtocolService protocolService;

    @Operation(summary = "分页查询执行器协议列表")
    @GetMapping("page")
    public R<IPage<PageProtocolResponse>> findAll(@Validated PageProtocolRequest pageRequest) {
        return R.ok(protocolService.findAll(pageRequest));
    }

    @Operation(summary = "根据ID查询执行器协议详情")
    @GetMapping("{id}")
    public R<InfoProtocolResponse> findById(@PathVariable String id) {
        return R.ok(protocolService.findById(id));
    }

    @Operation(summary = "新增执行器协议")
    @PostMapping
    public R<String> save(@RequestBody @Validated CreateProtocolRequest request) {
        return R.ok(protocolService.save(request));
    }

    @Operation(summary = "更新执行器协议")
    @PutMapping
    public R<Object> updateById(@RequestBody @Validated UpdateProtocolRequest request) {
        protocolService.updateById(request);
        return R.ok();
    }

    @Operation(summary = "切换执行器协议启用/停用状态")
    @PutMapping("{id}/toggle-status")
    public R<String> toggleStatus(@PathVariable String id) {
        return R.ok(protocolService.toggleStatus(id));
    }

    @Operation(summary = "删除执行器协议")
    @DeleteMapping("{id}")
    public R<Object> deleteById(@PathVariable String id) {
        protocolService.deleteByIds(List.of(id));
        return R.ok();
    }
}