package com.simple.ai.common.dto.agentMemoryDetail;

import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@Schema(title = "智能体记忆详情(agent_memory_detail)单条数据请求参数")
public class DeleteAgentMemoryDetailRequest {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "智能体记忆ID")
    private String agentMemoryId;

    @Schema(description = "步骤名称")
    private String stepName;

    @Schema(description = "步骤类型：智能体记忆步骤类型")
    private String stepType;

    @Schema(description = "执行内容")
    private String execContent;

    @Schema(description = "返回的数据格式")
    private String returnDataFormat;

    @Schema(description = "父步骤ID")
    private String parentStepId;

    @Schema(description = "下一个步骤ID")
    private String nextStepId;

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

    @Schema(description = "扩展")
    private String reserve;

    @Schema(description = "备注")
    private String remark;

}

