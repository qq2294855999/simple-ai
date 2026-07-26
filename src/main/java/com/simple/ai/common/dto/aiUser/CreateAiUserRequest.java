package com.simple.ai.common.dto.aiUser;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 创建 AI 平台用户请求 DTO。
 * <p>
 * 包含授权中心所需的基础信息（nickname/username/phone）以及 AI 业务独有字段。
 * 密码由授权中心使用系统默认密码自动生成。
 * </p>
 *
 * @author qty
 */
@Data
@Schema(title = "创建AI平台用户请求参数")
public class CreateAiUserRequest {

    @Schema(description = "用户昵称")
    @NotEmpty(message = "昵称不能为空")
    private String nickname;

    @Schema(description = "用户账号")
    @NotEmpty(message = "用户账号不能为空")
    private String username;

    @Schema(description = "手机号码")
    @NotEmpty(message = "手机号码不能为空")
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