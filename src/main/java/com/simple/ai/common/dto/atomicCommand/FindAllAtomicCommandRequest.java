package com.simple.ai.common.dto.atomicCommand;

import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

@Data
@Accessors(chain = true)
@Schema(title = "原子命令(atomic_command)列表查询参数")
public class FindAllAtomicCommandRequest {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "命令")
    private String command;

    @Schema(description = "命令编码列表，用于批量IN查询")
    private List<String> commands;

    @Schema(description = "角色")
    private String role;

    @Schema(description = "智能体技能ID")
    private String skillId;

    /**
     * 智能体技能ID列表，用于批量查询。
     */
    @Schema(description = "智能体技能ID列表")
    private List<String> skillIds;

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
