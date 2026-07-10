package com.simple.ai.common.dto.subAgentRelation;

import com.simple.common.mp.common.enums.Status;
import com.simple.common.mp.page.PageBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 子智能体关系聚合分页请求。
 *
 * @author qty
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(title = "子智能体关系聚合分页请求")
public class PageAggregateSubAgentRelationRequest extends PageBase {

    @Schema(description = "关键字，匹配主智能体名称、子智能体名称和备注")
    private String keyword;

    @Schema(description = "主智能体名称")
    private String mainAgentName;

    @Schema(description = "子智能体名称")
    private String subAgentName;

    @Schema(description = "状态")
    private Status status;
}
