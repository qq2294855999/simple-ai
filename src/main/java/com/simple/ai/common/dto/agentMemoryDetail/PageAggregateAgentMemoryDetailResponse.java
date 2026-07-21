package com.simple.ai.common.dto.agentMemoryDetail;

import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 智能体记忆详情聚合分页响应。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "智能体记忆详情聚合分页响应")
public class PageAggregateAgentMemoryDetailResponse {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "智能体记忆ID")
    private String agentMemoryId;

    @Schema(description = "记忆名称")
    private String memoryName;

    @Schema(description = "智能体ID")
    private String agentId;

    @Schema(description = "智能体名称")
    private String agentName;

    @Schema(description = "步骤名称")
    private String stepName;

    @Schema(description = "步骤类型")
    private String stepType;

    @Schema(description = "步骤类型标签")
    private String stepTypeLabel;

    @Schema(description = "执行内容")
    private String execContent;

    @Schema(description = "返回数据格式")
    private String returnDataFormat;

    @Schema(description = "父步骤ID")
    private String parentStepId;

    @Schema(description = "父步骤名称")
    private String parentStepName;

    @Schema(description = "下一个步骤ID")
    private String nextStepId;

    @Schema(description = "下一个步骤名称")
    private String nextStepName;

    @Schema(description = "分支条件")
    private String branchCondition;

    @Schema(description = "分支路由")
    private String branchRoute;

    @Schema(description = "模型")
    private String model;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "修改时间")
    private Date updateTime;

    @Schema(description = "状态")
    private Status status;

    @Schema(description = "备注")
    private String remark;
}