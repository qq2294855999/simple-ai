package com.simple.ai.controller.aiModelProvider;

import com.simple.ai.common.dto.aiModelProvider.AiModelProviderResponse;
import com.simple.ai.common.dto.aiModelProvider.AiModelProviderSaveRequest;
import com.simple.ai.common.service.aiModelProvider.AiModelProviderService;
import com.simple.common.auth.client.common.annotation.HasAuthority;
import com.simple.common.core.response.R;
import com.simple.common.core.utils.AssertUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI 模型供应商管理控制层。
 *
 * @author qty
 */
@Tag(name = "AI模型供应商")
@RequestMapping("sys/ai-model-provider")
@RestController
public class AiModelProviderController {

    @Autowired
    private AiModelProviderService aiModelProviderService;

    /**
     * 查询全部供应商。
     *
     * @return 供应商列表
     */
    @GetMapping("list")
    @Operation(summary = "查询全部供应商")
    @HasAuthority("sys:ai-model-provider:list")
    public R<List<AiModelProviderResponse>> list() {
        return R.ok(aiModelProviderService.findAll());
    }

    /**
     * 保存供应商。
     *
     * @param request 保存请求
     * @return 供应商主键
     */
    @PostMapping("save")
    @Operation(summary = "保存供应商")
    @HasAuthority("sys:ai-model-provider:save")
    public R<String> save(@RequestBody @Validated AiModelProviderSaveRequest request) {
        return R.ok(aiModelProviderService.save(request));
    }

    /**
     * 删除供应商。
     *
     * @param id 供应商主键
     * @return 空响应
     */
    @DeleteMapping("delete/{id}")
    @Operation(summary = "删除供应商")
    @HasAuthority("sys:ai-model-provider:delete")
    public R<Object> deleteById(@PathVariable String id) {
        AssertUtils.notEmpty(id, "主键不能为空");
        aiModelProviderService.deleteById(id);
        return R.ok();
    }
}
