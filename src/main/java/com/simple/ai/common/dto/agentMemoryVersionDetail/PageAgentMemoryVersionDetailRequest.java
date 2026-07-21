package com.simple.ai.common.dto.agentMemoryVersionDetail;

import com.simple.common.mp.page.PageBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 记忆版本步骤(agent_memory_version_detail)分页请求参数。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "记忆版本步骤分页请求参数")
public class PageAgentMemoryVersionDetailRequest extends PageBase {

    @Schema(description = "记忆版本ID")
    private String versionId;

    @Schema(description = "原子命令编码")
    private String atomicCommandCode;

    @Schema(description = "状态")
    private String status;
}
