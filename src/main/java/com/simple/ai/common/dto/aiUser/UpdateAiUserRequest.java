package com.simple.ai.common.dto.aiUser;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 更新 AI 平台用户请求 DTO。
 *
 * @author qty
 */
@Data
@Schema(title = "更新AI平台用户请求参数")
public class UpdateAiUserRequest {

    @Schema(description = "用户主键")
    @NotEmpty(message = "用户ID不能为空")
    private String id;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "用户账号")
    private String username;

    @Schema(description = "手机号码")
    private String phone;

    @Schema(description = "头像URL")
    private String avatarUrl;

    @Schema(description = "每日AI调用次数上限")
    private Integer dailyQuota;

    @Schema(description = "用户偏好设置")
    private String preferences;

    @Schema(description = "备注")
    private String remark;
}