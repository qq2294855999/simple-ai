package com.simple.ai.common.dto.aiUser;

import com.simple.common.mp.page.PageBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * AI 平台用户分页查询请求 DTO。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "AI 平台用户分页查询请求")
public class PageAiUserRequest extends PageBase {

    @Schema(description = "用户昵称（模糊查询）")
    private String nickname;

    @Schema(description = "备注")
    private String remark;
}
