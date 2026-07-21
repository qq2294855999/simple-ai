package com.simple.ai.common.dto.agentMemoryDetail;

import com.simple.common.mp.common.enums.Status;
import com.simple.common.mp.page.PageBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 智能体记忆详情聚合分页请求参数。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "智能体记忆详情聚合分页请求参数")
public class PageAggregateAgentMemoryDetailRequest extends PageBase {

    @Schema(description = "关键字")
    private String keyword;

    @Schema(description = "智能体记忆ID")
    private String agentMemoryId;

    @Schema(description = "记忆名称")
    private String memoryName;

    @Schema(description = "智能体名称")
    private String agentName;

    @Schema(description = "步骤类型")
    private String stepType;

    @Schema(description = "状态")
    private Status status;
}