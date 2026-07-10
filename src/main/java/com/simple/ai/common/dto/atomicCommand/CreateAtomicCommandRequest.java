package com.simple.ai.common.dto.atomicCommand;

import java.util.Date;

import com.simple.common.mp.common.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Map;

@Data
@Accessors(chain = true)
@Schema(title = "原子命令(atomic_command)创建请求参数")
public class CreateAtomicCommandRequest {

    @Schema(description = "名称")
    @NotEmpty(message = "名称不能为空")
    private String name;

    @Schema(description = "命令")
    @NotEmpty(message = "命令不能为空")
    private String command;

    @Schema(description = "作用")
    @NotEmpty(message = "作用不能为空")
    private String role;

    @Schema(description = "智能体技能ID")
    private String skillId;

    @Schema(description = "备注")
    private String remark;
}

