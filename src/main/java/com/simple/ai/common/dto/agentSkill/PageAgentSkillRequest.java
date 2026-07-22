package com.simple.ai.common.dto.agentSkill;

import com.simple.common.mp.common.enums.Status;
import com.simple.common.mp.page.PageBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(title = "智能体技能(agent_skill)列表请求参数")
public class PageAgentSkillRequest extends PageBase {

    @Schema(description = "智能体ID")
    private String agentId;

    @Schema(description = "定义描述")
    private String definitionDesc;

    @Schema(description = "执行内容")
    private String execContent;

    @Schema(description = "返回的数据格式")
    private String returnDataFormat;

    @Schema(description = "状态")
    private Status status;

    @Schema(description = "扩展")
    private String reserve;

    @Schema(description = "备注")
    private String remark;
}

