package com.simple.ai.common.dto.agentMemory;

import com.simple.common.mp.common.enums.Status;
import com.simple.common.mp.page.PageBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 智能体记忆分页请求参数
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "智能体记忆(agent_memory)分页请求参数")
public class PageAgentMemoryRequest extends PageBase {

    @Schema(description = "智能体ID")
    private String agentId;

    @Schema(description = "记忆名称模板")
    private String memoryName;

    @Schema(description = "版本状态：1=DRAFT, 2=PUBLISHED")
    private Integer versionStatus;

    @Schema(description = "状态")
    private Status status;

    @Schema(description = "扩展")
    private String reserve;

    @Schema(description = "备注")
    private String remark;
}