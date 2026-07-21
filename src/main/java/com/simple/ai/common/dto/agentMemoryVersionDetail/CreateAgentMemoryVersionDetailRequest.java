package com.simple.ai.common.dto.agentMemoryVersionDetail;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 记忆版本步骤(agent_memory_version_detail)新增请求参数。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "记忆版本步骤新增请求参数")
public class CreateAgentMemoryVersionDetailRequest {

    @Schema(description = "记忆版本ID")
    @NotEmpty(message = "记忆版本ID不能为空")
    private String versionId;

    @Schema(description = "步骤序号")
    @NotNull(message = "步骤序号不能为空")
    private Integer sequenceNo;

    @Schema(description = "原子命令ID")
    @NotEmpty(message = "原子命令ID不能为空")
    private String atomicCommandId;

    @Schema(description = "原子命令编码")
    private String atomicCommandCode;

    @Schema(description = "参数模板")
    private String argsTemplate;

    @Schema(description = "执行前随机延迟最小值(毫秒)")
    private Integer delayMinMs;

    @Schema(description = "执行前随机延迟最大值(毫秒)")
    private Integer delayMaxMs;

    @Schema(description = "命令超时时间(毫秒)")
    private Integer timeoutMs;

    @Schema(description = "幂等键")
    private String idempotencyKey;

    @Schema(description = "成功断言规则")
    private String successAssertion;

    @Schema(description = "失败处理策略")
    private String failureStrategy;

    @Schema(description = "状态")
    private String status;
}
