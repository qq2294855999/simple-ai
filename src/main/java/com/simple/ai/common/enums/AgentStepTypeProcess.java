package com.simple.ai.common.enums;

/**
 * 智能体步骤类型过程枚举。
 *
 * @author qty
 */
public enum AgentStepTypeProcess {

    /**
     * 判断
     */
    JUDGE("JUDGE", "判断"),

    /**
     * 原子命令
     */
    ATOMIC_COMMAND("ATOMIC_COMMAND", "原子命令"),

    /**
     * 循环开始
     */
    LOOP_START("LOOP_START", "循环开始"),

    /**
     * 循环结束
     */
    LOOP_END("LOOP_END", "循环结束");

    /**
     * 类型编码
     */
    private final String code;

    /**
     * 类型说明
     */
    private final String desc;

    /**
     * 创建智能体步骤类型过程枚举。
     *
     * @param code 类型编码
     * @param desc 类型说明
     */
    AgentStepTypeProcess(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取类型编码。
     *
     * @return 类型编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取类型说明。
     *
     * @return 类型说明
     */
    public String getDesc() {
        return desc;
    }

}
