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
      db/migration/        Flyway 迁移（V1 业务表，V2 会话表，V3 媒体视频字段）
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
| MySQL | 8.4.13 LTS | 与文档基线一致；InnoDB、`utf8mb4_0900_ai_ci` |
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
| `CMS_LEAD_ENCRYPTION_KEY` | 空 | 咨询手机号 AES-256-GCM 密钥，Base64 32 字节 |
| `CMS_LEAD_HASH_KEY` | 空 | 幂等载荷与限流摘要 HMAC 密钥，Base64 32 字节 |

本机开发已创建 `server/src/main/resources/application-local.yml`（已被 .gitignore 排除），
用 `--spring.profiles.active=local` 激活即可，不需要每次导出环境变量。
新机器请复制 `application-local.yml.example` 后填写。

生产环境另需：素材目录、限流参数，以及上面两个咨询密钥。密钥可用
`openssl rand -base64 32` 生成；密钥丢失将无法解密历史手机号。

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
| 1 | 三工程、迁移基线、统一响应、健康检查 | 已完成，真实 MySQL 8.4.13 验证通过（见 `docs/stage1-verification.md`） |
| 2 | 登录、CSRF、角色、账号、审计 | 已完成，HTTP 与浏览器端到端验证通过（见 `docs/stage2-verification.md`） |
| 3 | 素材、分类、产品纵向闭环 | 接口、发布链路与后台页面均完成（`docs/stage3-verification.md`） |
| 4 | 方案、案例、新闻、客户标识 | 完成：结构化字段、后台编辑与官网页面接入（`docs/stage4-verification.md`） |
| 5 | 站点配置、导航、首页、单页 | 完成：配置与单页见 `docs/stage5a-verification.md`，首页编排与导航见 `docs/stage5b-verification.md` |
| 6 | 咨询闭环 | 完成：官网表单、幂等与限流、后台线索处理（`docs/stage6-verification.md`） |
| 7 | 联调、测试、部署、恢复 | 未开始 |

官网九个页面已完成，说明见 `docs/frontend-pages.md`；视觉与媒体增强见 `docs/ui-redesign.md`。
页面数据全部来自 `/api/public/*`，示例数据层已删除；列表与详情区分加载、空态与请求失败三种状态。

### 素材存储

上传的图片与视频默认落在 `server/data/media/{年}/{月}/`，可用 `CMS_MEDIA_ROOT` 覆盖。
该目录已在 `.gitignore` 中排除。

| 类型 | 上限 | 识别方式 | 用途 |
|---|---|---|---|
| 图片 | 5MB | 文件头判断 PNG / JPEG / WebP | 封面、正文图、Logo、首屏背景 |
| 视频 | 50MB | 文件头判断 MP4（`ftyp`）/ WebM | 首页首屏、内容主图 |

- 扩展名与 MIME 只作为初筛，最终以文件头为准；视频不做像素校验。
- 公开访问地址为 `/api/public/media/{id}`，只有被**已发布**内容或已发布配置引用时才可读取，草稿引用返回 404。
- 视频接口支持 `Range`，返回 `206` 与 `Content-Range`，单段最多 8MB，其余流式输出，进度条可拖动。
- 后台素材列表可按「全部 / 图片 / 视频」筛选；首页首屏的桌面端与手机端可分别配置图片或视频。

后台需要登录后访问，分为 `ADMIN` 与 `EDITOR` 两个角色：编辑只能保存草稿，发布与下架仅管理员可操作。

## 5. 已实现的接口约定

- 成功：`{"code":"OK","message":"success","data":...,"requestId":"req-..."}`
- 失败：同时使用正确 HTTP 状态码，并带 `code`、`message`、可选 `fieldErrors`、`requestId`
- 每个响应带 `X-Request-Id` 头；异常日志按该 ID 可追踪
- `/api/public/**` 允许匿名；`/api/admin/**` 除 `auth/csrf`、`auth/login` 外需要登录
- `POST /api/public/leads` 是唯一排除 CSRF 的公开写接口，必须携带 UUID 格式的 `Idempotency-Key`
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
3. **MySQL 已升级到 8.4.13 LTS**：升级后复核了迁移校验、CHECK 约束、生成列、`cms_*` 账号认证插件与四字节字符往返，均正常；原先「8.0.46 代替 8.4」的偏差已消除。注意 8.4 默认禁用 `mysql_native_password`，新建账号必须使用 `caching_sha2_password`。
4. **产品详情按 P0 做图文正文**：参考站的模块化区块（硬件卡片、角色价值拆分）列入二期，不在首版范围。
5. **咨询表单在联系页内嵌，首页与详情页通过同一咨询弹窗打开**，与参考站「按钮跳转联系页」不同，属于本项目改进。
6. **后台首版全量引入 Element Plus**，构建产物约 1 MB；上线前可改为按需引入。

## 7. 安全注意

- `db/00-init-env.sql` 中是占位密码，执行后不要把真实密码写回文件或提交。
- 不存在默认管理员账号；初始管理员通过部署时一次性初始化创建（阶段 2 实现）。
- 咨询密钥只从环境变量加载，禁止写入源码、数据库或日志；生产密钥需单独受控备份。
- 咨询手机号加密存储，列表仅返回末四位；查看完整号码与状态处理都写审计。
- 限流为进程内滑动窗口，仅适用于单实例；多实例部署时需改为共享存储。
- 公开接口不返回草稿、版本号、后台备注或线索信息。
