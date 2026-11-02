# 阶段 3 验证记录

日期：2026-11-02
范围：素材上传、分类、产品内容、草稿/发布状态机、公开内容接口、前端产品页接入
结论：**核心链路通过**（真实 MySQL 8.0.46 + 真实浏览器）；后台管理页面尚未实现

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

## 3. 修复的缺陷

1. **JDBC 类型强转**：MySQL `INT UNSIGNED` 通过 JDBC 返回 `Long`，`(Integer) rs.getObject(...)` 抛 `ClassCastException`，导致公开分类接口 500。改为按 `Number` 取值。
2. **时区类型错误**：`LocalDate.atStartOfDay(ZoneOffset.UTC)` 返回 `ZonedDateTime`，无法赋给 `LocalDateTime`，编译失败。改为不带时区的 `atStartOfDay()`（数据库统一存 UTC 朴素时间）。

## 4. 未完成

- **后台管理页面**：内容、分类、素材目前只有接口，管理站仍是占位页。阶段 3 的「后台可编辑」尚未交付。
- **方案/案例/新闻**：通用实现已就绪，但没有后台录入界面，也没有真实数据。
- **站点配置、首页、导航、单页**：属阶段 5，`/api/public/site-config`、`/home`、`/pages/*` 尚未实现，官网这些页面仍用 mock。
- **富文本编辑器**：正文目前按 HTML 字符串提交，后台需要接入编辑器与 XSS 清洗。
- **图片像素上限**：只验证了 1440×900，未构造超 2000 万像素样本。
