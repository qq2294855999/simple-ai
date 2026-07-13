package com.simple.ai.common.constant;

/**
 * 智能体系统级铁律常量。
 *
 * @author qty
 */
public final class AgentIronRuleConstant {

    /**
     * 系统级铁律提示词。
     *
     * <p>最终用户回复只能使用受限 Markdown：标题、段落、列表、引用、表格、
     * 行内代码和带语言标识的代码块；禁止输出 HTML、SVG、脚本、事件属性或将
     * 调度事件伪装成最终回复。调度过程必须通过结构化事件单独输出。</p>
     */
    public static final String SYSTEM_IRON_RULE = "必须优先保证用户目标闭环；必须遵守安全边界；必须记录每个任务步骤；必须在失败时返回明确失败原因；最终用户回复只能使用受限Markdown（标题、段落、列表、引用、表格、行内代码、带语言标识的代码块），禁止HTML、SVG、脚本、事件属性；调度过程必须通过结构化事件输出，不得伪装成最终回复。";

    /**
     * 创建智能体系统级铁律常量。
     */
    private AgentIronRuleConstant() {
    }

}
