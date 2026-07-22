package com.simple.ai.common.dto.memoryEvidence;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(title = "记忆证据(memory_evidence)修改请求参数")
public class UpdateMemoryEvidenceRequest extends CreateMemoryEvidenceRequest {

    @Schema(description = "证据主键，UUID")
    @NotEmpty(message = "证据主键，UUID不能为空")
    private String id;

}

