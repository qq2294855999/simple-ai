package com.simple.ai.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 智能体步骤类型过程枚举。
 *
 * <p>数据库存储 int code 值，MyBatis-Plus 按 @EnumValue 映射。</p>
 *
 * @author qty
 */
@Getter
@AllArgsConstructor
public enum AgentStepTypeProcess {

    JUDGE(1, "判断"),
    ATOMIC_COMMAND(2, "原子命令"),
    LOOP_START(3, "循环开始"),
    LOOP_END(4, "循环结束");

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
