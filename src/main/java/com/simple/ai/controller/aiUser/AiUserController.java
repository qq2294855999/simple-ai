package com.simple.ai.controller.aiUser;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.simple.ai.common.dto.aiUser.CreateAiUserRequest;
import com.simple.ai.common.dto.aiUser.InfoAiUserResponse;
import com.simple.ai.common.dto.aiUser.PageAiUserRequest;
import com.simple.ai.common.dto.aiUser.PageAiUserResponse;
import com.simple.ai.common.dto.aiUser.UpdateAiUserRequest;
import com.simple.ai.common.service.aiUser.AiUserService;
import com.simple.common.core.response.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
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

/**
 * AI 平台用户管理控制器。
 *
 * @author qty
 */
@Slf4j
@Tag(name = "AI 平台用户管理")
@RequestMapping("ai/user")
@RestController
public class AiUserController {

    @Autowired
    private AiUserService aiUserService;

    @Operation(summary = "分页查询用户列表")
    @GetMapping("page")
    public R<IPage<PageAiUserResponse>> findAll(@Validated PageAiUserRequest pageRequest) {
        return R.ok(aiUserService.findAll(pageRequest));
    }

    @Operation(summary = "根据ID查询用户详情")
    @GetMapping("{id}")
    public R<InfoAiUserResponse> findById(@PathVariable String id) {
        return R.ok(aiUserService.findById(id));
    }

    @Operation(summary = "创建用户")
    @PostMapping
    public R<String> create(@RequestBody @Validated CreateAiUserRequest request) {
        return R.ok(aiUserService.create(request));
    }

    @Operation(summary = "更新用户信息")
    @PutMapping
    public R<Object> update(@RequestBody @Validated UpdateAiUserRequest request) {
        aiUserService.update(request);
        return R.ok();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("{id}")
    public R<Object> delete(@PathVariable String id) {
        aiUserService.delete(id);
        return R.ok();
    }
}
