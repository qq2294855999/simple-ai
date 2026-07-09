package com.simple.ai.common.dto.agentDefinition;

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
@Schema(title = "智能体定义(agent_definition)明细响应")
public class InfoAgentDefinitionResponse {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "定义描述")
    private String definitionDesc;

    @Schema(description = "第一铁律")
    private String firstRule;

    @Schema(description = "第二规则")
    private String secondRule;

    @Schema(description = "第三技能")
    private String thirdSkill;

    @Schema(description = "状态")
    private Status status;

    @Schema(description = "模型")
    private String model;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "修改人")
    private String updateBy;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "修改时间")
    private Date updateTime;
}

