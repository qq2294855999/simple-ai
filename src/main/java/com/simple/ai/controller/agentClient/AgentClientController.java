package com.simple.ai.controller.agentClient;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.agentClient.*;
import com.simple.ai.common.service.agentClient.AgentClientService;
import com.simple.common.auth.client.util.LoginUserUtils;
import com.simple.common.core.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客户端实例管理控制器。
 * <p>创建客户端时自动生成密钥，明文仅创建时返回一次。</p>
 *
 * @author qty
 */
@Slf4j
@Tag(name = "客户端实例管理")
@RequestMapping("agent/client")
@RestController
public class AgentClientController {

    @Autowired
    private AgentClientService agentClientService;

    @Operation(summary = "分页查询客户端实例列表")
    @GetMapping("page")
    public R<IPage<PageAgentClientResponse>> findAll(@Validated PageAgentClientRequest pageRequest) {
        return R.ok(agentClientService.findAll(pageRequest));
    }

    @Operation(summary = "根据ID查询客户端实例详情")
    @GetMapping("{id}")
    public R<InfoAgentClientResponse> findById(@PathVariable String id) {
        return R.ok(agentClientService.findById(id));
    }

    @Operation(summary = "新增客户端实例（密钥仅创建时返回）")
    @PostMapping
    public R<InfoAgentClientResponse> save(@RequestBody @Validated CreateAgentClientRequest createRequest) {
        // 通过 LoginUserUtils 获取当前登录用户ID，后端统一赋值不信任前端传入
        String userId = LoginUserUtils.getUserTemporary().getUserId();
        return R.ok(agentClientService.save(createRequest, userId));
    }

    @Operation(summary = "更新客户端实例")
    @PutMapping
    public R<Object> updateById(@RequestBody @Validated UpdateAgentClientRequest request) {
        agentClientService.updateById(request);
        return R.ok();
    }

    @Operation(summary = "删除客户端实例")
    @DeleteMapping("{id}")
    public R<Object> deleteById(@PathVariable String id) {
        agentClientService.deleteByIds(List.of(id));
        return R.ok();
    }
}
