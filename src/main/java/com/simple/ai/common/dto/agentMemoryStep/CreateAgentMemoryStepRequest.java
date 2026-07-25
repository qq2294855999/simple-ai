package com.simple.ai.common.dto.agentMemoryStep;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 智能体记忆步骤(agent_memory_step)创建请求参数。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "智能体记忆步骤(agent_memory_step)创建请求参数")
public class CreateAgentMemoryStepRequest {

    @Schema(description = "记忆ID")
    @NotEmpty(message = "记忆ID不能为空")
    private String memoryId;

    @Schema(description = "步骤序号，从10开始递增")
    @NotNull(message = "步骤序号不能为空")
    private Integer sequenceNo;

    @Schema(description = "原子命令主键")
    @NotEmpty(message = "原子命令主键不能为空")
    private String atomicCommandId;

    @Schema(description = "原子命令编码")
    @NotEmpty(message = "原子命令编码不能为空")
    private String atomicCommandCode;

    @Schema(description = "步骤名称")
    @NotEmpty(message = "步骤名称不能为空")
    private String stepName;

    @Schema(description = "参数模板JSON，支持{param}占位符")
    private String argsTemplate;

    @Schema(description = "执行前随机延迟最小值（毫秒）")
    private Integer delayMinMs;

    @Schema(description = "执行前随机延迟最大值（毫秒）")
    private Integer delayMaxMs;

    @Schema(description = "命令超时时间（毫秒）")
    private Integer timeoutMs;

    @Schema(description = "成功断言规则")
    private String successAssertion;

    @Schema(description = "失败处理策略: STOP/RETRY/SKIP")
    private String failureStrategy;

    @Schema(description = "备注")
    private String remark;
}