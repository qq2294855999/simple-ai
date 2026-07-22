package com.simple.ai.common.dto.memoryEvidence;

import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@Schema(title = "记忆证据(memory_evidence)单条数据请求参数")
public class DeleteMemoryEvidenceRequest {

    @Schema(description = "证据主键，UUID")
    private String id;

    @Schema(description = "轮次主键，关联 chat_turn.id")
    private String turnId;

    @Schema(description = "记忆版本主键，关联 agent_memory_version.id")
    private String memoryVersionId;

    @Schema(description = "证据类型: EXECUTION_TRACE/REASONING_SUMMARY")
    private String evidenceType;

    @Schema(description = "证据内容: 原子命令调用链+结果摘要")
    private String evidenceContent;

    @Schema(description = "质量评分 0.00-1.00")
    private Object qualityScore;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "状态: ON/DISABLE")
    private Status status;

}

