package com.simple.ai.common.dto.executionEvent;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 执行事件(execution_event)删除请求参数。
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "执行事件删除请求参数")
public class DeleteExecutionEventRequest {

    @Schema(description = "事件主键")
    @NotEmpty(message = "事件主键不能为空")
    private String id;
}