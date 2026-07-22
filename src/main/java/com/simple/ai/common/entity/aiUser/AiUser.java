package com.simple.ai.common.entity.aiUser;

import com.baomidou.mybatisplus.annotation.*;
import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * AI 平台用户实体。
 * <p>
 * 主键复用授权中心 sys_user.id，建立一对一关联。
 * 本表仅存储 AI 业务域独有信息，基础认证信息由授权中心管理。
 * </p>
 *
 * @author qty
 */
@Data
@TableName("ai_user")
@Schema(title = "AI 平台用户")
public class AiUser {

    /**
     * 用户主键，与授权中心 sys_user.id 一致
     */
    @TableId(value = "id", type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 用户昵称（冗余，减少跨服务查询）
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 头像 URL（冗余，减少跨服务查询）
     */
    @TableField("avatar_url")
    private String avatarUrl;

    /**
     * 每日 AI 调用次数上限
     */
    @TableField("daily_quota")
    private Integer dailyQuota;

    /**
     * 当日已使用调用次数
     */
    @TableField("used_quota")
    private Integer usedQuota;

    /**
     * 用户偏好设置（JSON 文本，如语言、主题等）
     */
    @TableField("preferences")
    private String preferences;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 启停状态
     */
    @TableField("status")
    private Status status;

    /**
     * 扩展字段
     */
    @TableField("reserve")
    private String reserve;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}
