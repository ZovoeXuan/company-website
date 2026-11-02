# 阶段 1 验证记录

日期：2026-11-02
范围：三工程骨架、Flyway 迁移基线、统一响应与异常、健康检查
结论：**通过**（在真实 MySQL 实例上执行，非内存库；初始验证在 8.0.46，升级到 8.4.13 后已复验，见第 9 节）

## 1. 验证环境

| 项 | 实际值 |
|---|---|
| OS | Windows 11（win32） |
| JDK | Temurin 21.0.12.1 LTS |
| Spring Boot | 4.1.2（内置 Tomcat 11.0.27、Spring Framework 7.0.10） |
| Spring Session | 4.1.1 |
| Jackson | 3.1.7（`tools.jackson`） |
| MySQL | 8.4.13 LTS，默认端口 3306，InnoDB，`utf8mb4_0900_ai_ci` |
| Node / npm | 22.22.2 / 10.9.7 |
| Vue / Vite / TypeScript | 3.5.44 / 8.3.5 / 6.0.3 |

数据库：`cms_dev`，字符集 `utf8mb4`，排序规则 `utf8mb4_0900_ai_ci`。
账号：`cms_migrator`（建表）与 `cms_app`（只读写）分离，均非 root。

## 2. 迁移结果

```
Migrating schema `cms_dev` to version "1 - baseline"
Migrating schema `cms_dev` to version "2 - spring session"
Successfully applied 2 migrations to schema `cms_dev`, now at version v2 (execution time 00:00.344s)
```

迁移后对象清单（12 张表）：

```
cms_category, cms_config, cms_content, cms_content_ref, cms_lead,
cms_media, cms_media_ref, sys_audit_log, sys_user,
spring_session, spring_session_attributes, flyway_schema_history
```

配置初始化（6 条，全部未发布、空草稿）：

| config_key | publication_status | draft_version | 草稿字段数 |
|---|---|---|---|
| ABOUT | UNPUBLISHED | 1 | 0 |
| CONTACT | UNPUBLISHED | 1 | 0 |
| HOME | UNPUBLISHED | 1 | 0 |
| NAVIGATION | UNPUBLISHED | 1 | 0 |
| PRIVACY | UNPUBLISHED | 1 | 0 |
| SITE | UNPUBLISHED | 1 | 0 |

## 3. 接口验证

### 3.1 健康检查

```
GET /actuator/health  ->  200
{"groups":["liveness","readiness"],"status":"UP"}
```

### 3.2 未登录访问后台接口

```
GET /api/admin/auth/me  ->  401
X-Request-Id: req-bdf522a229ed

{"code":"UNAUTHENTICATED","message":"登录状态已失效，请重新登录","requestId":"req-bdf522a229ed"}
```

验证点：返回 JSON 而不是 HTML 登录页；`requestId` 与响应头一致；无堆栈信息。

### 3.3 未实现的公开接口

```
GET /api/public/site-config  ->  404
{"code":"NOT_FOUND","message":"请求的资源不存在","requestId":"req-270f1471a882"}
```

验证点：统一异常处理生效，404 也使用统一响应体。

## 4. 数据库约束验证

| 验证项 | 操作 | 结果 |
|---|---|---|
| 权限分离 | 用 `cms_app` 执行 `CREATE TABLE` | `ERROR 1142: CREATE command denied`，符合预期 |
| 角色白名单 | 插入 `role_code='SUPER'` | `ERROR 3819: Check constraint 'ck_user_role' is violated` |
| 快照一致性 | 把 SITE 直接改为 PUBLISHED 但不写快照 | `ERROR 3819: Check constraint 'ck_config_snapshot_state' is violated` |
| 生成列 | 查询 `active_published_name` | `STORED GENERATED`，应用不可赋值 |

这三条证明：数据库层能挡住越权改结构和绕过发布状态机的写入，应用层校验不是唯一防线。

## 5. 前端构建

| 工程 | 结果 | 产物 |
|---|---|---|
| web | 通过 | `dist/index.html` 0.49 kB、CSS 7.01 kB、JS 97.07 kB |
| admin | 通过 | `dist/`，JS 1.02 MB（全量引入 Element Plus，待按需优化） |

## 6. 遇到的问题与处理

1. **Jackson 3 包名变更**：Spring Boot 4 使用 `tools.jackson.databind.ObjectMapper`，`com.fasterxml.jackson.databind` 已不在类路径。首次编译失败，已改。
2. **`spring.jackson.serialization.write-dates-as-timestamps` 已移除**：Jackson 3 默认输出 ISO 8601，该配置项不存在，会导致启动失败，已删除。
3. **Spring Session JDBC 启动即连库**：`JdbcHttpSessionConfiguration` 创建 JdbcTemplate 时要读数据库元信息，没有数据库连接应用无法启动（即使关闭 Flyway）。这不是缺陷，但意味着「空库启动」必须真有一个可连的库。
4. **环境变量 `SERVER__PORT`**：本机运行环境注入了该变量（值 64586）且端口被占用，宽松绑定会覆盖 `application.yml` 的 8080。验证时用 `--server.port=18080` 覆盖。命令行参数优先级高于环境变量。
5. **独立 MySQL 实例启动失败**：曾尝试在 3307 用独立 datadir 起第二个实例，mysqld 静默退出（无日志、退出码 0/1 不稳定），`--no-defaults`、`--skip-networking`、关沙箱均无效。已放弃并删除临时目录，改用现有 3306 实例。

## 7. 未验证项（下一阶段）

- 登录、CSRF、会话持久化、账号与审计（阶段 2）
- 草稿/发布/下线状态机与乐观锁（阶段 3）
- 图片上传与引用校验（阶段 3）
- 咨询幂等、限流与手机号加密（阶段 6）
- 移动端与错误态（阶段 7）

## 8. 复现命令

```bash
# 迁移（首次启动自动执行）
cd website/server
./mvnw spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local --server.port=18080"

# 接口
curl -i http://127.0.0.1:18080/actuator/health
curl -i http://127.0.0.1:18080/api/admin/auth/me

# 数据库对象
mysql -h 127.0.0.1 -P 3306 -u root -p cms_dev -e "SHOW TABLES;"
```

## 9. 升级到 MySQL 8.4.13 LTS 后的复验

2026-11-02 将本机 MySQL 从 8.0.46 升级到 8.4.13 LTS（原地升级，端口仍为 3306）。升级后复核结果：

| 检查项 | 结果 |
|---|---|
| 版本 | `8.4.13`，InnoDB `8.4.13`，`utf8mb4` / `utf8mb4_0900_ai_ci` |
| 数据留存 | 12 张表齐全；`cms_content` 2 条、`cms_media` 2 条、`sys_user` 2 个 |
| Flyway | `Successfully validated 2 migrations`，`Schema cms_dev is up to date`，无新增迁移 |
| 应用启动 | `Started CmsApplication`，`Database: ... (MySQL 8.4)` |
| 接口 | `/actuator/health` UP；公开产品列表、登录、内容列表均正常 |
| CHECK 约束 | 非法角色仍被 `ck_user_role` 拒绝；无快照改成已发布仍被 `ck_config_snapshot_state` 拒绝 |
| 生成列 | `active_published_name` 仍为 `STORED GENERATED` |
| 账号插件 | `cms_app`、`cms_migrator` 均为 `caching_sha2_password`（8.4 默认，可用） |
| 四字节字符 | 通过接口创建分类「测试🚀四字节」并回读一致，emoji 往返正常 |

注意事项：

- 8.4 默认禁用 `mysql_native_password`，新建账号不要指定该插件，否则无法登录。
- 升级不可回退，生产环境升级前必须做全量备份与恢复演练。
- 本次复验的约束与字符集检查走的是应用链路（JDBC + JSON），控制台直接执行含 emoji 的 SQL 会因终端编码显示乱码，不代表数据库问题。
