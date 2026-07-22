package com.simple.ai.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 客户端实例状态枚举。
 * <p>数据库存储 int code 值，MyBatis-Plus 按 @EnumValue 映射。</p>
 *
 * @author qty
 */
@Getter
@AllArgsConstructor
public enum AgentClientStatusProcess {

    ACTIVE(1, "活跃"),
    EXPIRED(2, "已过期"),
    DISABLED(3, "已禁用"),
    REVOKED(4, "已吊销");

    /**
     * MyBatis-Plus 数据库映射值
     */
    @EnumValue
    private final int code;

    /**
     * 中文说明
     */
    private final String label;
}
