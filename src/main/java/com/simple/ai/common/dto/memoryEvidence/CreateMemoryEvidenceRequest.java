package com.simple.ai.common.dto.memoryEvidence;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(title = "记忆证据(memory_evidence)创建请求参数")
public class CreateMemoryEvidenceRequest {

    @Schema(description = "轮次主键，关联 chat_turn.id")
    @NotEmpty(message = "轮次主键，关联 chat_turn.id不能为空")
    private String turnId;

    @Schema(description = "记忆版本主键，关联 agent_memory_version.id")
    @NotEmpty(message = "记忆版本主键，关联 agent_memory_version.id不能为空")
    private String memoryVersionId;

    @Schema(description = "证据类型: EXECUTION_TRACE/REASONING_SUMMARY")
    @NotEmpty(message = "证据类型: EXECUTION_TRACE/REASONING_SUMMARY不能为空")
    private String evidenceType;

    @Schema(description = "证据内容: 原子命令调用链+结果摘要")
    @NotEmpty(message = "证据内容: 原子命令调用链+结果摘要不能为空")
    private String evidenceContent;

    @Schema(description = "质量评分 0.00-1.00")
    @NotNull(message = "质量评分 0.00-1.00不能为空")
    private Double qualityScore;
}

