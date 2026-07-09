package com.simple.ai.common.dto.atomicCommand;

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
@Schema(title = "原子命令(atomic_command)分页明细响应")
public class PageAtomicCommandResponse {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "命令")
    private String command;

    @Schema(description = "作用")
    private String role;

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

