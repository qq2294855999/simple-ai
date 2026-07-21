package com.simple.ai.common.dto.agentMemoryVersionDetail;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 记忆版本步骤(agent_memory_version_detail)分页明细响应。
 *
 * @author qty
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(chain = true)
@Schema(title = "记忆版本步骤分页明细响应")
public class PageAgentMemoryVersionDetailResponse {

    @Schema(description = "主键")
    private String id;

    @Schema(description = "记忆版本ID")
    private String versionId;

    @Schema(description = "步骤序号")
    private Integer sequenceNo;

    @Schema(description = "原子命令ID")
    private String atomicCommandId;

    @Schema(description = "原子命令编码")
    private String atomicCommandCode;

    @Schema(description = "参数模板")
    private String argsTemplate;

    @Schema(description = "执行前随机延迟最小值(毫秒)")
    private Integer delayMinMs;

    @Schema(description = "执行前随机延迟最大值(毫秒)")
    private Integer delayMaxMs;

    @Schema(description = "命令超时时间(毫秒)")
    private Integer timeoutMs;

    @Schema(description = "幂等键")
    private String idempotencyKey;

    @Schema(description = "成功断言规则")
    private String successAssertion;

    @Schema(description = "失败处理策略")
    private String failureStrategy;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "修改时间")
    private Date updateTime;
}
