# Simple Executor Protocol v1.0

## 协议概述

SEP v1.0 是智能体系统与远程执行器之间的标准通信协议。基于 WebSocket 全双工通道，采用 JSON 格式的 messageType/payload 双层结构，支持批量命令下发、逐项结果回传、心跳保活和内置系统命令。

---

## 外层消息结构

所有 WebSocket 业务消息均使用统一的外层结构包裹，通过 messageType 字段区分消息类型，payload 字段承载具体业务数据。

### JSON 示例

```json
{
	"messageType": "COMMAND_BATCH",
	"payload": {
		...
	}
}
```

### 字段说明

| 字段名      | 类型   | 必填 | 描述                                                                        |
|-------------|--------|------|-----------------------------------------------------------------------------|
| messageType | String | 是   | 协议消息类型，取值：COMMAND_BATCH、COMMAND_RESULT、HEARTBEAT、HEARTBEAT_ACK |
| payload     | Object | 是   | 消息负载，结构随 messageType 变化                                           |

---

## 消息类型

### 1. COMMAND_BATCH

- **方向**：Server → Executor
- **描述**：服务端向执行器下发批量命令。执行器收到后按顺序逐条执行，每条命令执行完毕后单独回传 COMMAND_RESULT。

#### JSON 示例

```json
{
	"dispatchId": "1901234567890",
	"taskId": "1901234567890",
	"clientId": "client_001",
	"stopOnFailure": true,
	"minDelayMs": 100,
	"maxDelayMs": 500,
	"commands": [
		{
			"commandId": "1901234567891",
			"sequenceNo": 10,
			"atomicCommandCode": "window.find",
			"args": {},
			"timeoutMs": 10000,
			"idempotencyKey": "taskId+seq"
		}
	]
}
```

#### 字段说明

| 字段名               | 类型                       | 必填 | 描述                                           |
|----------------------|----------------------------|------|------------------------------------------------|
| dispatchId           | String                     | 是   | 调度ID（雪花ID），唯一标识一次命令调度         |
| taskId               | String                     | 是   | 任务ID，关联的任务主键                         |
| clientId             | String                     | 是   | 目标客户端ID，WebSocket 点对点路由依据         |
| stopOnFailure        | Boolean                    | 否   | 是否失败即停止后续命令执行                     |
| minDelayMs           | Integer                    | 否   | 执行前最小随机延迟(毫秒)，用于模拟人工操作间隔 |
| maxDelayMs           | Integer                    | 否   | 执行前最大随机延迟(毫秒)                       |
| commands             | Array<ExecutorCommandItem> | 是   | 命令列表，按顺序执行                           |
| └─ commandId         | String                     | 是   | 命令ID（雪花ID），用于关联回执                 |
| └─ sequenceNo        | Integer                    | 是   | 步骤序号，从10递增                             |
| └─ atomicCommandCode | String                     | 是   | 原子命令编码，如 window.find、control.click    |
| └─ args              | Map<String,Object>         | 否   | 命令参数，键值对形式                           |
| └─ timeoutMs         | Integer                    | 否   | 命令超时时间(毫秒)，超时视为失败               |
| └─ idempotencyKey    | String                     | 否   | 幂等键，用于去重                               |

---

### 2. COMMAND_RESULT

- **方向**：Executor → Server
- **描述**：执行器向服务端回传单条命令的执行结果。每条命令独立回传，包含执行状态、返回数据和错误详情。

#### JSON 示例

```json
{
	"dispatchId": "1901234567890",
	"taskId": "1901234567890",
	"commandId": "1901234567891",
	"sequenceNo": 10,
	"success": true,
	"message": "窗口已找到",
	"data": {
		"handle": "0x123ABC"
	},
	"error": null,
	"startedAt": "2026-07-21T10:00:00Z",
	"finishedAt": "2026-07-21T10:00:01Z"
}
```

#### 字段说明

| 字段名     | 类型                 | 必填 | 描述                                                             |
|------------|----------------------|------|------------------------------------------------------------------|
| dispatchId | String               | 是   | 调度ID，与 COMMAND_BATCH 中的 dispatchId 对应                    |
| taskId     | String               | 是   | 任务ID，与 COMMAND_BATCH 中的 taskId 对应                        |
| commandId  | String               | 是   | 命令ID，与 ExecutorCommandItem.commandId 对应                    |
| sequenceNo | Integer              | 是   | 步骤序号                                                         |
| success    | Boolean              | 是   | 是否执行成功                                                     |
| message    | String               | 否   | 执行说明，成功时为成功描述，失败时为错误简述                     |
| data       | Map<String,Object>   | 否   | 返回数据，包含执行结果的关键信息                                 |
| error      | ExecutorCommandError | 否   | 错误详情，失败时包含 error.code、error.detail、error.recoverable |
| startedAt  | Instant              | 是   | 开始执行时间（ISO8601 UTC）                                      |
| finishedAt | Instant              | 是   | 执行完成时间（ISO8601 UTC）                                      |

---

### 3. HEARTBEAT

- **方向**：Server → Executor
- **描述**：服务端向执行器发送心跳探测。执行器收到后应立即回复 HEARTBEAT_ACK。心跳用于检测 WebSocket 连接的活跃状态，超时未回复视为断连。

#### JSON 示例

```json
{
	"messageType": "HEARTBEAT",
	"payload": {}
}
```

#### 字段说明

| 字段名  | 类型   | 必填 | 描述                 |
|---------|--------|------|----------------------|
| payload | Object | 否   | 心跳消息负载为空对象 |

---

### 4. HEARTBEAT_ACK

- **方向**：Executor → Server
- **描述**：执行器对心跳探测的确认回复，表示执行器仍在运行且 WebSocket 连接正常。

#### JSON 示例

```json
{
	"messageType": "HEARTBEAT_ACK",
	"payload": {}
}
```

#### 字段说明

| 字段名  | 类型   | 必填 | 描述                     |
|---------|--------|------|--------------------------|
| payload | Object | 否   | 心跳确认消息负载为空对象 |

---

## 内置系统命令

### system.capability

- **描述**：返回执行器支持的全部原子命令列表。握手鉴权通过后，服务端自动下发此命令以同步执行器能力清单。
- **参数**：（无参数）— system.capability 不需要额外参数
- **返回数据**：返回原子命令列表，每条包含命令 code、name、description、argsSchema、resultSchema、riskLevel、isIdempotent 等元信息

#### JSON 示例

```json
{
	"messageType": "COMMAND_BATCH",
	"payload": {
		"dispatchId": "system",
		"commands": [
			{
				"commandId": "sys_cap_001",
				"sequenceNo": 0,
				"atomicCommandCode": "system.capability",
				"args": {}
			}
		]
	}
}
```

---

### system.health

- **描述**：返回执行器的健康状态，包括进程状态、系统资源使用情况和当前检查时间。
- **参数**：（无参数）— system.health 不需要额外参数
- **返回数据**：返回健康检查结果，包含 status（健康状态）、checkedAt（检查时间）、process（进程信息）等

#### JSON 示例

```json
{
	"messageType": "COMMAND_BATCH",
	"payload": {
		"dispatchId": "system",
		"commands": [
			{
				"commandId": "sys_health_001",
				"sequenceNo": 0,
				"atomicCommandCode": "system.health",
				"args": {}
			}
		]
	}
}
```

---

## 通信流程

1. WebSocket 连接建立，携带 type=agent-executor、cliKey=clientId、token=secret 鉴权参数
2. 服务端鉴权通过后，自动下发 system.capability 命令
3. 执行器执行 system.capability 并返回支持的命令列表
4. 服务端 upsert 原子命令表，同步执行器能力
5. 正常运行期间，服务端按需下发 COMMAND_BATCH 批量命令
6. 执行器逐条执行命令，每条完成后回传 COMMAND_RESULT
7. 服务端按 commandId 完成等待器，AI 根据结果继续决策
8. 服务端定期发送 HEARTBEAT，执行器回复 HEARTBEAT_ACK
9. 断连后服务端清理等待器，执行器自动重连后重新握手