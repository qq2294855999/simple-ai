package com.simple.ai.common.dto.aiUser;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * AI 平台用户详情响应 DTO。
 * <p>
 * 聚合本地 ai_user 与授权中心 sys_user 的基础信息。
 * </p>
 *
 * @author qty
 */
@Data
@Schema(title = "AI 平台用户详情响应")
public class InfoAiUserResponse {

    @Schema(description = "用户主键")
    private String id;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "用户账号（登录名）")
    private String username;

    @Schema(description = "手机号码")
    private String phone;

    @Schema(description = "头像 URL")
    private String avatarUrl;

    @Schema(description = "每日 AI 调用次数上限")
    private Integer dailyQuota;

    @Schema(description = "当日已使用调用次数")
    private Integer usedQuota;

    @Schema(description = "用户偏好设置（JSON）")
    private String preferences;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "修改时间")
    private Date updateTime;

    @Schema(description = "备注")
    private String remark;
}
