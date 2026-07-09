package com.simple.ai.common.dto.task;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.simple.common.mp.common.enums.DeleteState;
import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

import lombok.experimental.Accessors;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(chain = true)
@Schema(title = "任务(task)明细响应")
public class InfoTaskResponse {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "智能体记忆主键")
    private String agentMemoryId;

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "执行状态")
    private Integer executionStatus;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "修改时间")
    private Date updateTime;
}

