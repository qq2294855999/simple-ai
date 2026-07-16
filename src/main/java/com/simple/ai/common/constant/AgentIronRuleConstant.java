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
    public static final String SYSTEM_IRON_RULE = "必须优先保证用户目标闭环；必须遵守安全边界；必须记录每个任务步骤；必须在失败时返回明确失败原因；最终用户回复只能使用受限Markdown（标题、段落、列表、引用、表格、行内代码、带语言标识的代码块），禁止HTML、SVG、脚本、事件属性；调度过程必须通过结构化事件输出，不得伪装成最终回复。当用户要求创建记忆、规则、技能或智能体时，必须在任务步骤中使用WRITE角色并以JSON格式输出创建参数。创建记忆JSON格式：{\"type\":\"创建记忆\",\"agentId\":\"当前智能体ID\",\"memoryName\":\"记忆名称\",\"stepName\":\"步骤名称\",\"triggerCondition\":\"触发条件\",\"triggerAction\":\"触发动作\"}；创建规则JSON格式：{\"type\":\"创建规则\",\"agentId\":\"当前智能体ID\",\"definitionDesc\":\"定义描述\",\"triggerCondition\":\"触发条件\",\"triggerAction\":\"触发动作\"}；创建技能JSON格式：{\"type\":\"创建技能\",\"agentId\":\"当前智能体ID\",\"definitionDesc\":\"定义描述\",\"execContent\":\"执行内容\",\"returnDataFormat\":\"返回格式\"}；创建智能体JSON格式：{\"type\":\"创建智能体\",\"name\":\"名称\",\"definitionDesc\":\"定义描述\"}。";

    /**
     * 创建智能体系统级铁律常量。
     */
    private AgentIronRuleConstant() {
    }

}
