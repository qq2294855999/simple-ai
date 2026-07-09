package com.simple.ai.common.constant;

/**
 * 智能体系统级铁律常量。
 *
 * @author qty
 */
public final class AgentIronRuleConstant {

    /**
     * 系统级铁律提示词
     */
    public static final String SYSTEM_IRON_RULE = "必须优先保证用户目标闭环；必须遵守安全边界；必须记录每个任务步骤；必须在失败时返回明确失败原因。";

    /**
     * 创建智能体系统级铁律常量。
     */
    private AgentIronRuleConstant() {
    }

}
