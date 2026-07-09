package com.simple.ai.common.dto.agentRule;

import java.util.Date;

import com.simple.common.mp.page.PageBase;
import io.swagger.v3.oas.annotations.media.Schema;
import com.simple.common.mp.common.enums.DeleteState;
import com.simple.common.mp.common.enums.Status;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(title = "智能体规则(agent_rule)列表请求参数")
public class PageAgentRuleRequest extends PageBase {

    @Schema(description = "名称")
    private String name;

    @Schema(description = "定义描述")
    private String definitionDesc;

    @Schema(description = "状态")
    private Status status;
}

