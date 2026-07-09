package com.simple.ai.common.dto.subAgentRelation;

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
@Schema(title = "子智能体关联(sub_agent_relation)明细响应")
public class InfoSubAgentRelationResponse {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "主智能体")
    private String mainAgentId;

    @Schema(description = "子智能体")
    private String subAgentId;

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

