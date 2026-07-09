package com.simple.ai.common.dto.atomicCommand;

import java.util.Date;

import com.simple.common.mp.page.PageBase;
import io.swagger.v3.oas.annotations.media.Schema;
import com.simple.common.mp.common.enums.DeleteState;
import com.simple.common.mp.common.enums.Status;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(title = "原子命令(atomic_command)列表请求参数")
public class PageAtomicCommandRequest extends PageBase {

    @Schema(description = "名称")
    private String name;

    @Schema(description = "命令")
    private String command;

    @Schema(description = "作用")
    private String func;

    @Schema(description = "状态")
    private Status status;

    @Schema(description = "同步")
    private Integer sync;
}

