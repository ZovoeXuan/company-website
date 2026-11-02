# 阶段 2 验证记录

日期：2026-11-02
范围：登录、CSRF、角色权限、会话撤销、账号管理、审计日志、工作台
结论：**通过**（真实 MySQL 8.0.46，HTTP 层与浏览器端到端均已验证）

## 1. 实现的接口

| 方法 | 路径 | 权限 | 说明 |
|---|---|---|---|
| GET | `/api/admin/auth/csrf` | 匿名 | 返回原始令牌，头名 `X-CSRF-TOKEN` |
| POST | `/api/admin/auth/login` | 匿名 + CSRF | 表单编码，成功返回当前用户 |
| POST | `/api/admin/auth/logout` | 登录 | 撤销当前会话并审计 |
| GET | `/api/admin/auth/me` | 登录 | id、username、displayName、role |
| PUT | `/api/admin/auth/password` | 登录 | 改密后撤销该账号全部会话 |
| GET | `/api/admin/dashboard` | 登录 | 按角色裁剪字段 |
| GET | `/api/admin/users` | ADMIN | 分页、关键字 |
| POST | `/api/admin/users` | ADMIN | 新建账号，返回 201 |
| PUT | `/api/admin/users/{id}` | ADMIN | 期望版本号，最后管理员保护 |
| POST | `/api/admin/users/{id}/reset-password` | ADMIN | 重置后撤销会话 |
| GET | `/api/admin/audit-logs` | ADMIN | 只读，按操作人/动作/日期筛选 |

## 2. HTTP 层验证

| 编号 | 场景 | 预期 | 实际 |
|---|---|---|---|
| B01 | 匿名获取 CSRF | 200 + token | `{"token":"73c7...","headerName":"X-CSRF-TOKEN"}` |
| B02 | 登录不带 CSRF | 403 CSRF_INVALID | 403，统一 JSON |
| B03 | 错误密码 | 401，统一提示 | 401「账号或密码错误」 |
| B04 | 正确登录 | 200 + 用户信息 | 200，`{"id":"1","role":"ADMIN"}` |
| B05 | 登录后访问 me | 200 | 200 |
| B06 | EDITOR 访问账号列表 | 403 | 403 FORBIDDEN |
| B07 | EDITOR 访问审计日志 | 403 | 403 FORBIDDEN |
| B08 | EDITOR 新建账号 | 403 | 403 FORBIDDEN |
| B09 | EDITOR 访问工作台 | 200 且不含线索/账号字段 | 200，仅 `publishedContents/draftContents/contentsByType` |
| B10 | 禁用唯一管理员 | 409 | 409 LAST_ADMIN_PROTECTED |
| B11 | 修改本人密码 | 200，旧会话失效 | 200；随后 me 返回 401 |
| B12 | 新密码登录 | 200 | 200 |
| B13 | 连续失败登录 | 第 5 次起 429 | 第 5 次 429，`Retry-After: 600` |
| B14 | 退出登录 | 200，会话失效 | 200；随后 me 返回 401 |
| B15 | 审计日志内容 | 含操作人、动作、结果 | 见下 |

审计日志实际输出（节选）：

```
LOGOUT          SUCCESS  编辑一号    账号 editor01 退出登录
LOGIN_FAILURE   FAILURE  —          账号 nobody01 登录失败：失败次数过多，已临时锁定
LOGIN_FAILURE   FAILURE  —          账号 nobody01 登录失败：账号或密码错误
LOGIN_SUCCESS   SUCCESS  编辑一号    账号 editor01 登录成功
PASSWORD_CHANGE SUCCESS  编辑一号    修改本人密码，全部会话已撤销
USER_CREATE     SUCCESS  系统管理员  创建账号 editor01（EDITOR）
INIT_ADMIN      SUCCESS  —          创建初始管理员 admin
```

会话表核对：撤销后 `spring_session` 仅剩 1 行（管理员会话），证明撤销落到了框架表。

## 3. 浏览器端到端

环境：Chrome 156 无头模式，Vite dev server（5174，`/api` 代理到 8080）。

| 步骤 | 结果 |
|---|---|
| 打开 `/admin/login` | 页面渲染正常，品牌、输入框、按钮、提示齐全 |
| 填入 admin 账号密码并提交 | 跳转 `/admin/dashboard` |
| 工作台内容 | 顶部显示「系统管理员 / ADMIN / 修改密码 / 退出登录」；统计为已发布 0、草稿 0、待处理咨询 0、后台账号 2；内容分布为空态 |
| 左侧菜单 | ADMIN 可见「咨询管理」与「系统管理」子菜单 |

未自动验证：账号管理页在浏览器内的表格渲染。该页构建通过、接口已用 HTTP 验证，但脚本的跨页导航未生效，留待阶段 3 联调时人工确认。
脚本：`docs/e2e-login.mjs`（凭据通过环境变量传入，不写入仓库）。

## 4. 过程中修复的缺陷

1. **循环依赖**：`PasswordEncoder` 定义在 `SecurityConfig`，而认证处理器依赖使用编码器的 `AuthService`，形成 `securityConfig → jsonAuthHandlers → authService → passwordEncoder` 环。拆出 `PasswordConfig` 解决。
2. **CSRF 头名不一致**：`CookieCsrfTokenRepository` 默认头名为 `X-XSRF-TOKEN`，文档要求 `X-CSRF-TOKEN`，导致合法请求被判 403。已显式设置头名。
3. **方法级权限被吞成 500**：`@PreAuthorize` 抛出的 `AccessDeniedException` 发生在 Controller 调用内，不经过安全过滤器的 `AccessDeniedHandler`，被兜底异常处理成 500。已在 `GlobalExceptionHandler` 中单独映射为 403，并补 401 映射。
4. **登录后 CSRF 令牌轮换**：Spring Security 的会话固定保护会在认证成功后更换 CSRF 令牌。前端登录后必须重新获取令牌，否则首个写请求会 403。这属于正确行为，已在客户端 `clearCsrf()` 中处理。

## 5. 安全边界核对

- 登录失败提示不区分「账号不存在」与「密码错误」，`hideUserNotFoundExceptions` 已开启。
- 限流键为「账号 + 来源地址」，仅存在内存，不落库、不写日志；达到阈值后锁定 600 秒。
- 密码哈希 BCrypt(12)，`sys_user.password_hash` 不出现在任何响应 DTO 中。
- 会话表由 Flyway 迁移创建，应用不自动建表；会话超时 30 分钟。
- 审计只记录必要概况，不含密码、令牌、手机号。
- 初始管理员仅在账号表为空且提供 `CMS_INIT_USERNAME` / `CMS_INIT_PASSWORD` 时创建一次，无固定默认密码。

## 6. 遗留项

- 登录页未展示「剩余锁定时间」倒计时，仅在响应体与 `Retry-After` 头中返回。
- 审计日志 UI 未提供按操作人筛选，接口已支持 `actorId`。
- 账号管理页的浏览器内渲染待人工确认。
