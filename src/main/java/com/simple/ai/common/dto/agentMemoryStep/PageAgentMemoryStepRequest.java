package com.simple.ai.common.dto.agentMemoryStep;

import com.simple.common.mp.page.PageBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 智能体记忆步骤(agent_memory_step)分页请求参数。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "智能体记忆步骤(agent_memory_step)分页请求参数")
public class PageAgentMemoryStepRequest extends PageBase {

    @Schema(description = "记忆ID")
    private String memoryId;

    @Schema(description = "步骤名称")
    private String stepName;

    @Schema(description = "原子命令编码")
    private String atomicCommandCode;

    @Schema(description = "状态：ON/OFF")
    private String status;
}