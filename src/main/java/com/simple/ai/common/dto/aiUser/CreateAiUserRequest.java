package com.simple.ai.common.dto.aiUser;

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
public class CreateAiUserRequest {

    /**
     * 用户昵称（必填）
     */
    @NotEmpty(message = "昵称不能为空")
    private String nickname;

    /**
     * 用户账号（登录名，必填，需唯一）
     */
    @NotEmpty(message = "用户账号不能为空")
    private String username;

    /**
     * 手机号码（必填）
     */
    @NotEmpty(message = "手机号码不能为空")
    private String phone;

    /**
     * 头像 URL（非必填，为空则自动生成）
     */
    private String avatarUrl;

    /**
     * 每日 AI 调用次数上限，默认 100
     */
    private Integer dailyQuota;

    /**
     * 用户偏好设置（JSON 文本）
     */
    private String preferences;

    /**
     * 备注
     */
    private String remark;
}
