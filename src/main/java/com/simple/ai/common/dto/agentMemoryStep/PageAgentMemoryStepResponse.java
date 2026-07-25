package com.simple.ai.common.dto.agentMemoryStep;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 智能体记忆步骤(agent_memory_step)分页响应。
 *
 * @author qty
 */
@Data
@Schema(title = "智能体记忆步骤(agent_memory_step)分页响应")
public class PageAgentMemoryStepResponse {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "记忆ID")
    private String memoryId;

    @Schema(description = "步骤序号")
    private Integer sequenceNo;

    @Schema(description = "原子命令主键")
    private String atomicCommandId;

    @Schema(description = "原子命令编码")
    private String atomicCommandCode;

    @Schema(description = "步骤名称")
    private String stepName;

    @Schema(description = "参数模板JSON")
    private String argsTemplate;

    @Schema(description = "执行前延迟最小值（毫秒）")
    private Integer delayMinMs;

    @Schema(description = "执行前延迟最大值（毫秒）")
    private Integer delayMaxMs;

    @Schema(description = "命令超时时间（毫秒）")
    private Integer timeoutMs;

    @Schema(description = "成功断言规则")
    private String successAssertion;

    @Schema(description = "失败处理策略")
    private String failureStrategy;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "修改时间")
    private Date updateTime;

    @Schema(description = "备注")
    private String remark;
}