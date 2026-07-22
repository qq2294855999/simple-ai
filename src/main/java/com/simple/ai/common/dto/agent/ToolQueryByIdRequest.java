package com.simple.ai.common.dto.agent;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * AI 工具按 ID 查询的通用请求参数。
 *
 * @author qty
 */
@Data
@Schema(title = "工具按ID查询请求参数")
public class ToolQueryByIdRequest {

    @Schema(description = "数据主键ID")
    private String id;
}
