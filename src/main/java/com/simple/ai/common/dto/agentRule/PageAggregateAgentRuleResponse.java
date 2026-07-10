package com.simple.ai.common.dto.agentRule;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 智能体规则聚合分页响应。
 *
 * @author qty
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(title = "智能体规则聚合分页响应")
public class PageAggregateAgentRuleResponse {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "智能体ID")
    private String agentId;

    @Schema(description = "智能体名称")
    private String agentName;

    @Schema(description = "定义描述")
    private String definitionDesc;

    @Schema(description = "触发条件")
    private String triggerCondition;

    @Schema(description = "触发动作")
    private String triggerAction;

    @Schema(description = "修改时间")
    private Date updateTime;

    @Schema(description = "状态")
    private Status status;

    @Schema(description = "备注")
    private String remark;
}
