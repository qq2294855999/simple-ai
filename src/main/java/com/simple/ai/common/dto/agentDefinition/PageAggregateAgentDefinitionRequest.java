package com.simple.ai.common.dto.agentDefinition;

import com.simple.common.mp.common.enums.Status;
import com.simple.common.mp.page.PageBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 智能体定义聚合分页请求。
 *
 * @author qty
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(title = "智能体定义聚合分页请求")
public class PageAggregateAgentDefinitionRequest extends PageBase {

    @Schema(description = "关键字，匹配名称和定义描述")
    private String keyword;

    @Schema(description = "状态")
    private Status status;
}
