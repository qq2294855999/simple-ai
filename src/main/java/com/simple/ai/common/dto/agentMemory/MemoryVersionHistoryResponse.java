package com.simple.ai.common.dto.agentMemory;

import com.simple.ai.common.enums.AgentMemoryVersionStatusProcess;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 记忆版本历史响应参数。
 * <p>用于前端展示某记忆的版本演进链路。</p>
 *
 * @author qty
 */
@Data
@Schema(title = "记忆版本历史响应参数")
public class MemoryVersionHistoryResponse {

    @Schema(description = "记忆ID")
    private String id;

    @Schema(description = "版本号")
    private Integer versionNo;

    @Schema(description = "版本状态")
    private AgentMemoryVersionStatusProcess versionStatus;

    @Schema(description = "父记忆ID")
    private String parentMemoryId;

    @Schema(description = "创建原因：MANUAL/AI_EXPLORATION/MEMORY_REVISE")
    private String createReason;

    @Schema(description = "记忆摘要")
    private String summary;

    @Schema(description = "创建时间")
    private Date createTime;
}