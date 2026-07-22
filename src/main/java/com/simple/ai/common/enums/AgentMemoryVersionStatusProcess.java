package com.simple.ai.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 记忆版本状态枚举。
 * <p>数据库存储 int code 值，MyBatis-Plus 按 @EnumValue 映射。</p>
 *
 * @author qty
 */
@Getter
@AllArgsConstructor
public enum AgentMemoryVersionStatusProcess {

    DRAFT(1, "草稿"),
    PUBLISHED(2, "已发布"),
    RETIRED(3, "已退役");

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
