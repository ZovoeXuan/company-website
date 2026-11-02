# 公司官网（Java + Vue + MySQL）

内容由后台维护的企业官网：官网 `web`、管理后台 `admin`、服务端 `server` 三个工程，共用一套 `/api` 接口。

需求、字段、接口与验收标准以 `../公司官网后台及前后端联动开发文档.md` 为准；本文件只说明怎么把它跑起来、当前做到哪一步。

## 1. 目录

```text
website/
  server/                  Spring Boot 服务端
    src/main/java/com/company/cms/
      common/              统一响应、错误码、异常处理、requestId
      config/              安全基线
    src/main/resources/
      db/migration/        Flyway 迁移（V1 业务表，V2 会话表）
      application.yml
  web/                     官网 Vue 3
  admin/                   管理后台 Vue 3 + Element Plus
db/00-init-env.sql         本地建库与账号脚本（一次性，用 root 执行）
```

## 2. 版本基线（本机实测）

| 组件 | 版本 | 说明 |
|---|---|---|
| JDK | 21.0.12 LTS (Temurin) | 与文档一致 |
| Spring Boot | 4.1.2 | 文档要求 4.1.x，取当前稳定版 |
| Spring Session JDBC | 4.1.1 | 由 Boot BOM 管理 |
| Jackson | 3.1.7（`tools.jackson`） | Boot 4 已从 Jackson 2 迁移，**包名不同** |
| MySQL | 8.0.46 | 文档基线为 8.4 LTS，本地按 8.0.46 验证，差异见第 6 节 |
| Node / npm | 22.22.2 / 10.9.7 | 满足 Vite 8 要求 |
| Vue / Vite / TS | 3.5.44 / 8.3.5 / 6.0.3 | 由 create-vite 生成 |
| Element Plus | 2.14.7 | 仅后台使用 |

Maven 使用 Wrapper（`./mvnw`），不依赖本机安装的 Maven。

## 3. 启动步骤

### 3.1 初始化数据库（一次）

用 root 执行 `../db/00-init-env.sql`，先把两处 `__CHANGE_ME__` 换成自己的密码：

```bash
mysql -u root -p < ../db/00-init-env.sql
```

脚本只建 `cms_dev`、`cms_test` 两个库和两个账号，不建业务表、不建默认账号。

### 3.2 配置环境变量

服务端从环境变量读取，不把密码写进仓库：

| 变量 | 默认值 | 用途 |
|---|---|---|
| `CMS_DB_URL` | `jdbc:mysql://127.0.0.1:3306/cms_dev?...` | 业务连接 |
| `CMS_DB_USER` | `cms_app` | 只读写，不能改结构 |
| `CMS_DB_PASSWORD` | 空 | 应用账号密码 |
| `CMS_MIGRATION_USER` | `cms_migrator` | Flyway 专用账号 |
| `CMS_MIGRATION_PASSWORD` | 空 | 迁移账号密码 |

本机开发已创建 `server/src/main/resources/application-local.yml`（已被 .gitignore 排除），
用 `--spring.profiles.active=local` 激活即可，不需要每次导出环境变量。
新机器请复制 `application-local.yml.example` 后填写。

生产环境另需：手机号加密密钥、摘要密钥、限流参数、素材目录（阶段 6 接入）。

> 端口提示：若环境中存在 `SERVER__PORT` 变量（宽松绑定会覆盖 `server.port`），启动时用
> `--server.port=18080` 显式指定；命令行参数优先级高于环境变量。

### 3.3 启动

```bash
# 服务端（首次启动自动执行 Flyway 迁移）
cd website/server && ./mvnw spring-boot:run

# 官网
cd website/web && npm install && npm run dev      # http://localhost:5173

# 后台
cd website/admin && npm install && npm run dev    # http://localhost:5174/admin/
```

Vite 已把 `/api` 代理到 `127.0.0.1:8080`，开发期不需要处理跨域。

### 3.4 构建产物

```bash
cd website/server && ./mvnw -DskipTests package   # target/*.jar
cd website/web    && npm run build                 # dist/
cd website/admin  && npm run build                 # dist/，base 为 /admin/
```

## 4. 当前进度

| 阶段 | 内容 | 状态 |
|---|---|---|
| 1 | 三工程、迁移基线、统一响应、健康检查 | 已完成，真实 MySQL 8.0.46 验证通过（见 `docs/stage1-verification.md`） |
| 2 | 登录、CSRF、角色、账号、审计 | 已完成，HTTP 与浏览器端到端验证通过（见 `docs/stage2-verification.md`） |
| 3 | 素材、分类、产品纵向闭环 | 后端未开始；官网产品页已完成（mock 数据） |
| 4 | 方案、案例、新闻、客户标识 | 后端未开始；官网对应页面已完成（mock 数据） |
| 5 | 站点配置、导航、首页、单页 | 后端未开始；官网首页、关于、联系、隐私已完成（mock 数据） |
| 6 | 咨询闭环 | 未开始（联系页表单仅前端校验） |
| 7 | 联调、测试、部署、恢复 | 未开始 |

官网九个页面已完成，说明见 `docs/frontend-pages.md`。页面数据来自 `src/api/public.ts` 的
`USE_MOCK` 开关：当前为 mock，公开接口就绪后改为 `false` 即可，页面无需改动。

后台页面在登录未接入前可直接访问，顶部有醒目提示，**不得**当作可交付状态。

## 5. 已实现的接口约定

- 成功：`{"code":"OK","message":"success","data":...,"requestId":"req-..."}`
- 失败：同时使用正确 HTTP 状态码，并带 `code`、`message`、可选 `fieldErrors`、`requestId`
- 每个响应带 `X-Request-Id` 头；异常日志按该 ID 可追踪
- `/api/public/**` 允许匿名；`/api/admin/**` 除 `auth/csrf`、`auth/login` 外需要登录
- 未登录返回 401 JSON，CSRF 失败返回 403 `CSRF_INVALID`，不返回 HTML 登录页

### 认证流程（阶段 2）

1. `GET /api/admin/auth/csrf` 取原始令牌（头名 `X-CSRF-TOKEN`）。
2. `POST /api/admin/auth/login`，表单 `username/password` + 请求头带令牌。
3. 登录成功后**必须重新获取令牌**：会话固定保护会轮换 CSRF 令牌，沿用旧令牌的写请求会被拒绝。
4. 之后所有写请求带 `X-CSRF-TOKEN`；`GET /api/admin/auth/me` 取当前用户。
5. 连续登录失败达到阈值返回 429，带 `Retry-After`；锁定期内不再比对密码。

### 初始管理员

账号表为空且提供以下环境变量时，启动会创建一次管理员，无固定默认密码：

```bash
CMS_INIT_USERNAME=admin CMS_INIT_PASSWORD='至少12位的密码' ./mvnw spring-boot:run
```

创建成功后应从运行环境移除这两个变量。`sys_user` 非空时初始化逻辑直接跳过。

## 6. 已知偏差与决定

1. **Jackson 3**：Spring Boot 4 使用 `tools.jackson.databind.ObjectMapper`，`com.fasterxml.jackson.databind` 已不在类路径。迁移代码时不要照抄 Boot 3 示例。
2. **TypeScript 6**：`baseUrl` 已废弃，路径别名改用 `paths` 相对 `tsconfig` 的写法。
3. **MySQL 8.0.46 代替 8.4 LTS**：`schema.sql` 用到的 CHECK 约束、生成列、`utf8mb4_0900_ai_ci` 在 8.0.46 均可用；交付说明需记录该偏差。
4. **产品详情按 P0 做图文正文**：参考站的模块化区块（硬件卡片、角色价值拆分）列入二期，不在首版范围。
5. **咨询表单内嵌在产品/方案/案例详情页**，与参考站「按钮跳转联系页」不同，属于本项目改进。
6. **后台首版全量引入 Element Plus**，构建产物约 1 MB；上线前可改为按需引入。

## 7. 安全注意

- `db/00-init-env.sql` 中是占位密码，执行后不要把真实密码写回文件或提交。
- 不存在默认管理员账号；初始管理员通过部署时一次性初始化创建（阶段 2 实现）。
- 公开接口不返回草稿、版本号、后台备注或线索信息。
