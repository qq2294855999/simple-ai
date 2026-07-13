# 本地运行与部署准备说明

## 配置安全原则

[`application-local.yaml`](../src/main/resources/application-local.yaml) 仅保留 Spring Boot 属性与环境变量引用，不包含数据库地址、账户、密码、AI Key、对象存储密钥或 JWT 密钥。可提交的字段模板见 [`application-local.example.yaml`](../src/main/resources/application-local.example.yaml)。

禁止将真实配置写入 YAML、HTTP 回归文件、前端源码、日志或计划文档。部署平台应使用 Secret 注入；本机仅在当前终端或受保护的本地环境变量文件中设置值。

## 必填环境变量

| 分类 | 环境变量 | 缺失行为 |
|---|---|---|
| PostgreSQL | `SPRING_DATASOURCE_URL`、`SPRING_DATASOURCE_USERNAME`、`SPRING_DATASOURCE_PASSWORD` | 数据源不能建立，应用启动失败；不应伪造可用状态。 |
| Redis | `SPRING_DATA_REDIS_HOST`、`SPRING_DATA_REDIS_PASSWORD` | Redis 会话摘要属于辅助上下文；写入失败会记录告警，但不改变已经成功完成的核心任务状态。 |
| Spring AI | `SPRING_AI_OPENAI_API_KEY`、`SPRING_AI_OPENAI_CHAT_MODEL` | 模型请求无法完成；聊天路径必须落库 `SYSTEM_ERROR` 并发送 `CHAT_FAILED`。 |
| RabbitMQ | `SPRING_RABBITMQ_ADDRESSES`、`SPRING_RABBITMQ_USERNAME`、`SPRING_RABBITMQ_PASSWORD` | MQ 自动配置或事件实现不可用，应用按 Spring Boot 实际自动配置策略失败。 |
| 对象存储 | `SIMPLE_ANNEX_SERVER_URL`、`SIMPLE_ANNEX_ACCESS_KEY`、`SIMPLE_ANNEX_ACCESS_SECRET` | 附件功能不可用，不影响不使用附件的页面。 |
| 认证 | `SIMPLE_AUTH_HS_KEY` | 认证组件无法安全初始化，不得用仓库中的固定密钥替代。 |

`SPRING_DATA_REDIS_PORT`、`SPRING_DATA_REDIS_DATABASE`、连接池参数、`SERVER_PORT`、`SPRING_AI_OPENAI_BASE_URL` 等拥有非敏感默认值；其余变量应在目标环境显式提供。

## 本地启动前检查

- 使用 `local` profile，并确保 `SPRING_DATASOURCE_URL` 指向隔离测试库，而非生产库。
- 确认 PostgreSQL、Redis、RabbitMQ、AI 服务和所选模型对本机或部署节点可达。
- 前端开发服务器在 `5173` 运行，Vite 把 `/api` 代理至后端 `8000`；生产部署由反向代理将 `/api` 转发至相同后端上下文。REST 与 POST SSE 均只使用 `/api` 根路径。
- 启动命令和环境变量示例不得包含真实值；实际命令由部署平台 Secret 或受保护终端环境提供。

## 聊天 SSE 运行时边界

- 前端取消请求仅停止浏览器等待和 loading 状态；服务端已经接受并开始处理的消息仍按审计语义完成最终消息或失败消息持久化。
- 服务端 SSE 有有限超时。超时、执行器拒绝、数据库连接失败和 Spring AI 调用失败都必须以 `CHAT_FAILED` 或 HTTP 失败结束，前端不得继续显示 loading，也不得把临时 token 当作成功回复。
- SSE 成功仅以 `MESSAGE_COMPLETED` 为准；`AI_TOKEN` 只代表临时片段。失败仅以 `CHAT_FAILED` 或 HTTP/网络异常为准，并在刷新会话消息后显示持久化的 `SYSTEM_ERROR`。

## PostgreSQL 迁移前置条件

仅对已有库执行 [`agent-command-dispatch-postgresql.sql`](sql/agent-command-dispatch-postgresql.sql)；[`agent-design-full-postgresql.sql`](sql/agent-design-full-postgresql.sql) 含全量建表的 `DROP TABLE`，只允许用于新建、可销毁数据库。

在维护窗口完成以下操作后，才可执行增量脚本：

- 对目标库完成可验证备份并记录恢复演练方式。
- 核对 `atomic_command.skill_id` 与 `task.agent_id` 当前是否已存在及其数据量。
- 审阅 `UPDATE task ... FROM agent_memory` 的预期回填行数，并确认空 `agent_id` 是允许回填的目标集。
- 审阅占位原子命令 ID 是否与现有业务数据冲突；脚本通过主键 `WHERE NOT EXISTS` 保持重复执行安全。
- 在事务策略、锁等待和索引建立影响得到 DBA 确认后执行；本仓库治理步骤不执行迁移。

## 真实 SSE 联调恢复行

准备可达、隔离且已备份的 PostgreSQL 测试库，提供 PostgreSQL 执行器、全部必填环境变量、有效 `SPRING_AI_OPENAI_API_KEY`、可用 `SPRING_AI_OPENAI_CHAT_MODEL` 与已启用智能体；审阅并执行聊天迁移后，从 [`智能体人机流式聊天回归.http`](http/智能体人机流式聊天回归.http) 第 01 步顺序运行至第 05 步。
