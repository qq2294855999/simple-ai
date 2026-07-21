package com.simple.ai.common.dto.task;

import com.simple.common.mp.page.PageBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 任务聚合分页请求参数。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "任务聚合分页请求参数")
public class PageAggregateTaskRequest extends PageBase {

    @Schema(description = "关键字")
    private String keyword;

    @Schema(description = "智能体ID")
    private String agentId;

    @Schema(description = "智能体名称")
    private String agentName;

    @Schema(description = "记忆ID")
    private String memoryId;

    @Schema(description = "记忆名称")
    private String memoryName;

    @Schema(description = "步骤类型")
    private String stepType;

    @Schema(description = "执行状态")
    private String execStatus;
}