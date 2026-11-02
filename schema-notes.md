# schema.sql 使用与字段说明

本文件与《公司官网后台及前后端联动开发文档》第 5、7、8、10 章一起使用。`schema.sql` 面向 MySQL 8.4；只创建九张业务表并初始化六条空配置草稿，不创建/删除数据库，不创建默认账号，不执行数据库安装。

**验证边界：本次仅完成文件与字段、约束、索引的静态一致性核对，尚未在真实 MySQL 8.4 实例执行。** 开发者须在独立空测试库运行迁移并完成读写、发布、并发幂等和约束测试后，才可以声明数据库验证通过。

## 1. 执行前提与迁移

1. 使用已经创建的空测试数据库，确认连接目标，使用 UTF-8 编码读取脚本。数据库默认可以与脚本相同，脚本也为每张表显式声明 InnoDB、utf8mb4 和 utf8mb4_0900_ai_ci。
2. 将基线纳入 Flyway 等迁移工具。不要反复执行整份脚本，也不要修改已经在共享环境执行的迁移。脚本故意不用 `IF NOT EXISTS / INSERT IGNORE` 掩盖表结构差异。
3. 迁移账号可以建表，应用账号按最小权限部署。脚本不含 `USE`、`CREATE DATABASE`、`DROP`、`TRUNCATE` 或任何账号/密码初始化；由部署人员明确选库。
4. MySQL DDL 会产生隐式提交；整份文件不是一个可全量回滚的事务。若空测试库执行失败，先检查错误，再在明确的测试库重建基线；不得对已有业务数据的库盲目重跑或清表。
5. JDBC、服务端及数据库连接会话采用 UTC。DATETIME(3) 自身不保存时区；应用提供 UTC 的 `created_at/updated_at/published_at/handled_at/consent_at`。初始化配置使用 `UTC_TIMESTAMP(3)`。API 返回带时区的 ISO 8601，Vue 按 Asia/Shanghai 显示。

## 2. 九张表与主要字段

| 表 | 关键字段 | 使用规则 |
|---|---|---|
| sys_user | username、password_hash、role_code、enabled、lock_version | ADMIN/EDITOR 固定角色；用户名 ASCII 且大小写不敏感唯一；应用校验字符白名单；密码只存 PasswordEncoder 哈希；不物理删除账号 |
| cms_config | config_key 与通用快照字段 | SITE/NAVIGATION/HOME/ABOUT/CONTACT/PRIVACY 六个固定单例；数据库限制不可删除或整体下线 |
| cms_category | category_type、快照、published_name、published_sort_order | PRODUCT/SOLUTION/CASE/NEWS，一层分类；CUSTOMER 不分类 |
| cms_content | content_type、快照与四个发布投影 | PRODUCT/SOLUTION/CASE/NEWS/CUSTOMER；无 slug，官网用稳定 ID 路由 |
| cms_media | storage_key、original_name、extension、mime_type、byte_size、width、height | 首版只存成功验证并落盘的 JPG/JPEG/PNG/WebP；5 MiB、2000 万像素限制；不允许文件覆盖 |
| cms_media_ref | source_kind/source_id、stage、media_id、field_path | 标记草稿/发布快照在何处用图，包含富文本中的图片 |
| cms_content_ref | source_kind/source_id、stage、target_content_id、field_path | 记录首页、导航、推荐内容等结构化关联；不用于分类引用 |
| cms_lead | 幂等、加密手机号、来源、同意版本、状态、last_note、lock_version | 与 CMS 发布模型无关；无首版删除、历史跟进表或导出 |
| sys_audit_log | actor_id、action、target_type/id、版本、request_id、result、summary | 只写最少必要概况；普通后台只读；不记录敏感正文和凭据 |

所有 ID 使用 BIGINT UNSIGNED，Java 可使用 Long 并将实际生成值控制在其正数范围，对外 JSON 序列化为字符串。DTO 使用 camelCase，数据库使用 snake_case；例如 `contentType → content_type`、`displayTime → published_display_time`（仅发布投影）、`sourcePath → source_path`、`sourcePosition → source_position`、`consentVersion → consent_version`、`lastNote → last_note`。

## 3. 快照与查询投影

三张 CMS 主表均有 draft_json、published_json、publication_status、draft_version、published_version、lock_version、published_at/by、created_at/by、updated_at/by、deleted。JSON 必须是对象；不能向客户端开放任意 JSON 透传。空草稿 `{}` 合法，发布必填校验由固定 Java DTO 完成。

- 保存草稿：只修改 draft_json、draft_version、lock_version、更新时间与 DRAFT 引用；不改变发布状态、快照或发布投影。
- 发布：同事务写快照、published_version、状态、发布时间/发布人、查询投影、PUBLISHED 引用和成功审计。published_version 可以小于 draft_version，表示线上仍是旧稿。
- 下线：保留快照与引用，公开接口不再返回。内容/分类已发布时不能逻辑删除，必须先下线并解除入站引用。
- 配置不提供整体下线；用模块开关控制显示。所有六条初始化记录都是 UNPUBLISHED、draft_version=1、lock_version=0，线上快照为空，created_by/updated_by 允许初始化为空。
- cms_content 的 published_title、published_category_id、published_sort_order、published_display_time 只能由发布 DTO 提取。其他类型不使用 displayTime；新闻发布必须有该时间。分类也只在发布时写 published_name、published_sort_order。
- 数据库 CHECK 验证基本状态、JSON 类型和版本范围，无法验证整份 DTO、权限或安全清洗；不能把“插入成功”理解为“内容可发布”。

分类的 active_published_name 是数据库生成列：仅在 `publication_status='PUBLISHED' AND deleted=0` 时取 published_name，其他状态为 NULL。唯一键 `(category_type, active_published_name)` 保证两个并发事务不能上线同类型同名分类；应用仍提前校验并把唯一键冲突转换为可理解的 409/字段错误。名称比较遵守表的 utf8mb4_0900_ai_ci 排序规则，发布前统一去除首尾空白。生成列不能由应用赋值。

sortOrder 按主稿限定为 0～9999。公开新闻按 `published_display_time DESC, id DESC`；其他内容按 `published_sort_order ASC, id DESC`。查询分类筛选时同时检查分类当前状态和类型；索引不代替公开状态过滤。后台列表参数使用 `contentType`，不要与旧建议里的 `type` 混用。主稿中的上传 5MB 在本基线精确定义为 5×1024×1024=5,242,880 字节，Java、代理和前端配置采用同一上限。

## 4. 外键、引用位置和应用层约束

外键负责单目标记录存在性：操作人到 sys_user、published_category_id 到 cms_category、素材引用到 cms_media、内容引用/线索来源到 cms_content。外键未配置级联操作，默认阻止被物理删除的目标；正常 CMS 采用逻辑删除，是否可删仍由服务端判断。

source_kind 为 CONFIG/CONTENT/CATEGORY，source_id 是对应表 ID；这是多态来源，MySQL 单个外键不能同时指向三张表。应用在保存、发布、删除事务中按统一顺序加锁并校验真实来源，不能仅凭 source_id 存在就算有效。field_path 为最长 512 的 ASCII 稳定字段路径，例如 `HOME.services.groups[0].contentIds[1]`、`PRODUCT.bodyHtml.img[0]`；同一目标在两个位置出现要保存两行。唯一约束同时包含来源、阶段、目标、字段位置。

保存/发布均从经过清洗的 DTO 重算相应阶段引用；前端不能直接维护引用表。来源下线保留引用；来源逻辑删除后清理其出站引用，删除目标前检查其他来源的 DRAFT 和 PUBLISHED 入站引用。不要仅凭“有 PUBLISHED 引用行”开放图片：还须核对来源当前已发布、未删除及有效分类。

以下规则由应用事务保证，不能由现有外键单独保证：

- content_type/category_type 创建后不可改；发布分类类型匹配，CUSTOMER 不带分类。
- published_by 正常发布必须填写；正常编辑 created_by/updated_by 必须填写。SQL 仅对初始化允许空，外键只检查非空值对应账号存在。
- 分类删除前既查 published_category_id，又扫描有效来源 draft_json 的 categoryId；不能只检查线上投影。分类有已发布使用者时禁止下线；只有草稿使用者时允许下线但禁止删除。
- 内容引用目标必须满足本字段指定的类型和已发布状态；公开聚合再次过滤后来失效的对象。
- `cms_lead.content_type/content_id` 必须同时为空或成对填写；限 PRODUCT/SOLUTION/CASE，首次受理时校验目标有效。后续目标下线不删除既有业务线索。
- 禁止禁用/降级最后一个启用 ADMIN；通过事务锁住管理员集合或一致的专用锁完成并发检查，不能只先 count 后无条件修改。
- 更新用户与处理线索都比较 expectedLockVersion，成功后递增 lock_version。会话撤销由框架/应用完成，不是改 enabled 后任由旧会话继续使用。

这些分工也避免在 CHECK 中加入跨表查询或外键级联动作；MySQL CHECK 不支持跨表子查询，不能将完整业务校验塞入建表语句。

## 5. 咨询幂等与敏感字段

`idempotency_key` 为规范化小写 UUID，数据库全局唯一；`receipt_no` 是独立安全随机值且唯一，用于原样返回受理结果。`payload_hash` 保存规范化请求的 HMAC-SHA256 原始 32 字节，不保存十六进制字符串，不把手机号等完整载荷存到审计日志。规范化应固定字段顺序、空值规则、Unicode/空白处理及版本，并纳入姓名、手机、公司、需求、来源、关联内容、consent=true、consentVersion。不得加入服务端受理时间等每次重试变化的字段。

`payload_hash_version` 用于选择对应的规范化/摘要密钥版本；更换密钥时保留仍在幂等保留期内旧版本的验证能力。用于幂等比较的密钥、手机限流 HMAC 密钥、手机号加密密钥按用途隔离，不存数据库或源码。

提交顺序：解析并规范化请求 → 查询已有 key → 已存在则用其版本计算并比较摘要，相同返回原 receiptNo、不同返回 409 → 不存在才校验当前表单开关、隐私版本、关联内容与业务限流 → 插入线索。并发插入若撞唯一键，回滚该次事务，在有效的新事务中重读已成功行并比较摘要；不要吞掉所有 SQL 异常并假装成功。既有成功请求的重试可以在表单后来关闭或隐私说明更新后返回原结果，不重新入库、不重复消耗业务提交额度；仍受通用请求频率保护。

phone_ciphertext 保存成熟认证加密方案输出的版本化信封（含密钥版本、随机 nonce、密文、tag），长度上限 1024 字节；phone_last4 只用于列表脱敏，不提供完整号码搜索，也不对末四位建立全量搜索入口。完整手机仅管理员查看详情时解密并审计；手机号格式、数字末四位及加密信封结构由应用验证。`consent_version` 是受理时 PRIVACY 的 published_version 整数，consent_at 是服务端受理同意时间，不关联“当前隐私版本”的外键，以免后来发布破坏历史同意记录。

IP 与手机号限流使用带服务端密钥的摘要，在单实例内存或已有受控短期存储中设置 TTL；首版业务表不长期保存原始 IP、IP hash 或全量手机号 HMAC。反向代理与访问日志也要配套减少原始 IP 和表单内容留存。应用多实例后再选择共享限流存储，不为本首版强制新增 Redis。

保留期由公司决定并写入运维手册。线索到期受控清理后，该行幂等结果也会消失，因此承诺的重试窗口须短于留存窗口；不承诺永久记住所有历史 key。首版 API 无删除按钮；备份与加密密钥也按批准策略保留与恢复。

## 6. Spring Session JDBC 单独初始化

本脚本不手写 SPRING_SESSION 等框架表。项目确定 Spring Boot / Spring Session JDBC 依赖后，从实际依赖 JAR 内 `org/springframework/session/jdbc/schema-mysql.sql` 获取框架自带脚本，核对该依赖版本的官方说明，并纳入独立迁移。若该版本路径发生变化，以实际依赖为准。

测试和生产只选一种表初始化责任：推荐迁移工具创建框架表，并关闭框架重复建表；不要让应用自动初始化与 Flyway 同时争抢创建。会话 30 分钟无操作失效、索引清理、principalName 查询与全部会话撤销都要在集成测试中验证。账号主键和 principalName 的映射由应用保持稳定，不改框架表结构凑业务字段。

## 7. 必做验证清单

- 在真实 MySQL 8.4 空测试库执行；业务表正好九张，配置正好六条且全部未发布，sys_user 空；session 表单独迁移。
- 验证非对象 JSON、错误类型/状态、缺少发布快照的 PUBLISHED、超限图片、重复幂等 key 会被拒绝。
- 两个事务并发发布同类型同名分类，一次成功另一次失败且无部分更新；不同分类类型可同名，下线名称不占上线唯一位。
- 保存已发布内容的新标题、图片、分类和排序后，公开投影与快照仍旧；发布后全部一起变化。
- 相同 key 相同载荷并发只生成一条线索，相同 key 不同载荷返回 409；表单关闭后旧成功重试返回旧回执、新 key 被拒绝。
- 线索/用户 expectedLockVersion 冲突、最后管理员并发降级、素材删除/发布竞争均不能覆盖或产生悬空公开引用。
- 初始化无需默认密码；数据库备份中不存在明文手机，加密密钥独立备份且可受控恢复。

设计时核对的数据库规则见 [MySQL 8.4 CHECK 约束](https://dev.mysql.com/doc/refman/8.4/en/create-table-check-constraints.html) 和 [生成列规则](https://dev.mysql.com/doc/refman/8.4/en/create-table-generated-columns.html)。文件存在及静态核对不等于真实数据库执行通过。
