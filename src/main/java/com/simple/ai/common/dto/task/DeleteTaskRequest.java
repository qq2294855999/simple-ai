package com.simple.ai.common.dto.task;

import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@Accessors(chain = true)
@Schema(title = "任务(task)单条数据请求参数")
public class DeleteTaskRequest {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "智能体记忆主键")
    private String agentMemoryId;

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "父任务ID")
    private String parentTaskId;

    @Schema(description = "下一个任务ID")
    private String nextTaskId;

    @Schema(description = "步骤类型：智能体步骤类型")
    private String stepType;

    @Schema(description = "分支条件")
    private String branchCondition;

    @Schema(description = "分支路由")
    private String branchRoute;

    @Schema(description = "请求参数")
    private String requestParams;

    @Schema(description = "返回参数")
    private String returnParams;

    @Schema(description = "执行状态")
    private String execStatus;

    @Schema(description = "失败原因")
    private String failureReason;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "修改时间")
    private Date updateTime;

    @Schema(description = "状态")
    private Status status;

    @Schema(description = "扩展")
    private String reserve;

    @Schema(description = "备注")
    private String remark;

}

