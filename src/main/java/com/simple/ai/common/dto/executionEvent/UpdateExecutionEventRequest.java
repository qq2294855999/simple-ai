package com.simple.ai.common.dto.executionEvent;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(title = "执行事件(execution_event)修改请求参数")
public class UpdateExecutionEventRequest extends CreateExecutionEventRequest {

    @Schema(description = "事件主键，UUID")
    @NotEmpty(message = "事件主键，UUID不能为空")
    private String id;

}

