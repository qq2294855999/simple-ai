package com.simple.ai.common.dto.atomicCommand;

import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

@Data
@Accessors(chain = true)
@Schema(title = "原子命令(atomic_command)列表请求参数")
public class FindAllAtomicCommandRequest {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "命令")
    private String command;

    @Schema(description = "作用")
    private String role;

    @Schema(description = "智能体技能ID")
    private String skillId;

    /**
     * 智能体技能ID列表（批量查询用）
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
    private String reserver;

    @Schema(description = "备注")
    private String remark;

}

