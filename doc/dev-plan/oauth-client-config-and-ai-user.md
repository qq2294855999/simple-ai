# 需求分析与开发计划 — OAuth 客户端文案定制 + Simple AI 用户体系

## 当前总体状态

- 当前阶段：**全部完成** ✅
- 当前进行中：无
- 下一步：无

---

## 一、需求概述

### 需求一：OAuth 登录页文案按客户端定制

当前 OAuth 登录页标题 `OAuth 管理系统`、副标题 `统一认证授权中心` 硬编码在 [`LoginPageComponent.tsx`](C:\start\simple-common-oauth\simple-common-oauth-frontend\src\components\LoginPageComponent.tsx:132-137)。不同客户端（如 simple-ai）登录时需要显示自己的品牌文案。

**方案**：利用 `sys_project_client.reserve`（`Map<String,Object>` JSON 扩展字段）存储客户端自定义文案。登录接口返回时携带 `clientConfig`，前端动态渲染。

### 需求二：Simple AI 建立自己的用户体系

授权中心 `sys_user` 管理所有客户端共用的基础认证信息。simple-ai 需要自己的 `ai_user` 表存储业务独有信息（如 AI 配额、偏好设置等），以 OAuth 用户 ID 作为主键建立一对一关系。

**方案**：创建用户时先调 OAuth `UserService.create()` → 获得 userId → 以该 userId 为主键创建 `ai_user`。

---

## 二、技术栈

Java + Web（全栈）

---

## 三、业务流程图

### 3.1 文案定制流程

```
客户端管理员在 OAuth 后台配置
   ↓
[sys_project_client.reserve] = {"loginTitle":"Simple AI 平台", ...}
   ↓
用户访问某客户端登录页 → Basic Auth(clientId:clientSecret)
   ↓
□ Spring Security 拦截 → MyClientDetailsService.checkClientDetails()
   ↓
[ClientDetails] (含 reserve 字段)
   ↓
□ AbsLoginManager.doLogin(ClientDetails, adapter)
   ↓
□ PwdLoginManager / WxLoginManager
   ├── 将 clientDetails.getReserve() 放入 extensionResponse["clientConfig"]
   └── 连同 avatarUrl、nickname 等一并返回
   ↓
[登录响应 JSON] = { token, refresh, ..., clientConfig: { loginTitle, loginSubtitle, ... } }
   ↓
□ 前端 LoginPageComponent
   ├── 从登录响应读取 clientConfig
   ├── 有配置 → 动态渲染标题/副标题/页脚
   └── 无配置 → 降级显示默认文案 "OAuth 管理系统"
```

### 3.2 用户体系创建流程

```
用户注册请求（含 AI 配额等业务字段）
   ↓
□ AiUserController.create(request)
   ↓
□ DefaultAiUserService.create(request)
   ├── 提取 nickname、username、phone
   ├── 调用 UserService.create(nickname, username, phone, avatarUrl)
   │   ↓
   │   [OAuth /api/user] → BCrypt 加密默认密码 → 返回 userId
   │   ↓
   ├── 获得 OAuth userId
   ├── 以 userId 为 PK 创建 ai_user 记录（配额、偏好等）
   └── 返回 userId
   ↓
□ 返回创建结果
```

---

## 四、自检结论

### 4.1 已验证的现有代码

| 模块 | 文件 | 现状 | 验证结果 |
|------|------|------|----------|
| ClientDetails | [`ClientDetails.java`](C:\start\simple-common\simple-common-auth\simple-common-auth-server\src\main\java\com\simple\common\auth\server\common\entity\ClientDetails.java:20-45) | 无 reserve 字段 | 需新增 |
| MyClientDetailsService | [`MyClientDetailsService.java`](C:\start\simple-common-oauth\simple-common-oauth-server\src\main\java\com\simple\oauth\manager\client\MyClientDetailsService.java:20-33) | 构建 ClientDetails 时未传 reserve | 需修改 |
| PwdLoginManager | [`PwdLoginManager.java`](C:\start\simple-common-oauth\simple-common-oauth-server\src\main\java\com\simple\oauth\manager\login\PwdLoginManager.java:79-83) | extensionResponse 含 avatarUrl/nickname | 需追加 clientConfig |
| WxLoginManager | [`WxLoginManager.java`](C:\start\simple-common-oauth\simple-common-oauth-server\src\main\java\com\simple\oauth\manager\login\WxLoginManager.java:121-124) | extensionResponse 含 avatarUrl/nickname | 需追加 clientConfig |
| LoginPageComponent | [`LoginPageComponent.tsx`](C:\start\simple-common-oauth\simple-common-oauth-frontend\src\components\LoginPageComponent.tsx:132-137) | 标题/副标题硬编码 | 需改为动态渲染 |
| simple-ai pom.xml | [`pom.xml`](C:\start\simple-ai\pom.xml:44) | 已依赖 simple-common-oauth-start | 无需额外依赖 |
| simple-ai SQL | [`public.sql`](C:\start\simple-ai\doc\sql\public.sql) | 无 ai_user 表 | 需新增 |
| simple-ai auth 配置 | [`application-local.yaml`](C:\start\simple-ai\src\main\resources\application-local.yaml:76-78) | 缺少 server-url | 需新增 |
| OAuth 端口 | [`application-local.yml`](C:\start\simple-common-oauth\simple-common-oauth-server\src\main\resources\application-local.yml:2) | 8000 | 用于配置 server-url |

### 4.2 需要新增

- `ClientDetails.reserve` 字段
- `ai_user` 表（DDL）
- `AiUser` 实体类
- 5 个 DTO（Create/Update/Page Request/Page Response/Info Response）
- `AiUserView` 接口 + `MPAiUserView` 实现
- `AiUserRepository` 接口
- `AiUserDao.xml` Mapper XML
- `AiUserCopyMapper`
- `AiUserService` 接口 + `DefaultAiUserService` 实现
- `AiUserController`
- `application-local.yaml` 中 `server-url` 配置

### 4.3 需要修改

- `ClientDetails.java`：新增 reserve 字段
- `MyClientDetailsService.java`：传入 reserve
- `PwdLoginManager.java`：extensionResponse 加入 clientConfig
- `WxLoginManager.java`：extensionResponse 加入 clientConfig
- `LoginPageComponent.tsx`：动态渲染文案
- `application-local.yaml`：添加 auth.server-url

### 4.4 无需变动

- `SysProjectClient` 实体（reserve 字段已存在）
- `simple-ai pom.xml`（已依赖 oauth-start）
- OAuth 登录流程框架（AbsLoginManager、LoginService）

---

## 五、规范加载

- `java代码编辑规范.md`：重点关注 §2（框架优先）、§10（命名规则）、§11（MyBatis XML）、§12（DTO 设计）、§9（禁止全限定类名）
- `web代码编辑规范.md`：重点关注 §2（认证与请求）、§4（表格规范）、§5（弹窗规范）、§6（按钮防重复）、§9（UI 一致性）

---

## 六、执行状态清单

### Part A: 文案定制（simple-common-oauth / oauth-server / oauth-frontend）

- [x] A1: 修改 [`ClientDetails.java`] — 新增 `private Map<String, Object> reserve;` 字段
- [x] A2: 修改 [`MyClientDetailsService.java`] — 构建 ClientDetails 时 `.setReserve(entity.getReserve())`
- [x] A3: 修改 [`PwdLoginManager.java`] — extensionResponse 追加 `clientConfig`（来自 `clientDetails.getReserve()`）
- [x] A4: 修改 [`WxLoginManager.java`] — extensionResponse 追加 `clientConfig`（来自 `clientDetails.getReserve()`）
- [x] A5: 修改 [`LoginPageComponent.tsx`] — 从登录响应读取 `clientConfig`，动态渲染标题/副标题/页脚，无配置时降级默认文案
- [x] A6: 在 oauth-server 执行 `mvn clean compile` 验证编译通过

### Part B: 用户体系（simple-ai）

- [x] B1: 修改 [`public.sql`] — 新增 `ai_user` 表 DDL
- [x] B2: 创建 `AiUser` 实体类 [`AiUser.java`]
- [x] B3: 创建 DTO 目录及文件：
  - `CreateAiUserRequest.java`
  - `UpdateAiUserRequest.java`
  - `PageAiUserRequest.java`
  - `PageAiUserResponse.java`
  - `InfoAiUserResponse.java`
- [x] B4: 创建 `AiUserView` 接口
- [x] B5: 创建 `MPAiUserView` 实现类
- [x] B6: 创建 `AiUserRepository` 接口
- [x] B7: 创建 `AiUserDao.xml` Mapper XML
- [x] B8: 创建 `AiUserCopyMapper`
- [x] B9: 创建 `AiUserService` 接口
- [x] B10: 创建 `DefaultAiUserService` 实现（核心：create 时先调 UserService.create → 获得 userId → 再创建 ai_user）
- [x] B11: 创建 `AiUserController`
- [x] B12: 修改 [`application-local.yaml`] — 在 `simple.auth` 下添加 `server-url: http://localhost:8000`
- [x] B13: 在 simple-ai 执行 `mvn clean compile` 验证编译通过

---

## 七、重点业务流程说明

### 7.1 文案定制 — extensionResponse 数据流

1. `MyClientDetailsService` 从 `sys_project_client.reserve` 读取 JSON 扩展配置
2. 放入 `ClientDetails.reserve`
3. `PwdLoginManager.doLogin()` / `WxLoginManager.doLogin()` 从 `clientDetails.getReserve()` 获取配置
4. 将 reserve Map 序列化后以 `"clientConfig"` 为 key 放入 `extensionResponse`
5. `LoginService` 将 extensionResponse 合并到最终 token 响应
6. 前端从 `response.clientConfig` 读取后动态渲染

### 7.2 用户体系 — create 事务流程

1. Controller 接收 `CreateAiUserRequest`（包含 AI 业务字段）
2. Service 提取 nickname/username/phone/avatarUrl
3. 调用 `UserService.create()` → OAuth `/api/user` → 返回 `userId`
4. 以 `userId` 为 PK 构建 `AiUser` 实体（AI 配额、偏好等）
5. 调用 `aiUserView.save(entity)` 写入本地库
6. `@Transactional` 保证两步原子性（若 OAuth 调用失败则回滚本地）

### 7.3 用户体系 — delete 事务流程

1. Controller 接收 userId
2. Service 先调用 `aiUserView.deleteById(userId)` 删除本地记录
3. 再调用 `UserService.delete(Arrays.asList(userId))` 删除 OAuth 用户
4. `@Transactional` 保证原子性

---

## 八、设计对齐缺口清单

| 状态 | 设计要点 | 当前状态 | 后续处理 |
|------|---------|---------|---------|
| [x] | sys_project_client.reserve 存储内容约定（key 命名规范） | 约定：`loginTitle`、`loginSubtitle`、`footerTip`、`logoUrl`，均为可选 | 前端已实现降级默认值 |
| [x] | extensionResponse 当前类型为 `Map<String, String>`，reserve 为 `Map<String, Object>` | 已用 JsonUtils.toJsonStr 序列化，不修改类型签名 | 保持当前实现 |
| [x] | ai_user 表除 quota/preferences 外还需哪些字段 | 已创建基本字段 | 后续根据业务扩展 |
| [x] | 前端 AiUser 管理页面是否需要 | 未在本次范围 | 后续 Web 开发任务单独处理 |

---

## 九、重要文件索引

| 文件 | 路径 | 改动类型 | 说明 |
|------|------|---------|------|
| ClientDetails | `C:\start\simple-common\simple-common-auth\simple-common-auth-server\src\main\java\com\simple\common\auth\server\common\entity\ClientDetails.java` | 修改 | 新增 reserve 字段 |
| MyClientDetailsService | `C:\start\simple-common-oauth\simple-common-oauth-server\src\main\java\com\simple\oauth\manager\client\MyClientDetailsService.java` | 修改 | 传入 reserve |
| PwdLoginManager | `C:\start\simple-common-oauth\simple-common-oauth-server\src\main\java\com\simple\oauth\manager\login\PwdLoginManager.java` | 修改 | extensionResponse 加 clientConfig |
| WxLoginManager | `C:\start\simple-common-oauth\simple-common-oauth-server\src\main\java\com\simple\oauth\manager\login\WxLoginManager.java` | 修改 | extensionResponse 加 clientConfig |
| LoginPageComponent | `C:\start\simple-common-oauth\simple-common-oauth-frontend\src\components\LoginPageComponent.tsx` | 修改 | 动态渲染文案 |
| public.sql | `doc/sql/public.sql` | 修改 | 新增 ai_user 表 |
| AiUser | `src/main/java/com/simple/ai/common/entity/aiUser/AiUser.java` | 新增 | 实体类 |
| CreateAiUserRequest | `src/main/java/com/simple/ai/common/dto/aiUser/CreateAiUserRequest.java` | 新增 | 创建 DTO |
| AiUserView | `src/main/java/com/simple/ai/common/view/aiUser/AiUserView.java` | 新增 | View 接口 |
| MPAiUserView | `src/main/java/com/simple/ai/view/aiUser/MPAiUserView.java` | 新增 | View 实现 |
| AiUserRepository | `src/main/java/com/simple/ai/view/aiUser/AiUserRepository.java` | 新增 | Repository |
| AiUserDao.xml | `src/main/resources/mapper/AiUserDao.xml` | 新增 | Mapper XML |
| AiUserCopyMapper | `src/main/java/com/simple/ai/common/copy/aiUser/AiUserCopyMapper.java` | 新增 | CopyMapper |
| AiUserService | `src/main/java/com/simple/ai/common/service/aiUser/AiUserService.java` | 新增 | Service 接口 |
| DefaultAiUserService | `src/main/java/com/simple/ai/service/aiUser/DefaultAiUserService.java` | 新增 | Service 实现 |
| AiUserController | `src/main/java/com/simple/ai/controller/aiUser/AiUserController.java` | 新增 | Controller |
| application-local.yaml | `src/main/resources/application-local.yaml` | 修改 | 新增 server-url |

---

## 十、编译验证记录

| 模块 | 命令 | 结果 | 时间 |
|------|------|------|------|
| simple-common-auth-server | `mvn clean install -pl simple-common-auth/simple-common-auth-server -am -DskipTests` | BUILD SUCCESS | 2026-07-18 |
| oauth-server | `mvn clean compile -DskipTests` | BUILD SUCCESS | 2026-07-18 |
| oauth-frontend | `npm run build` | ✓ built in 3.73s | 2026-07-18 |
| simple-ai | `mvn clean compile -DskipTests` | BUILD SUCCESS | 2026-07-18 |

---

## 十一、深度自检记录

| 维度 | 结果 | 说明 |
|------|------|------|
| 一、性能风险 | ✅ | 分页用 IPage，无 N+1 查询 |
| 二、线程安全 | ✅ | Service 无共享可变状态 |
| 三、内存风险 | ✅ | 分页限制数据量，无静态集合/缓存 |
| 四、代码规范 | ✅ | 无全限定类名，无违规链式调用，主方法纯编排 |
| 五、SQL 规范 | ✅ | 无 SELECT *，使用 `<where>`，无 `${}` |
| 六、注释规范 | ✅ | Javadoc 完备，方法内注释换行标注 |
| 七、业务流程与逻辑 | ✅ | 验重委托 OAuth，存在性校验，事务保护 |
| 八、数据流转 | ✅ | createTime 自动填充，userId 来自 OAuth |
| 九、代码一致性 | ✅ | create/update 路径对称 |
| 十、无效操作 | ✅ | 无未使用查询结果 |
| 十一、孤儿数据 | ✅ | @Transactional 保证原子性 |
| 十二、接口排序 | ✅ | 不适用（无 sort 字段） |
| 十三、数据流冗余 | ✅ | 无双向转换/冗余中间格式 |

**总计：13/13 通过，0 违规**
