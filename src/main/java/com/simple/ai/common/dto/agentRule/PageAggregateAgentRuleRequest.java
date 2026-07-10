package com.simple.ai.common.dto.agentRule;

import com.simple.common.mp.common.enums.Status;
import com.simple.common.mp.page.PageBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 智能体规则聚合分页请求。
 *
 * @author qty
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(title = "智能体规则聚合分页请求")
public class PageAggregateAgentRuleRequest extends PageBase {

    @Schema(description = "关键字，匹配规则定义、触发条件、触发动作、备注和智能体名称")
    private String keyword;

    @Schema(description = "智能体ID")
    private String agentId;

    @Schema(description = "智能体名称")
    private String agentName;

    @Schema(description = "状态")
    private Status status;
}
