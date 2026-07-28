# Simple Executor Protocol v1.2

## 协议概述

SEP v1.2 是智能体系统与远程执行器之间的标准通信协议。基于 WebSocket 全双工通道，采用 **传输层 WebSocketRequest + 业务层 SEP messageType/payload**
的双层结构，支持同步请求-响应（requestId 关联）、批量命令下发、逐项结果回传、心跳保活和内置系统命令。

> **v1.2 变更**：能力清单 `SepCapabilityParameter` 新增 `type` 字段（取值 `string`/`string[]`/`int`/`bool`/`object`），帮助 AI
> 准确构造参数。本协议文档新增完整命令参考章节，每条命令包含参数类型、JSON 格式说明、请求/响应示例。

> **v1.1 变更**：所有消息统一使用 `WebSocketRequest` 传输层格式（`requestId` + `data`）。同步请求（COMMAND_BATCH）携带 `requestId`，执行器回复（COMMAND_RESULT）必须携带相同
> `requestId`；异步消息（HEARTBEAT）无 `requestId`。

---

## 传输层：WebSocketRequest

所有 WebSocket 文本帧均使用统一的传输层结构，`requestId` 用于同步请求-响应关联。

### JSON 格式

```json
{
	"requestId": "雪花ID（同步请求非空，异步消息为 null）",
	"data": {
		// SEP 业务消息，见下方"业务层"章节
	}
}
```

### 字段说明

| 字段名    | 类型   | 必填     | 描述                                                   |
|-----------|--------|----------|--------------------------------------------------------|
| requestId | String | 同步必填 | 同步请求唯一标识（雪花ID），回复时必须携带相同值以关联 |
| data      | Object | 是       | SEP 业务消息（messageType + payload）                  |

### 消息流向规则

| 方向                                | requestId | 说明                                    |
|-------------------------------------|-----------|-----------------------------------------|
| Server → Executor（COMMAND_BATCH）  | 有        | 同步命令，执行器必须按此 requestId 回复 |
| Executor → Server（COMMAND_RESULT） | 有        | 同步回复，requestId 与请求一致          |
| Server → Executor（HEARTBEAT）      | 无        | 异步心跳                                |
| Executor → Server（HEARTBEAT_ACK）  | 无        | 异步心跳回复                            |

---

## 业务层：SEP messageType / payload

传输层 `data` 字段内承载 SEP 业务消息，通过 `messageType` 区分消息类型，`payload` 承载具体业务数据。

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
- **描述**：服务端向执行器下发批量命令（同步请求，携带 requestId）。执行器收到后按顺序逐条执行，每条命令执行完毕后单独回传 COMMAND_RESULT。

#### 完整 JSON 示例（传输层 + 业务层）

```json
{
	"requestId": "1901234567890",
	"data": {
		"messageType": "COMMAND_BATCH",
		"payload": {
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
	}
}
```

#### 字段说明

| 字段名               | 类型                         | 必填 | 描述                                           |
|----------------------|------------------------------|------|------------------------------------------------|
| dispatchId           | String                       | 是   | 调度ID（雪花ID），唯一标识一次命令调度         |
| taskId               | String                       | 是   | 任务ID，关联的任务主键                         |
| clientId             | String                       | 是   | 目标客户端ID，WebSocket 点对点路由依据         |
| stopOnFailure        | Boolean                      | 否   | 是否失败即停止后续命令执行，默认 true          |
| minDelayMs           | Integer                      | 否   | 执行前最小随机延迟(毫秒)，用于模拟人工操作间隔 |
| maxDelayMs           | Integer                      | 否   | 执行前最大随机延迟(毫秒)                       |
| commands             | Array\<ExecutorCommandItem\> | 是   | 命令列表，按顺序执行                           |
| └─ commandId         | String                       | 是   | 命令ID（雪花ID），用于关联回执                 |
| └─ sequenceNo        | Integer                      | 是   | 步骤序号，从10递增                             |
| └─ atomicCommandCode | String                       | 是   | 原子命令编码，如 window.find、element.click    |
| └─ args              | Map\<String,Object\>         | 否   | 命令参数，键值对形式（JSON 对象）              |
| └─ timeoutMs         | Integer                      | 否   | 命令超时时间(毫秒)，超时视为失败，默认 60000   |
| └─ idempotencyKey    | String                       | 否   | 幂等键，用于去重                               |

---

### 2. COMMAND_RESULT

- **方向**：Executor → Server
- **描述**：执行器向服务端回传单条命令的执行结果（同步回复，requestId 必须与请求一致）。每条命令独立回传，包含执行状态、返回数据和错误详情。

#### 完整 JSON 示例（传输层 + 业务层）

```json
{
	"requestId": "1901234567890",
	"data": {
		"messageType": "COMMAND_RESULT",
		"payload": {
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
	}
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
| data       | Map\<String,Object\> | 否   | 返回数据，包含执行结果的关键信息                                 |
| error      | ExecutorCommandError | 否   | 错误详情，失败时包含 error.code、error.detail、error.recoverable |
| startedAt  | Instant              | 是   | 开始执行时间（ISO8601 UTC）                                      |
| finishedAt | Instant              | 是   | 执行完成时间（ISO8601 UTC）                                      |

#### 错误码参考

| error.code        | 含义               | recoverable |
|-------------------|--------------------|-------------|
| INVALID_COMMAND   | 命令编码未注册     | true        |
| INVALID_ARGS      | 参数格式或类型错误 | true        |
| TIMEOUT           | 命令执行超时       | true        |
| INTERNAL_ERROR    | 执行器内部异常     | true        |
| WINDOW_NOT_FOUND  | 目标窗口未找到     | true        |
| ELEMENT_NOT_FOUND | 目标控件未找到     | true        |

---

### 3. HEARTBEAT

- **方向**：Server → Executor
- **描述**：服务端向执行器发送心跳探测（异步消息，无 requestId）。执行器收到后应立即回复 HEARTBEAT_ACK。

#### 完整 JSON 示例

```json
{
	"data": {
		"messageType": "HEARTBEAT",
		"payload": {}
	}
}
```

---

### 4. HEARTBEAT_ACK

- **方向**：Executor → Server
- **描述**：执行器对心跳探测的确认回复（异步消息，无 requestId）。

#### 完整 JSON 示例

```json
{
	"data": {
		"messageType": "HEARTBEAT_ACK",
		"payload": {}
	}
}
```

---

## 通信流程

1. WebSocket 连接建立，携带 type=agent-executor、cliKey=clientId、token=secret 鉴权参数
2. 服务端鉴权通过后，通过 `sendSyncMsg()` 同步下发 system.capability 命令（携带 requestId）
3. 执行器执行 system.capability 并返回支持的命令列表（COMMAND_RESULT 携带相同 requestId）
4. 服务端 `WebSocketServerHandler` 检测 requestId → `WebSocketSyncManager.complete()` 唤醒等待线程 → `sendSyncMsg()` 返回
5. 服务端 upsert 原子命令表，同步执行器能力
6. 正常运行期间，服务端按需 `sendSyncMsg()` 下发 COMMAND_BATCH 批量命令（携带 requestId）
7. 执行器逐条执行命令，每条完成后回传 COMMAND_RESULT（携带相同 requestId）
8. 服务端按 requestId 完成同步等待，AI 根据结果继续决策
9. 服务端定期发送 HEARTBEAT（无 requestId，异步），执行器回复 HEARTBEAT_ACK
10. 断连后服务端清理等待器，执行器自动重连后重新握手

---

## system.capability 能力清单

握手后执行器返回的能力清单。每条命令包含 code、name、description、parameters（含 name、label、type、required）、example（请求示例 JSON，可直接嵌入 COMMAND_BATCH.commands
数组）、riskLevel、isIdempotent。AI 应根据 `type` 字段判断参数是 string（字符串）、string[]（JSON 字符串数组）、int（整数）、bool（布尔值）还是 object（JSON 对象）。

### COMMAND_RESULT 返回示例（能力清单）

```json
{
	"requestId": "1901234567890",
	"data": {
		"messageType": "COMMAND_RESULT",
		"payload": {
			"dispatchId": "system",
			"commandId": "sys_cap_001",
			"sequenceNo": 0,
			"success": true,
			"message": "已返回执行器能力清单。",
			"data": [
				{
					"code": "window.find",
					"name": "查找窗口",
					"description": "按标题关键词或进程名称查找窗口。",
					"parameters": [
						{
							"name": "titleKeyword",
							"label": "窗口标题关键词（如'记事本'、'微信'）",
							"type": "string",
							"required": false
						},
						{
							"name": "processName",
							"label": "进程名称（如'notepad'、'WeChat'，不含.exe）",
							"type": "string",
							"required": false
						}
					],
					"example": "{ \"args\": { \"titleKeyword\": \"微信\" } }",
					"riskLevel": "LOW",
					"isIdempotent": true
				}
			]
		}
	}
}
```

---

## 原子命令参考（完整）

下面按功能分类列出所有可用原子命令。每条命令包含：

- **原子命令编码**（`atomicCommandCode` 字段必须使用的值）
- **参数表**（`args` 中的 JSON key、类型、是否必填、说明）
- **完整请求示例**（可直接嵌入 COMMAND_BATCH.commands 数组）
- **成功响应示例**（COMMAND_RESULT 的 payload 部分）

---

### 窗口命令

#### window.list — 列出窗口

列出当前桌面会话中的所有顶级窗口。

| 参数 | 无 |
|------|----|

**请求示例：**

```json
{
	"commandId": "1901234567891",
	"sequenceNo": 10,
	"atomicCommandCode": "window.list",
	"args": {},
	"timeoutMs": 10000
}
```

**成功响应 data 示例：**

```json
{
	"windows": [
		{
			"handle": "0x123ABC",
			"title": "记事本 - 无标题",
			"processName": "notepad",
			"processId": 12345
		},
		{
			"handle": "0x456DEF",
			"title": "微信",
			"processName": "WeChat",
			"processId": 67890
		}
	]
}
```

---

#### window.find — 查找窗口

按标题关键词或进程名称查找窗口。同时提供两个参数时为 AND 关系（同时满足）。

| 参数名       | 类型   | 必填 | 说明                                           |
|--------------|--------|------|------------------------------------------------|
| titleKeyword | string | 否   | 窗口标题关键词（模糊匹配），如"微信"、"记事本" |
| processName  | string | 否   | 进程名称（不含 .exe），如"WeChat"、"notepad"   |

> **注意**：至少需要提供 titleKeyword 或 processName 之一。

**请求示例：**

```json
{
	"commandId": "1901234567892",
	"sequenceNo": 20,
	"atomicCommandCode": "window.find",
	"args": {
		"titleKeyword": "微信"
	},
	"timeoutMs": 10000
}
```

**成功响应 data 示例：**

```json
{
	"found": true,
	"handle": "0x456DEF",
	"title": "微信",
	"processName": "WeChat",
	"processId": 67890
}
```

---

#### window.activate — 激活窗口

将指定窗口置于前台并激活。

| 参数名       | 类型   | 必填 | 说明                  |
|--------------|--------|------|-----------------------|
| titleKeyword | string | 否   | 窗口标题关键词        |
| processName  | string | 否   | 进程名称（不含 .exe） |

**请求示例：**

```json
{
	"commandId": "1901234567893",
	"sequenceNo": 30,
	"atomicCommandCode": "window.activate",
	"args": {
		"titleKeyword": "微信"
	},
	"timeoutMs": 10000
}
```

**成功响应 data 示例：**

```json
{
	"activated": true,
	"handle": "0x456DEF",
	"title": "微信"
}
```

---

#### window.wait — 等待窗口

等待指定窗口出现，可设置超时时间。

| 参数名       | 类型   | 必填 | 说明                       |
|--------------|--------|------|----------------------------|
| titleKeyword | string | 否   | 窗口标题关键词             |
| processName  | string | 否   | 进程名称（不含 .exe）      |
| timeoutMs    | int    | 否   | 超时时间(毫秒)，默认 30000 |

**请求示例：**

```json
{
	"commandId": "1901234567894",
	"sequenceNo": 40,
	"atomicCommandCode": "window.wait",
	"args": {
		"titleKeyword": "微信",
		"timeoutMs": 30000
	},
	"timeoutMs": 35000
}
```

**成功响应 data 示例：**

```json
{
	"found": true,
	"handle": "0x456DEF",
	"title": "微信",
	"waitedMs": 1250
}
```

---

#### window.minimize — 最小化窗口

| 参数名       | 类型   | 必填 | 说明                  |
|--------------|--------|------|-----------------------|
| titleKeyword | string | 否   | 窗口标题关键词        |
| processName  | string | 否   | 进程名称（不含 .exe） |

**请求示例：**

```json
{
	"commandId": "1901234567895",
	"sequenceNo": 50,
	"atomicCommandCode": "window.minimize",
	"args": {
		"titleKeyword": "微信"
	},
	"timeoutMs": 5000
}
```

---

#### window.maximize — 最大化窗口

| 参数名       | 类型   | 必填 | 说明                  |
|--------------|--------|------|-----------------------|
| titleKeyword | string | 否   | 窗口标题关键词        |
| processName  | string | 否   | 进程名称（不含 .exe） |

**请求示例：**

```json
{
	"commandId": "1901234567896",
	"sequenceNo": 60,
	"atomicCommandCode": "window.maximize",
	"args": {
		"titleKeyword": "微信"
	},
	"timeoutMs": 5000
}
```

---

### 控件树命令

#### tree.snapshot — 控件树快照

获取指定窗口的完整控件树结构，支持深度和节点数限制。

| 参数名       | 类型   | 必填 | 说明                                           |
|--------------|--------|------|------------------------------------------------|
| titleKeyword | string | 否   | 窗口标题关键词                                 |
| processName  | string | 否   | 进程名称（不含 .exe）                          |
| maxDepth     | int    | 否   | 最大抓取深度，0=不限制                         |
| nameFilter   | string | 否   | 按名称模糊过滤控件（名称包含此关键词的才返回） |

**请求示例：**

```json
{
	"commandId": "1901234567897",
	"sequenceNo": 70,
	"atomicCommandCode": "tree.snapshot",
	"args": {
		"titleKeyword": "微信",
		"maxDepth": 3,
		"nameFilter": "登录"
	},
	"timeoutMs": 15000
}
```

**成功响应 data 示例：**

```json
{
	"windowTitle": "微信",
	"processName": "WeChat",
	"totalElements": 45,
	"rootElement": {
		"elementId": "e_001",
		"name": "微信",
		"controlType": "Window",
		"automationId": "MainWindow",
		"className": "WeChatMainWnd",
		"children": [
			...
		]
	}
}
```

---

### 控件命令

#### element.find — 查找控件

在指定窗口中查找匹配条件的控件。

| 参数名               | 类型   | 必填 | 说明                                            |
|----------------------|--------|------|-------------------------------------------------|
| locator.name         | string | 否   | 控件名称（如"确定"、"取消"）                    |
| locator.automationId | string | 否   | 控件自动化ID                                    |
| locator.controlType  | string | 否   | 控件类型（如 Button、Edit、ListItem、ComboBox） |
| locator.className    | string | 否   | 控件类名                                        |

> **注意**：至少提供一个 locator 属性。多个属性为 AND 关系。

**请求示例：**

```json
{
	"commandId": "1901234567898",
	"sequenceNo": 80,
	"atomicCommandCode": "element.find",
	"args": {
		"locator": {
			"name": "登录",
			"controlType": "Button"
		}
	},
	"timeoutMs": 10000
}
```

**成功响应 data 示例：**

```json
{
	"found": true,
	"elementId": "e_042",
	"name": "登录",
	"controlType": "Button",
	"automationId": "btnLogin",
	"boundingRectangle": {
		"x": 300,
		"y": 400,
		"width": 100,
		"height": 36
	},
	"enabled": true
}
```

---

#### element.click — 点击控件

点击指定控件（优先 InvokePattern，兜底坐标点击）。

| 参数名               | 类型   | 必填 | 说明         |
|----------------------|--------|------|--------------|
| locator.name         | string | 否   | 控件名称     |
| locator.automationId | string | 否   | 控件自动化ID |
| locator.controlType  | string | 否   | 控件类型     |

**请求示例：**

```json
{
	"commandId": "1901234567899",
	"sequenceNo": 90,
	"atomicCommandCode": "element.click",
	"args": {
		"locator": {
			"name": "登录",
			"controlType": "Button"
		}
	},
	"timeoutMs": 10000
}
```

---

#### element.hover — 悬停控件

鼠标悬停在指定控件上。

| 参数名               | 类型   | 必填 | 说明         |
|----------------------|--------|------|--------------|
| locator.name         | string | 否   | 控件名称     |
| locator.automationId | string | 否   | 控件自动化ID |
| locator.controlType  | string | 否   | 控件类型     |

---

#### element.setValue — 设置控件值

向可编辑控件写入文本（优先 ValuePattern，失败后剪贴板兜底）。

| 参数名               | 类型   | 必填 | 说明             |
|----------------------|--------|------|------------------|
| locator.name         | string | 否   | 目标控件名称     |
| locator.automationId | string | 否   | 目标控件自动化ID |
| value                | string | 是   | 要写入的文本内容 |

**请求示例：**

```json
{
	"commandId": "1901234567900",
	"sequenceNo": 100,
	"atomicCommandCode": "element.setValue",
	"args": {
		"locator": {
			"name": "用户名",
			"controlType": "Edit"
		},
		"value": "admin"
	},
	"timeoutMs": 10000
}
```

---

#### element.getValue — 获取控件值

读取控件当前值。

| 参数名               | 类型   | 必填 | 说明         |
|----------------------|--------|------|--------------|
| locator.name         | string | 否   | 控件名称     |
| locator.automationId | string | 否   | 控件自动化ID |

**成功响应 data 示例：**

```json
{
	"value": "admin",
	"isReadOnly": false,
	"elementId": "e_030"
}
```

---

#### element.getText — 获取控件文本

读取控件文本内容（Name 属性）。

| 参数名               | 类型   | 必填 | 说明         |
|----------------------|--------|------|--------------|
| locator.name         | string | 否   | 控件名称     |
| locator.automationId | string | 否   | 控件自动化ID |

---

#### element.invoke — 调用控件方法

调用控件的指定 UIA Pattern 方法。

| 参数名               | 类型   | 必填 | 说明         |
|----------------------|--------|------|--------------|
| locator.name         | string | 否   | 控件名称     |
| locator.automationId | string | 否   | 控件自动化ID |

---

### 输入命令

#### input.type — 键盘输入

模拟键盘逐字输入文本到当前焦点控件。

| 参数名 | 类型   | 必填 | 说明             |
|--------|--------|------|------------------|
| text   | string | 是   | 要输入的文本内容 |

**请求示例：**

```json
{
	"commandId": "1901234567901",
	"sequenceNo": 110,
	"atomicCommandCode": "input.type",
	"args": {
		"text": "Hello World"
	},
	"timeoutMs": 10000
}
```

---

#### input.paste — 粘贴文本

从剪贴板粘贴文本到当前焦点控件（适合作业输入大量文本）。

| 参数名 | 类型   | 必填 | 说明             |
|--------|--------|------|------------------|
| text   | string | 是   | 要粘贴的文本内容 |

---

#### input.click — 鼠标点击

在屏幕指定坐标处模拟鼠标左键单击。

| 参数名 | 类型 | 必填 | 说明               |
|--------|------|------|--------------------|
| x      | int  | 是   | 屏幕横坐标（像素） |
| y      | int  | 是   | 屏幕纵坐标（像素） |

**请求示例：**

```json
{
	"commandId": "1901234567902",
	"sequenceNo": 120,
	"atomicCommandCode": "input.click",
	"args": {
		"x": 350,
		"y": 418
	},
	"timeoutMs": 5000
}
```

---

#### input.move — 移动鼠标

移动鼠标到指定屏幕坐标。

| 参数名 | 类型 | 必填 | 说明               |
|--------|------|------|--------------------|
| x      | int  | 是   | 目标横坐标（像素） |
| y      | int  | 是   | 目标纵坐标（像素） |

---

#### input.doubleClick — 双击鼠标

在屏幕指定坐标处左键双击。

| 参数名 | 类型 | 必填 | 说明               |
|--------|------|------|--------------------|
| x      | int  | 是   | 屏幕横坐标（像素） |
| y      | int  | 是   | 屏幕纵坐标（像素） |

---

#### input.rightClick — 右键点击

在屏幕指定坐标处右键单击。

| 参数名 | 类型 | 必填 | 说明               |
|--------|------|------|--------------------|
| x      | int  | 是   | 屏幕横坐标（像素） |
| y      | int  | 是   | 屏幕纵坐标（像素） |

---

#### input.scroll — 滚动鼠标

在指定坐标处模拟鼠标滚轮滚动。

| 参数名 | 类型 | 必填 | 说明                                                             |
|--------|------|------|------------------------------------------------------------------|
| x      | int  | 是   | 屏幕横坐标（像素）                                               |
| y      | int  | 是   | 屏幕纵坐标（像素）                                               |
| delta  | int  | 否   | 滚动增量，正数=向上滚动，负数=向下滚动，默认 -120（WHEEL_DELTA） |

---

#### input.hotkey — 快捷键

模拟快捷键组合按键。

| 参数名 | 类型     | 必填 | 说明                                                                                                                                                                                                                              |
|--------|----------|------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| keys   | string[] | 是   | **按键名称的 JSON 字符串数组**。常用键名：Ctrl、Shift、Alt、LWin（左Win键）、RWin、Enter、Tab、Esc、Space、Backspace、Delete、F1-F12、Home、End、PageUp、PageDown、Up、Down、Left、Right。字母键直接写大写字母（如"A"、"C"、"V"） |

> **关键约束**：`keys` 必须是 JSON 数组格式，即 `["Ctrl","A"]`， **不能**是字符串 `"Ctrl,A"`。

**请求示例（正确）：**

```json
{
	"commandId": "1901234567903",
	"sequenceNo": 130,
	"atomicCommandCode": "input.hotkey",
	"args": {
		"keys": [
			"Ctrl",
			"A"
		]
	},
	"timeoutMs": 5000
}
```

**❌ 错误示例（keys 是字符串而非数组）：**

```json
{
	"args": {
		"keys": "Ctrl,A"
	}
}
```

**更多组合键示例：**

| 操作         | keys 参数        |
|--------------|------------------|
| 全选         | `["Ctrl", "A"]`  |
| 复制         | `["Ctrl", "C"]`  |
| 粘贴         | `["Ctrl", "V"]`  |
| 保存         | `["Ctrl", "S"]`  |
| 打开开始菜单 | `["LWin"]`       |
| 打开运行框   | `["LWin", "R"]`  |
| 关闭窗口     | `["Alt", "F4"]`  |
| 切换窗口     | `["Alt", "Tab"]` |

---

### 验证命令

#### verify.waitText — 等待文本出现

等待指定窗口控件树中出现目标文本。

| 参数名    | 类型   | 必填 | 说明                       |
|-----------|--------|------|----------------------------|
| text      | string | 是   | 等待出现的文本内容         |
| timeoutMs | int    | 否   | 超时时间(毫秒)，默认 10000 |

**请求示例：**

```json
{
	"commandId": "1901234567904",
	"sequenceNo": 140,
	"atomicCommandCode": "verify.waitText",
	"args": {
		"text": "登录成功",
		"timeoutMs": 15000
	},
	"timeoutMs": 20000
}
```

---

#### verify.waitElement — 等待控件出现

等待指定控件出现在窗口中。

| 参数名               | 类型   | 必填 | 说明                       |
|----------------------|--------|------|----------------------------|
| locator.name         | string | 否   | 控件名称                   |
| locator.automationId | string | 否   | 控件自动化ID               |
| timeoutMs            | int    | 否   | 超时时间(毫秒)，默认 10000 |

---

### 进程命令

#### process.list — 进程列表

列出当前系统中的进程。

| 参数名      | 类型   | 必填 | 说明                                                                             |
|-------------|--------|------|----------------------------------------------------------------------------------|
| processName | string | 否   | 进程名称关键词（如"WeChat"、"notepad"，不含 .exe）。为空时返回受限数量的全部进程 |

> **提示**：在执行 `app.locate` 之前，建议先用 `process.list` 确认目标应用进程的实际名称（如到底是 "WeChat" 还是 "WeChatApp"）。

**请求示例：**

```json
{
	"commandId": "1901234567905",
	"sequenceNo": 150,
	"atomicCommandCode": "process.list",
	"args": {
		"processName": "WeChat"
	},
	"timeoutMs": 10000
}
```

**成功响应 data 示例：**

```json
{
	"processes": [
		{
			"processName": "WeChat",
			"processId": 67890,
			"mainWindowTitle": "微信",
			"executablePath": "C:\\Program Files\\Tencent\\WeChat\\WeChat.exe"
		}
	]
}
```

---

#### app.start — 启动应用

按可执行文件路径启动应用程序。

| 参数名           | 类型     | 必填 | 说明                                                                      |
|------------------|----------|------|---------------------------------------------------------------------------|
| filePath         | string   | 是   | 可执行文件完整路径（如 `C:\\Program Files\\Tencent\\WeChat\\WeChat.exe`） |
| arguments        | string[] | 否   | 启动参数列表，JSON 字符串数组                                             |
| workingDirectory | string   | 否   | 工作目录路径，为空时使用可执行文件所在目录                                |

> **提示**：如果不知道 filePath，先用 `app.locate` 定位应用。

**请求示例：**

```json
{
	"commandId": "1901234567906",
	"sequenceNo": 160,
	"atomicCommandCode": "app.start",
	"args": {
		"filePath": "C:\\Program Files\\Tencent\\WeChat\\WeChat.exe"
	},
	"timeoutMs": 15000
}
```

---

#### app.kill — 终止应用

按进程名称终止应用程序。

| 参数名      | 类型   | 必填 | 说明                              |
|-------------|--------|------|-----------------------------------|
| processName | string | 是   | 进程名称（不含 .exe，如"WeChat"） |

**请求示例：**

```json
{
	"commandId": "1901234567907",
	"sequenceNo": 170,
	"atomicCommandCode": "app.kill",
	"args": {
		"processName": "WeChat"
	},
	"timeoutMs": 10000
}
```

---

#### app.locate — 定位应用

自动搜索应用可执行文件路径。先查找正在运行的进程获取路径，如果找不到再扫描常见安装目录（Program Files、Program Files (x86)、Desktop、Start Menu 等，最多 2 层深度）。

| 参数名          | 类型   | 必填 | 说明                                                                                                                                                     |
|-----------------|--------|------|----------------------------------------------------------------------------------------------------------------------------------------------------------|
| applicationName | string | 是   | 应用可执行文件名（如"WeChat"、"notepad"、"cc-switch"，可含 .exe 也可不含）。**建议先用 process.list 获取准确进程名后再传入，避免使用中文名或猜测的名称** |

> **重要**：`applicationName` 必须是可执行文件的基准文件名（basename），不是窗口标题，不是中文名，不是显示名称。例如微信的实际可执行文件名是 `WeChat.exe`，所以应传
> `"WeChat"` 或 `"WeChat.exe"`。

**请求示例：**

```json
{
	"commandId": "1901234567908",
	"sequenceNo": 180,
	"atomicCommandCode": "app.locate",
	"args": {
		"applicationName": "WeChat"
	},
	"timeoutMs": 30000
}
```

**成功响应 data 示例：**

```json
{
	"found": true,
	"paths": [
		"C:\\Program Files\\Tencent\\WeChat\\WeChat.exe"
	],
	"source": "running_process"
}
```

`source` 取值：
- `running_process`：从正在运行的进程中获取
- `file_system`：从文件系统搜索获取
- `not_found`：未找到

**失败响应 data 示例（未找到）：**

```json
{
	"found": false,
	"paths": [],
	"source": "not_found"
}
```

---

#### app.ensure — 确保应用运行

确保应用处于运行状态且窗口已激活到前台。如果进程未运行则自动定位并启动，如果已运行则等待窗口并激活。这是 **启动应用 + 等待窗口 + 激活窗口** 的组合操作，推荐作为应用启动的标准入口。

| 参数名          | 类型   | 必填 | 说明                                                   |
|-----------------|--------|------|--------------------------------------------------------|
| applicationName | string | 是   | 应用可执行文件名（如"WeChat"、"cc-switch"）            |
| titleKeyword    | string | 是   | 窗口标题关键词（用于等待和激活窗口）                   |
| filePath        | string | 否   | 已知可执行文件完整路径，为空时自动调用 app.locate 搜索 |
| waitTimeoutMs   | int    | 否   | 等待窗口超时(毫秒)，默认 30000                         |

**请求示例：**

```json
{
	"commandId": "1901234567909",
	"sequenceNo": 190,
	"atomicCommandCode": "app.ensure",
	"args": {
		"applicationName": "WeChat",
		"titleKeyword": "微信",
		"waitTimeoutMs": 30000
	},
	"timeoutMs": 45000
}
```

**成功响应 data 示例（应用已在运行）：**

```json
{
	"action": "activated_existing",
	"processName": "WeChat",
	"processId": 67890,
	"windowTitle": "微信"
}
```

**成功响应 data 示例（新启动了应用）：**

```json
{
	"action": "started_new",
	"processName": "WeChat",
	"processId": 12345,
	"windowTitle": "微信",
	"executablePath": "C:\\Program Files\\Tencent\\WeChat\\WeChat.exe"
}
```

---

### 屏幕命令

#### screen.capture — 屏幕截图

截取当前桌面屏幕或指定区域，保存到工作区受控目录。

| 参数名     | 类型   | 必填 | 说明                                             |
|------------|--------|------|--------------------------------------------------|
| outputPath | string | 否   | 输出文件名或相对路径，为空时自动生成时间戳文件名 |

**请求示例：**

```json
{
	"commandId": "1901234567910",
	"sequenceNo": 200,
	"atomicCommandCode": "screen.capture",
	"args": {},
	"timeoutMs": 5000
}
```

**成功响应 data 示例：**

```json
{
	"filePath": "screenshots\\screenshot_20260728_113500.png",
	"width": 1920,
	"height": 1080
}
```

---

#### screen.cleanup — 清理屏幕

清理屏幕上的弹窗等干扰元素（关闭意外弹出的对话框）。

| 参数 | 无 |
|------|----|

---

### 文件命令

#### file.readText — 读取文本文件

受限读取文本文件内容（默认最多 4096 字节）。

| 参数名   | 类型   | 必填 | 说明                      |
|----------|--------|------|---------------------------|
| filePath | string | 是   | 文本文件完整路径          |
| maxBytes | int    | 否   | 最大读取字节数，默认 4096 |

---

### OCR 命令

#### element.findText — OCR 查找文字

通过 Windows 内置 OCR 引擎在窗口中查找包含目标文字的位置，支持找到后自动点击。

| 参数名       | 类型   | 必填 | 说明                                       |
|--------------|--------|------|--------------------------------------------|
| titleKeyword | string | 否   | 窗口标题关键词                             |
| processName  | string | 否   | 进程名称（不含 .exe）                      |
| text         | string | 是   | 要查找的文字                               |
| click        | bool   | 否   | 找到后是否自动点击文字中心位置，默认 false |

**请求示例：**

```json
{
	"commandId": "1901234567911",
	"sequenceNo": 210,
	"atomicCommandCode": "element.findText",
	"args": {
		"titleKeyword": "微信",
		"text": "文件传输助手",
		"click": true
	},
	"timeoutMs": 15000
}
```

**成功响应 data 示例：**

```json
{
	"found": true,
	"text": "文件传输助手",
	"confidence": 0.95,
	"boundingRectangle": {
		"x": 200,
		"y": 150,
		"width": 120,
		"height": 30
	},
	"clicked": true
}
```

---

#### ocr.recognizeWindow — OCR 识别窗口

对窗口区域进行全量 OCR 识别，返回所有文字行及其坐标。

| 参数名        | 类型   | 必填 | 说明                      |
|---------------|--------|------|---------------------------|
| titleKeyword  | string | 否   | 窗口标题关键词            |
| processName   | string | 否   | 进程名称（不含 .exe）     |
| minConfidence | int    | 否   | 最小置信度(0-1)，默认 0.5 |

---

### 系统命令

#### system.capability — 能力清单

返回执行器支持的全部原子命令清单。握手后自动调用，无需手动下发。

| 参数 | 无 |
|------|----|

#### system.health — 健康检查

返回执行器健康状态。

| 参数 | 无 |
|------|----|

**成功响应 data 示例：**

```json
{
	"status": "UP",
	"startedAt": "2026-07-28T10:00:00Z",
	"checkedAt": "2026-07-28T11:35:00Z",
	"processId": 21644
}
```

---

## AI 决策指南

### 典型操作流程

以下展示常见自动化操作的命令编排模式。

#### 打开应用并操作

```
1. app.ensure     → 确保微信运行且窗口激活
   { applicationName: "WeChat", titleKeyword: "微信" }
2. tree.snapshot  → 抓取微信窗口控件树
   { titleKeyword: "微信", maxDepth: 3 }
3. element.click  → 点击"通讯录"按钮
   { locator: { name: "通讯录", controlType: "Button" } }
4. verify.waitText → 等待"通讯录"页面加载完成
   { text: "新的朋友", timeoutMs: 5000 }
```

#### 从进程名开始操作（不知道应用路径时）

```
1. process.list   → 先查进程名
   { processName: "WeChat" }
2. app.locate     → 用确认的进程名定位路径
   { applicationName: "WeChat" }   // 来自步骤1的 processName
3. app.start      → 启动应用
   { filePath: "C:\\...\\WeChat.exe" }  // 来自步骤2的 paths[0]
4. window.wait    → 等待窗口出现
   { titleKeyword: "微信" }
```

#### 填写表单

```
1. element.setValue → 填写用户名
   { locator: { name: "用户名", controlType: "Edit" }, value: "admin" }
2. element.setValue → 填写密码
   { locator: { name: "密码", controlType: "Edit" }, value: "****" }
3. element.click    → 点击登录按钮
   { locator: { name: "登录", controlType: "Button" } }
4. verify.waitText  → 验证登录成功
   { text: "欢迎", timeoutMs: 10000 }
```

### 关键约束速查

| 规则                | 说明                                                                                            |
|---------------------|-------------------------------------------------------------------------------------------------|
| `atomicCommandCode` | 必须使用「原子命令参考」中列出的精确编码，不能自创名称（如不能写 "start weixin"）               |
| `args` 类型         | 严格按参数表中 `type` 列构造 JSON 类型。`string[]` 必须用数组 `["a","b"]`，不能用字符串 `"a,b"` |
| `applicationName`   | 是可执行文件 basename（如 "WeChat"），不是窗口标题或中文名                                      |
| `processName`       | 不含 `.exe` 后缀                                                                                |
| locator 参数        | 传入 JSON 对象，如 `{ "locator": { "name": "登录" } }`，不能放在顶层                            |
| 顺序依赖            | 必须先找到窗口再操作控件。一条 COMMAND_BATCH 中的命令顺序执行，前一条的结果会影响后一条的上下文 |

---

## 常见错误排查

### ❌ 错误 1：命令编码不正确

```json
// 错误 — 不能用自然语言
{
	"atomicCommandCode": "start weixin"
}

// 正确 — 使用精确的命令编码
{
	"atomicCommandCode": "app.ensure",
	"args": {
		"applicationName": "WeChat",
		"titleKeyword": "微信"
	}
}
```

或先定位再启动：

```json
{
	"atomicCommandCode": "app.locate",
	"args": {
		"applicationName": "WeChat"
	}
}
// 拿到路径后：
{
	"atomicCommandCode": "app.start",
	"args": {
		"filePath": "C:\\...\\WeChat.exe"
	}
}
```

### ❌ 错误 2：快捷键 keys 参数用了字符串而非数组

```json
// 错误 — keys 是字符串
{
	"atomicCommandCode": "input.hotkey",
	"args": {
		"keys": "Ctrl,A"
	}
}

// 正确 — keys 是字符串数组
{
	"atomicCommandCode": "input.hotkey",
	"args": {
		"keys": [
			"Ctrl",
			"A"
		]
	}
}
```

### ❌ 错误 3：applicationName 用了中文名或窗口标题

```json
// 错误 — 微信的可执行文件名不是"WeChat"的中文名
{
	"atomicCommandCode": "app.locate",
	"args": {
		"applicationName": "微信"
	}
}

// 正确 — 先用 process.list 确认进程名，再传入
// 步骤1: process.list → 得到 processName: "WeChat"
// 步骤2: app.locate
{
	"atomicCommandCode": "app.locate",
	"args": {
		"applicationName": "WeChat"
	}
}
```

### ❌ 错误 4：args 中 locator 参数放在顶层

```json
// 错误 — locator 属性直接放在 args 根层级
{
	"atomicCommandCode": "element.find",
	"args": {
		"name": "登录",
		"controlType": "Button"
	}
}

// 正确 — locator 是一个嵌套对象
{
	"atomicCommandCode": "element.find",
	"args": {
		"locator": {
			"name": "登录",
			"controlType": "Button"
		}
	}
}
```

### ❌ 错误 5：app.locate 未找到应用

```
原因：applicationName 与系统中实际的可执行文件名不匹配。
排查步骤：
1. 执行 process.list 查询相关进程名
2. 使用 process.list 返回的 processName 作为 applicationName
3. 如果进程未运行，检查应用是否已安装在常见目录（Program Files、Program Files (x86)）
4. 如果应用安装在非标准位置，使用 app.start 直接指定 filePath
```

---

## 命令速查表

> **幂等列**：✓ = 幂等（重复执行不产生额外副作用，可安全重试），✗ = 非幂等（每次执行改变状态，重复会有累积效果）

| 分类 | 命令编码            | 说明              | 风险   | 幂等 |
|------|---------------------|-------------------|--------|------|
| 窗口 | window.list         | 列出所有顶级窗口  | LOW    | ✓   |
| 窗口 | window.find         | 查找窗口          | LOW    | ✓   |
| 窗口 | window.activate     | 激活窗口到前台    | HIGH   | ✓   |
| 窗口 | window.wait         | 等待窗口出现      | LOW    | ✓   |
| 窗口 | window.minimize     | 最小化窗口        | HIGH   | ✗   |
| 窗口 | window.maximize     | 最大化窗口        | HIGH   | ✗   |
| 控件 | tree.snapshot       | 控件树快照        | LOW    | ✓   |
| 控件 | element.find        | 查找控件          | LOW    | ✓   |
| 控件 | element.click       | 点击控件          | HIGH   | ✗   |
| 控件 | element.hover       | 悬停控件          | MEDIUM | ✓   |
| 控件 | element.setValue    | 写入控件值        | HIGH   | ✗   |
| 控件 | element.getValue    | 读取控件值        | LOW    | ✓   |
| 控件 | element.getText     | 读取控件文本      | LOW    | ✓   |
| 控件 | element.invoke      | 调用控件方法      | HIGH   | ✗   |
| 输入 | input.type          | 键盘输入文本      | HIGH   | ✗   |
| 输入 | input.paste         | 粘贴文本          | HIGH   | ✗   |
| 输入 | input.click         | 鼠标单击          | HIGH   | ✗   |
| 输入 | input.move          | 移动鼠标          | MEDIUM | ✓   |
| 输入 | input.doubleClick   | 鼠标双击          | HIGH   | ✗   |
| 输入 | input.rightClick    | 鼠标右键          | HIGH   | ✗   |
| 输入 | input.scroll        | 鼠标滚轮          | HIGH   | ✗   |
| 输入 | input.hotkey        | 快捷键            | HIGH   | ✗   |
| 验证 | verify.waitText     | 等待文本出现      | LOW    | ✓   |
| 验证 | verify.waitElement  | 等待控件出现      | LOW    | ✓   |
| 进程 | process.list        | 进程列表          | LOW    | ✓   |
| 进程 | app.start           | 启动应用          | HIGH   | ✗   |
| 进程 | app.kill            | 终止应用          | HIGH   | ✗   |
| 进程 | app.locate          | 定位应用路径      | LOW    | ✓   |
| 进程 | app.ensure          | 确保应用运行      | HIGH   | ✗   |
| 屏幕 | screen.capture      | 屏幕截图          | LOW    | ✓   |
| 屏幕 | screen.cleanup      | 清理弹窗          | HIGH   | ✗   |
| 文件 | file.readText       | 读取文本文件      | LOW    | ✓   |
| OCR  | element.findText    | OCR查找并可选点击 | MEDIUM | ✓   |
| OCR  | ocr.recognizeWindow | OCR识别窗口       | LOW    | ✓   |
| 系统 | system.capability   | 能力清单          | LOW    | ✓   |
| 系统 | system.health       | 健康检查          | LOW    | ✓   |
