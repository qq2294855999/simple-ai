package com.simple.ai.common.dto.taskDetail;

import java.util.Date;

import com.simple.common.mp.page.PageBase;
import io.swagger.v3.oas.annotations.media.Schema;
import com.simple.common.mp.common.enums.DeleteState;
import com.simple.common.mp.common.enums.Status;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(title = "任务详情(task_detail)单条数据请求参数")
public class DeleteTaskDetailRequest {

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

