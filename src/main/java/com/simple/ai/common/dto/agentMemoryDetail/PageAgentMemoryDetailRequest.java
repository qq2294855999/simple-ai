package com.simple.ai.common.dto.agentMemoryDetail;

import java.util.Date;

import com.simple.common.mp.page.PageBase;
import io.swagger.v3.oas.annotations.media.Schema;
import com.simple.common.mp.common.enums.DeleteState;
import com.simple.common.mp.common.enums.Status;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(title = "智能体记忆详情(agent_memory_detail)列表请求参数")
public class PageAgentMemoryDetailRequest extends PageBase {

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

    @Schema(description = "状态")
    private Status status;

    @Schema(description = "扩展")
    private String reserver;

    @Schema(description = "备注")
    private String remark;
}

