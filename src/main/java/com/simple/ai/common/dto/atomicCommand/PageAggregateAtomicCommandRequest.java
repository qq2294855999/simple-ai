package com.simple.ai.common.dto.atomicCommand;

import com.simple.common.mp.common.enums.Status;
import com.simple.common.mp.page.PageBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 原子命令聚合分页请求。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "原子命令聚合分页请求")
public class PageAggregateAtomicCommandRequest extends PageBase {

    @Schema(description = "关键词")
    private String keyword;

    @Schema(description = "技能ID")
    private String skillId;

    @Schema(description = "技能描述")
    private String skillDesc;

    @Schema(description = "智能体名称")
    private String agentName;

    @Schema(description = "状态")
    private Status status;
}
