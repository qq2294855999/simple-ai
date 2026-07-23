package com.simple.ai.common.dto.agentDefinition;

import com.simple.common.mp.common.enums.Status;
import com.simple.common.mp.page.PageBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(title = "智能体定义(agent_definition)列表请求参数")
public class PageAgentDefinitionRequest extends PageBase {

    @Schema(description = "名称")
    private String name;

    @Schema(description = "定义描述")
    private String definitionDesc;

    @Schema(description = "第一铁律")
    private String firstPrinciple;

    @Schema(description = "第二规则")
    private String secondRule;

    @Schema(description = "第三技能")
    private String thirdSkill;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "修改人")
    private String updateBy;

    @Schema(description = "状态")
    private Status status;

    @Schema(description = "扩展")
    private String reserve;

    @Schema(description = "备注")
    private String remark;
}

