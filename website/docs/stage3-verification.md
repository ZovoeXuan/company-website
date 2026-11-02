# 阶段 3 验证记录

日期：2026-11-02
范围：素材上传、分类、产品内容、草稿/发布状态机、公开内容接口、前端产品页接入
结论：**核心链路通过**（真实 MySQL + 真实浏览器），后台管理页面已完成

## 1. 实现的接口

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/api/admin/media` | 上传图片，multipart 字段 `file` |
| GET | `/api/admin/media` | 素材列表，支持 `keyword`、`referenced`、分页 |
| GET | `/api/admin/media/{id}/file` | 后台预览，鉴权、no-store |
| GET | `/api/admin/media/{id}/references` | 引用位置（草稿/发布） |
| DELETE | `/api/admin/media/{id}` | 有引用时拒绝（仅 ADMIN） |
| GET/POST | `/api/admin/categories` | 分类列表/新建 |
| PUT | `/api/admin/categories/{id}/draft` | 保存分类草稿 |
| POST | `/api/admin/categories/{id}/publish` | 发布（ADMIN） |
| POST | `/api/admin/categories/{id}/offline` | 下线，分类下有已发布内容时拒绝 |
| DELETE | `/api/admin/categories/{id}` | 已发布或仍被引用时拒绝 |
| GET/POST | `/api/admin/contents` | 内容列表/新建草稿 |
| GET | `/api/admin/contents/{id}` | 草稿 + 快照 + 版本 |
| GET | `/api/admin/contents/{id}/published` | 发布快照，无快照 404 |
| PUT | `/api/admin/contents/{id}/draft` | 保存草稿（乐观锁） |
| POST | `/api/admin/contents/{id}/publish` | 发布（ADMIN，双版本校验） |
| POST | `/api/admin/contents/{id}/offline` | 下线（ADMIN） |
| DELETE | `/api/admin/contents/{id}` | 已发布或被引用时拒绝 |
| GET | `/api/admin/contents/{id}/references` | 谁引用了它 |
| GET | `/api/admin/preview/contents/{id}` | 草稿预览 |
| GET | `/api/public/categories?type=` | 有效分类 |
| GET | `/api/public/products`・`/products/{id}` | 产品列表/详情（仅发布快照） |
| GET | `/api/public/solutions`・`/cases`・`/news` | 同一套通用实现，已就绪但未造数据 |
| GET | `/api/public/media/{id}` | 仅当被有效发布内容引用时返回 |

## 2. 验证结果

### 2.1 分类

| 场景 | 预期 | 实际 |
|---|---|---|
| 新建 PRODUCT 分类「设备管理」 | 201 | 201，id=1 |
| 发布分类 | 200 | 200 |
| 公开分类接口 | 只返回已发布 | `[{"id":"1","name":"设备管理","sortOrder":1}]` |
| 下线仍有已发布内容的分类 | 409 | 409 REFERENCED「该分类下仍有 1 条已发布内容」 |

### 2.2 素材

| 场景 | 预期 | 实际 |
|---|---|---|
| 上传 1440×900 PNG | 200，返回尺寸 | 200，`width=1440,height=900,byteSize=22173` |
| 伪装成 `.png` 的文本 | 415 | 415 UNSUPPORTED_MEDIA |
| 5.8 MB 文件 | 413 | 413 FILE_TOO_LARGE |
| 删除被引用的图片 | 409 | 409 REFERENCED |
| 引用面板 | 列出草稿与发布两处 | `stage=DRAFT/PUBLISHED`，`fieldPath=coverMediaId` |

格式判断只读文件头（PNG/JPEG/WebP），不信任扩展名与 Content-Type，也不做整图解码。

### 2.3 产品与发布状态机

| 场景 | 预期 | 实际 |
|---|---|---|
| 创建产品草稿 | 201，status=UNPUBLISHED | 201，draftVersion=1，lockVersion=0 |
| 未发布时公开详情 | 404 | 404 |
| 发布 | 200 | 200 |
| 发布后公开列表/详情 | 含封面、分类、能力标签 | 与前端类型一致 |
| 公开图片 | 200 image/png | 200，22173 字节 |
| 改草稿不发布 | 公开仍是旧标题 | 公开仍为「设备管理平台」，`hasUnpublishedChanges=true` |
| 用旧 lockVersion 发布 | 409 | 409 VERSION_CONFLICT |
| 用正确版本发布 | 200 | 200 |
| 发布后公开详情 | 变为「设备管理平台 V2」 | 一致 |
| 发布时缺分类 | 400 + fieldErrors | 400，`categoryId: 请选择分类` |
| 下线 | 200 | 200 |
| 下线后公开详情与图片 | 均 404 | 均 404（图片访问受发布来源约束） |
| 重新发布 | 200 | 200 |
| 删除已发布内容 | 409 | 409「请先下线」 |

### 2.4 前端接入

`web/src/api/public.ts` 改为逐资源开关，产品已切到真实接口：

```ts
const USE_MOCK = { site: true, home: true, products: false, solutions: true, ... }
```

浏览器实测 `/products`：分类按钮显示「设备管理」，计数「共 1 个产品」，卡片显示数据库中的「设备管理平台 V2」与上传的封面图，1440px 无横向溢出。

## 3. 后台界面

新增四个页面：内容列表、内容编辑、分类管理、素材管理。产品、方案、案例、新闻、客户标识共用同一套列表与编辑组件，按 `meta.contentType` 区分。

| 页面 | 验证内容 | 结果 |
|---|---|---|
| 产品列表 | 显示 2 条数据，状态为已发布/草稿，版本「草稿 v2 / 上线 v2」，有未发布修改标记 | 通过 |
| 产品编辑 | 打开已有内容，标题回填为「设备管理平台 V2」；改标题后点「保存草稿」，接口回读一致 | 通过 |
| 新建产品 | 表单为空，右侧提示「保存后显示版本信息」 | 通过 |
| 内容分类 | 「设备管理」显示为已发布，线上名称、排序、操作齐全；类型下拉可切换 | 通过 |
| 素材管理 | 2 张图片，引用状态分别为未引用/已引用，缩略图、尺寸、大小正常 | 通过 |

编辑器结构与限制：

- 核心字段：标题、摘要、分类、封面、能力标签、所属行业、正文、排序；新闻额外有展示日期。
- 封面通过素材选择弹窗完成，支持在弹窗内直接上传。
- **结构化字段（results、actions、outcome、quote 等）用「高级字段」JSON 维护**，保存时与核心字段合并。首版没有为每种类型做独立表单，这是取舍，不是遗漏。
- 正文为 HTML 源码输入，所见即所得编辑器尚未接入。

修复：素材列表原先直接输出 ISO 时间（`2026-11-02T06:22:16.844Z`），改为按本地时区显示到分钟。

未单独验证：在界面上点「发布 / 下线 / 删除」的确认弹窗全流程（三个接口已用 curl 验证）；「新建后自动跳转到编辑页」的路由跳转未做自动化断言。

## 4. 修复的缺陷

1. **JDBC 类型强转**：MySQL `INT UNSIGNED` 通过 JDBC 返回 `Long`，`(Integer) rs.getObject(...)` 抛 `ClassCastException`，导致公开分类接口 500。改为按 `Number` 取值。
2. **时区类型错误**：`LocalDate.atStartOfDay(ZoneOffset.UTC)` 返回 `ZonedDateTime`，无法赋给 `LocalDateTime`，编译失败。改为不带时区的 `atStartOfDay()`（数据库统一存 UTC 朴素时间）。

## 5. 未完成

- **方案/案例/新闻/客户标识的数据**：页面与接口都已就绪，但没有真实内容，官网对应页面仍读 mock。
- **站点配置、首页、导航、单页**：属阶段 5，`/api/public/site-config`、`/home`、`/pages/*` 尚未实现，官网导航与首页仍用 mock。
- **富文本编辑器**：正文目前按 HTML 字符串提交，需接入编辑器与服务端 XSS 清洗（当前未清洗，只有后台可写入）。
- **结构化字段的可视化编辑**：results/actions/outcome 等仍走 JSON。
- **图片像素上限**：只验证了 1440×900，未构造超 2000 万像素样本。
- **多实例部署**：媒体落本地磁盘、限流在进程内，多实例需要改共享存储与 Redis。
