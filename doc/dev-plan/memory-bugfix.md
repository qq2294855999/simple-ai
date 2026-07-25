# 记忆体系业务逻辑完善 - 开发计划

## 当前恢复入口

- 当前阶段：深度自检完成。
- 当前状态：8/8 步骤已完成。
- 下一步：无待处理任务。

## 问题清单

| # | 严重度 | 位置                           | 问题                                                  | 根因                                                         |
|---|--------|--------------------------------|-------------------------------------------------------|--------------------------------------------------------------|
| 1 | 高     | DefaultMemoryDistiller:92-97   | 修订场景版本号可能与已有版本冲突                      | calcNextVersionNo 仅基于旧记忆版本号+1，未查库确认最大版本号 |
| 2 | 中     | DefaultMemoryDistiller:88-91   | 修订场景仅退役PUBLISHED旧记忆，DRAFT状态不退役        | 条件判断 Integer.valueOf(2).equals() 过于严格                |
| 3 | 高     | DefaultMemoryExecutor:119-127  | RETRY重试成功时 hasFailure=false 覆盖了前面步骤的失败 | hasFailure是全局标志，重试成功不应清除之前步骤的失败         |
| 4 | 高     | DefaultMemoryExecutor:125-126  | RETRY重试成功时用substring截断拼接结果字符串          | 硬编码字符串操作，脆弱且易出错                               |
| 5 | 中     | DefaultMemoryExecutor:164-168  | Thread.sleep同步阻塞请求线程                          | 高并发下耗尽线程池                                           |
| 6 | 中     | DefaultMemoryExecutor:202-218  | 每个步骤new Thread()做超时控制                        | 无线程池、无资源限制                                         |
| 7 | 高     | DefaultMemoryDistiller:252     | AI返回的argsTemplate被双重JSON序列化                  | JsonUtils.toJsonStr() 对已解析的对象再次序列化               |
| 8 | 低     | DefaultMemoryDistiller:148-152 | AI调用未传递userId和sessionId                         | 硬编码为空字符串                                             |

## 执行状态清单

- [x] 步骤1: 修复问题7 - DefaultMemoryDistiller.parseDistillResult ()中argsTemplate双重序列化问题
- [x] 步骤2: 修复问题1 - DefaultMemoryDistiller.calcNextVersionNo ()改为查询数据库获取最大版本号
- [x] 步骤3: 修复问题2 - DefaultMemoryDistiller.distill ()修订场景退役逻辑，DRAFT/PUBLISHED均应退役
- [x] 步骤4: 修复问题8 - DefaultMemoryDistiller.identifyParameters ()传递userId和sessionId
- [x] 步骤5: 修复问题3+4 - DefaultMemoryExecutor RETRY策略的hasFailure标志位和结果字符串拼接逻辑
- [x] 步骤6: 修复问题5+6 - DefaultMemoryExecutor使用CompletableFuture+共享线程池替代Thread.sleep和new Thread
- [x] 步骤7: 编译验证 mvn clean package
- [x] 步骤8: 深度自检 code-inspector

## 重要文件索引

| 文件                   | 路径                                                                   | 改动类型 | 说明                                             |
|------------------------|------------------------------------------------------------------------|----------|--------------------------------------------------|
| DefaultMemoryDistiller | src/main/java/com/simple/ai/service/memory/DefaultMemoryDistiller.java | 修改     | 修复版本号计算、退役逻辑、双重序列化、AI调用参数 |
| DefaultMemoryExecutor  | src/main/java/com/simple/ai/service/memory/DefaultMemoryExecutor.java  | 修改     | 修复RETRY策略、线程池替代                        |