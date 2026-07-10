package com.simple.ai.common.dto.atomicCommand;

import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 原子命令聚合分页响应。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "原子命令聚合分页响应")
public class PageAggregateAtomicCommandResponse {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "命令")
    private String command;

    @Schema(description = "作用")
    private String role;

    @Schema(description = "技能ID")
    private String skillId;

    @Schema(description = "技能描述")
    private String skillDesc;

    @Schema(description = "智能体ID")
    private String agentId;

    @Schema(description = "智能体名称")
    private String agentName;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "状态")
    private Status status;

    @Schema(description = "备注")
    private String remark;
}
