package com.simple.ai.common.dto.agentMemory;

import com.simple.common.mp.common.enums.Status;
import com.simple.common.mp.page.PageBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 智能体记忆聚合分页请求。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "智能体记忆聚合分页请求")
public class PageAggregateAgentMemoryRequest extends PageBase {

    @Schema(description = "关键词")
    private String keyword;

    @Schema(description = "智能体ID")
    private String agentId;

    @Schema(description = "智能体名称")
    private String agentName;

    @Schema(description = "状态")
    private Status status;
}
