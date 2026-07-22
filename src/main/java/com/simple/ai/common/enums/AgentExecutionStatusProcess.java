package com.simple.ai.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 智能体执行状态过程枚举。
 *
 * <p>数据库存储 int code 值，MyBatis-Plus 按 @EnumValue 映射。</p>
 *
 * @author qty
 */
@Getter
@AllArgsConstructor
public enum AgentExecutionStatusProcess {

    WAITING(1, "等待执行"),
    RUNNING(2, "执行中"),
    SUCCESS(3, "执行成功"),
    FAILED(4, "执行失败");

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
