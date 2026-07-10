package com.simple.ai.common.dto.task;

import com.simple.ai.common.dto.taskDetail.PageTaskDetailResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 浠诲姟鑱氬悎璇︽儏鍝嶅簲銆? *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "浠诲姟鑱氬悎璇︽儏鍝嶅簲")
public class InfoAggregateTaskResponse {

    @Schema(description = "浠诲姟鍩虹淇℃伅")
    private PageAggregateTaskResponse task;

    @Schema(description = "浠诲姟璇︽儏閾捐矾")
    private List<PageTaskDetailResponse> details;
}
