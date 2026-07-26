package com.simple.ai.common.dto.task;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 任务(task)删除请求参数。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "任务删除请求参数")
public class DeleteTaskRequest {

    @Schema(description = "主键")
    @NotEmpty(message = "主键不能为空")
    private String id;
}