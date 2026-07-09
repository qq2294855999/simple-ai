package com.simple.ai.common.dto.taskDetail;

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
@Schema(title = "任务详情(task_detail)分页明细响应")
public class PageTaskDetailResponse {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "任务主键")
    private String taskId;

    @Schema(description = "序号")
    private Integer sequence;

    @Schema(description = "步骤名称")
    private String stepName;

    @Schema(description = "步骤类型")
    private String stepType;

    @Schema(description = "步骤内容")
    private String stepContent;

    @Schema(description = "请求参数")
    private String requestParams;

    @Schema(description = "返回参数")
    private String responseParams;

    @Schema(description = "执行状态")
    private Integer executionStatus;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "修改时间")
    private Date updateTime;
}

