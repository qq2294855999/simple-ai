package com.simple.ai.common.enums;

/**
 * 智能体执行状态过程枚举。
 *
 * @author qty
 */
public enum AgentExecutionStatusProcess {

    /**
     * 等待执行
     */
    WAITING("WAITING", "等待执行"),

    /**
     * 执行中
     */
    RUNNING("RUNNING", "执行中"),

    /**
     * 执行成功
     */
    SUCCESS("SUCCESS", "执行成功"),

    /**
     * 执行失败
     */
    FAILED("FAILED", "执行失败");

    /**
     * 状态编码
     */
    private final String code;

    /**
     * 状态说明
     */
    private final String desc;

    /**
     * 创建智能体执行状态过程枚举。
     *
     * @param code 状态编码
     * @param desc 状态说明
     */
    AgentExecutionStatusProcess(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取状态编码。
     *
     * @return 状态编码
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取状态说明。
     *
     * @return 状态说明
     */
    public String getDesc() {
        return desc;
    }

}
