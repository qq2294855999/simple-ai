package com.simple.ai.common.dto.agentMemory;

import com.simple.ai.common.entity.agentMemoryStep.AgentMemoryStep;
import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 智能体记忆详情响应
 *
 * @author qty
 */
@Data
@Schema(title = "智能体记忆详情响应")
public class InfoAgentMemoryResponse {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "智能体ID")
    private String agentId;

    @Schema(description = "记忆名称模板")
    private String memoryName;

    @Schema(description = "参数定义JSON")
    private String paramsDefinition;

    @Schema(description = "版本号")
    private Integer versionNo;

    @Schema(description = "版本状态")
    private Integer versionStatus;

    @Schema(description = "来源任务ID")
    private String sourceTaskId;

    @Schema(description = "记忆摘要")
    private String summary;

    @Schema(description = "创建原因")
    private String createReason;

    @Schema(description = "记忆步骤列表")
    private List<AgentMemoryStep> steps;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "修改时间")
    private Date updateTime;

    @Schema(description = "状态")
    private Status status;

    @Schema(description = "备注")
    private String remark;
}