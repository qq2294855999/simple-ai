# Prompt XML 格式改造 - 开发计划

## 当前恢复入口

- 当前阶段：阶段三，代码实现。
- 当前进行中：[-] 步骤1: 修改 appendAgentDefinition 方法。
- 下一步：逐个修改 6 个 append* 方法，然后修改 buildPromptContent 和 buildUserContent。

## 重点业务流程

将发送给大模型的 prompt 从 Markdown 格式（`#`、`##` 标题）改为 XML 标签包裹格式，提升模型对信息区块的定位精度，同时修复 sessionSummary 重复注入 bug。

## 执行状态清单

- [x] 步骤1: 修改 appendAgentDefinition → XML 标签
- [x] 步骤2: 修改 appendRules → XML 标签
- [x] 步骤3: 修改 appendSkills → XML 标签
- [x] 步骤4: 修改 appendSubAgentRelations → XML 标签
- [x] 步骤5: 修改 appendMemories → XML 标签
- [x] 步骤6: 修改 appendExecutors → XML 标签
- [x] 步骤7: 修改 buildPromptContent → 删除末尾 appendSessionSummary
- [x] 步骤8: 修改 SpringAiAgentAiClient.buildUserContent → XML 标签 + 修复重复注入
- [x] 步骤9: mvn clean package 编译验证
- [x] 步骤10: code-inspector 深度自检

## 重要文件索引

| 文件                          | 路径                                                                           | 改动类型 | 说明                    |
|-------------------------------|--------------------------------------------------------------------------------|----------|-------------------------|
| AgentContextAssembler         | src/main/java/com/simple/ai/service/agent/AgentContextAssembler.java           | 修改     | 7个方法改为XML格式      |
| SpringAiAgentAiClient         | src/main/java/com/simple/ai/service/agent/SpringAiAgentAiClient.java           | 修改     | buildUserContent改为XML |
| AgentIronRuleConstant         | src/main/java/com/simple/ai/common/constant/AgentIronRuleConstant.java         | 不变     | 铁律内容保持原样        |
| DefaultCommandDispatchService | src/main/java/com/simple/ai/service/command/DefaultCommandDispatchService.java | 不变     | 仅消费promptContent     |