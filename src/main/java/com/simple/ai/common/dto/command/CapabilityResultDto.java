package com.simple.ai.common.dto.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * system.capability 命令返回的数据结构。
 * <p>用于解析执行器通过 COMMAND_RESULT.data 返回的命令能力列表，
 * 包含每个原子命令的元信息（code、name、description 等）。</p>
 *
 * @author qty
 */
@Data
@Schema(title = "执行器能力查询结果")
public class CapabilityResultDto {

    /**
     * 执行器支持的原子命令列表
     */
    @Schema(description = "执行器支持的原子命令列表")
    private List<CommandItem> commands;

    /**
     * 单个原子命令的元信息。
     */
    @Data
    @Schema(title = "原子命令元信息")
    public static class CommandItem {

        /**
         * 命令编码（如 window.find、system.capability）
         */
        @Schema(description = "命令编码")
        private String code;

        /**
         * 命令名称（如 查找窗口）
         */
        @Schema(description = "命令名称")
        private String name;

        /**
         * 命令描述
         */
        @Schema(description = "命令描述")
        private String description;

        /**
         * 参数 Schema（JSON Schema 格式）
         */
        @Schema(description = "参数 Schema")
        private Map<String, Object> argsSchema;

        /**
         * 返回值 Schema（JSON Schema 格式）
         */
        @Schema(description = "返回值 Schema")
        private Map<String, Object> resultSchema;

        /**
         * 风险等级：low / medium / high
         */
        @Schema(description = "风险等级")
        private String riskLevel;

        /**
         * 是否幂等
         */
        @Schema(description = "是否幂等")
        private Boolean isIdempotent;
    }
}