package com.simple.ai.common.dto.subAgentAssociation;

import java.util.Date;

import com.simple.common.mp.page.PageBase;
import io.swagger.v3.oas.annotations.media.Schema;
import com.simple.common.mp.common.enums.DeleteState;
import com.simple.common.mp.common.enums.Status;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(title = "子智能体关联(sub_agent_association)列表请求参数")
public class FindAllSubAgentAssociationRequest {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "主智能体")
    private String mainAgent;

    @Schema(description = "子智能体")
    private String subAgent;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "修改时间")
    private Date updateTime;

}

