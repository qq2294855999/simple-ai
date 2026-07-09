package com.simple.ai.common.dto.agentRule;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.simple.common.mp.common.enums.DeleteState;
import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

import lombok.experimental.Accessors;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(chain = true)
@Schema(title = "智能体规则(agent_rule)明细响应")
public class InfoAgentRuleResponse {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "智能体ID")
    private String agentId;

    @Schema(description = "定义描述")
    private String definitionDesc;

    @Schema(description = "触发条件")
    private String triggerCondition;

    @Schema(description = "触发动作")
    private String triggerAction;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "修改时间")
    private Date updateTime;

    @Schema(description = "状态")
    private Status status;

    @Schema(description = "扩展")
    private String reserver;

    @Schema(description = "备注")
    private String remark;
}

