package com.simple.ai.common.dto.subAgentRelation;

import java.util.Date;

import com.simple.common.mp.page.PageBase;
import io.swagger.v3.oas.annotations.media.Schema;
import com.simple.common.mp.common.enums.DeleteState;
import com.simple.common.mp.common.enums.Status;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(title = "子智能体关联(sub_agent_relation)列表请求参数")
public class PageSubAgentRelationRequest extends PageBase {

    @Schema(description = "主智能体")
    private String mainAgentId;

    @Schema(description = "子智能体")
    private String subAgentId;

    @Schema(description = "状态")
    private Status status;

    @Schema(description = "扩展")
    private String reserver;

    @Schema(description = "备注")
    private String remark;
}

