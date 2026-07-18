package com.simple.ai.common.dto.aiUser;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * AI 平台用户分页响应 DTO。
 *
 * @author qty
 */
@Data
@Schema(title = "AI 平台用户分页响应")
public class PageAiUserResponse {

    @Schema(description = "用户主键")
    private String id;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "头像 URL")
    private String avatarUrl;

    @Schema(description = "每日 AI 调用次数上限")
    private Integer dailyQuota;

    @Schema(description = "当日已使用调用次数")
    private Integer usedQuota;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "备注")
    private String remark;
}
