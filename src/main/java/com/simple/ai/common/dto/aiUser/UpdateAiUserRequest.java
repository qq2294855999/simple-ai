package com.simple.ai.common.dto.aiUser;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 更新 AI 平台用户请求 DTO。
 *
 * @author qty
 */
@Data
public class UpdateAiUserRequest {

    /**
     * 用户主键（必填）
     */
    @NotEmpty(message = "用户ID不能为空")
    private String id;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户账号（登录名）
     */
    private String username;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 头像 URL
     */
    private String avatarUrl;

    /**
     * 每日 AI 调用次数上限
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
