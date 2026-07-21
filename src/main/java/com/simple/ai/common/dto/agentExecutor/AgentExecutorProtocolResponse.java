package com.simple.ai.common.dto.agentExecutor;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 执行器协议(SEP v1.0)明细响应。
 * <p>描述 Simple Executor Protocol v1.0 的完整协议定义，包括消息结构、消息类型、字段说明、
 * 内置系统命令和通信流程。</p>
 *
 * @author qty
 */
@Data
@Accessors(chain = true)
@Schema(title = "执行器协议(SEP v1.0)明细响应")
public class AgentExecutorProtocolResponse {

    /**
     * 协议名称。
     */
    @Schema(description = "协议名称")
    private String protocolName;

    /**
     * 协议版本。
     */
    @Schema(description = "协议版本")
    private String protocolVersion;

    /**
     * 协议描述。
     */
    @Schema(description = "协议描述")
    private String description;

    /**
     * 外层消息结构。
     */
    @Schema(description = "外层消息结构说明")
    private MessageStructure outerStructure;

    /**
     * 消息类型列表。
     */
    @Schema(description = "消息类型列表")
    private List<MessageTypeInfo> messageTypes;

    /**
     * 内置系统命令列表。
     */
    @Schema(description = "内置系统命令列表")
    private List<SystemCommandInfo> systemCommands;

    /**
     * 通信流程说明。
     */
    @Schema(description = "通信流程说明")
    private String communicationFlow;

    /**
     * 消息外层结构。
     */
    @Data
    @Accessors(chain = true)
    @Schema(title = "消息外层结构")
    public static class MessageStructure {

        @Schema(description = "结构描述")
        private String description;

        @Schema(description = "JSON 示例")
        private String jsonExample;

        @Schema(description = "字段说明列表")
        private List<FieldInfo> fields;
    }

    /**
     * 字段说明。
     */
    @Data
    @Accessors(chain = true)
    @Schema(title = "字段说明")
    public static class FieldInfo {

        @Schema(description = "字段名")
        private String name;

        @Schema(description = "字段类型")
        private String type;

        @Schema(description = "是否必填")
        private Boolean required;

        @Schema(description = "字段描述")
        private String description;
    }

    /**
     * 消息类型说明。
     */
    @Data
    @Accessors(chain = true)
    @Schema(title = "消息类型说明")
    public static class MessageTypeInfo {

        @Schema(description = "消息类型名称")
        private String typeName;

        @Schema(description = "消息方向（Server → Executor / Executor → Server）")
        private String direction;

        @Schema(description = "消息描述")
        private String description;

        @Schema(description = "字段说明列表")
        private List<FieldInfo> fields;

        @Schema(description = "JSON 示例")
        private String jsonExample;
    }

    /**
     * 系统命令说明。
     */
    @Data
    @Accessors(chain = true)
    @Schema(title = "系统命令说明")
    public static class SystemCommandInfo {

        @Schema(description = "命令编码")
        private String commandCode;

        @Schema(description = "命令描述")
        private String description;

        @Schema(description = "参数字段说明")
        private List<FieldInfo> args;

        @Schema(description = "返回数据说明")
        private String resultDescription;

        @Schema(description = "JSON 示例")
        private String jsonExample;
    }
}
