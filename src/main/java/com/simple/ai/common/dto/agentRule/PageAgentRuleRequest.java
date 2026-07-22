package com.simple.ai.common.dto.agentRule;

import com.simple.common.mp.common.enums.Status;
import com.simple.common.mp.page.PageBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(title = "智能体规则(agent_rule)列表请求参数")
public class PageAgentRuleRequest extends PageBase {

    @Schema(description = "智能体ID")
    private String agentId;

    @Schema(description = "定义描述")
    private String definitionDesc;

    @Schema(description = "触发条件")
    private String triggerCondition;

    @Schema(description = "触发动作")
    private String triggerAction;

    @Schema(description = "状态")
    private Status status;

    @Schema(description = "扩展")
    private String reserve;

    @Schema(description = "备注")
    private String remark;
}

