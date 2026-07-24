package com.simple.ai.common.dto.agentMemory;

import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 智能体记忆(agent_memory)单条查询请求参数
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "智能体记忆(agent_memory)单条查询请求参数")
public class FindOneAgentMemoryRequest {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "智能体ID")
    private String agentId;

    @Schema(description = "记忆名称模板")
    private String memoryName;

    @Schema(description = "版本状态")
    private Integer versionStatus;

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