package com.simple.ai.controller.aiModel;

import com.simple.ai.common.dto.aiModel.AiModelResponse;
import com.simple.ai.common.dto.aiModel.AiModelSaveRequest;
import com.simple.ai.common.service.aiModel.AiModelService;
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
 * AI 模型管理控制层。
 *
 * @author qty
 */
@Tag(name = "AI模型管理")
@RequestMapping("sys/ai-model")
@RestController
public class AiModelController {

    @Autowired
    private AiModelService aiModelService;

    /**
     * 查询全部模型。
     *
     * @return 模型列表
     */
    @GetMapping("list")
    @Operation(summary = "查询全部模型")
    @HasAuthority("sys:ai-model:list")
    public R<List<AiModelResponse>> list() {
        return R.ok(aiModelService.findAll());
    }

    /**
     * 查询指定智能体可用的模型。
     *
     * @param agentId 智能体主键
     * @return 可用模型列表
     */
    @GetMapping("available/{agentId}")
    @Operation(summary = "查询智能体可用模型")
    @HasAuthority("sys:ai-model:available")
    public R<List<AiModelResponse>> available(@PathVariable String agentId) {
        AssertUtils.notEmpty(agentId, "智能体主键不能为空");
        return R.ok(aiModelService.findAvailableByAgentId(agentId));
    }

    /**
     * 保存模型。
     *
     * @param request 保存请求
     * @return 模型主键
     */
    @PostMapping("save")
    @Operation(summary = "保存模型")
    @HasAuthority("sys:ai-model:save")
    public R<String> save(@RequestBody @Validated AiModelSaveRequest request) {
        return R.ok(aiModelService.save(request));
    }

    /**
     * 删除模型。
     *
     * @param id 模型主键
     * @return 空响应
     */
    @DeleteMapping("delete/{id}")
    @Operation(summary = "删除模型")
    @HasAuthority("sys:ai-model:delete")
    public R<Object> deleteById(@PathVariable String id) {
        AssertUtils.notEmpty(id, "主键不能为空");
        aiModelService.deleteById(id);
        return R.ok();
    }
}
