package com.simple.ai.common.dto.agentMemoryDetail;

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
@Schema(title = "智能体记忆详情(agent_memory_detail)分页明细响应")
public class PageAgentMemoryDetailResponse {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "智能体记忆ID")
    private String agentMemoryId;

    @Schema(description = "步骤名称")
    private String stepName;

    @Schema(description = "父步骤ID")
    private String parentStepId;

    @Schema(description = "下一个步骤ID")
    private String nextStepId;

    @Schema(description = "步骤类型")
    private String stepType;

    @Schema(description = "分支条件")
    private String branchCondition;

    @Schema(description = "分支路由")
    private String branchRoute;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "修改时间")
    private Date updateTime;
}

