package com.simple.ai.common.dto.agentMemory;

import java.util.Date;

import com.simple.common.mp.page.PageBase;
import io.swagger.v3.oas.annotations.media.Schema;
import com.simple.common.mp.common.enums.DeleteState;
import com.simple.common.mp.common.enums.Status;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(title = "智能体记忆(agent_memory)单条数据请求参数")
public class FindOneAgentMemoryRequest {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "智能体ID")
    private String agentId;

    @Schema(description = "记忆名称")
    private String memoryName;

    @Schema(description = "步骤名称")
    private String stepName;

    @Schema(description = "触发条件")
    private String triggerCondition;

    @Schema(description = "触发动作")
    private String triggerAction;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "修改时间")
    private Date updateTime;

    @Schema(description = "状态")
    private Status status;

    @Schema(description = "扩展")
    private String reserver;

    @Schema(description = "备注")
    private String remark;

}

