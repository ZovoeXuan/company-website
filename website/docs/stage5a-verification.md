# 阶段 5-A 验证记录

日期：2026-11-02
范围：站点配置（SITE / NAVIGATION / ABOUT / CONTACT / PRIVACY）、公开站点接口、后台配置页、HTML 白名单清洗
结论：**通过**（真实 MySQL 8.4.13 + 真实浏览器）

## 1. 实现的接口

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/api/admin/configs/{key}` | 草稿、快照、状态与版本 |
| PUT | `/api/admin/configs/{key}/draft` | 保存草稿（乐观锁） |
| POST | `/api/admin/configs/{key}/publish` | 发布（ADMIN，双版本校验） |
| GET | `/api/public/site-config` | 站点信息，合并 SITE + NAVIGATION + CONTACT 摘要 |
| GET | `/api/public/pages/{ABOUT\|CONTACT\|PRIVACY}` | 单页内容，未发布 404 |

键名由枚举白名单控制，`HOME` 的编排属于 5-B，本阶段只保留基础校验。

## 2. 校验规则（服务端）

| 配置 | 关键规则 |
|---|---|
| SITE | 站点名称必填 ≤50；标语 ≤100；邮箱格式；联系方式四项各有长度上限 |
| NAVIGATION | 每项名称 1–12 字；路径必须以 `/` 开头且禁止 `://` 外链；主导航 ≤12 项、页脚 ≤6 项 |
| ABOUT | 简介/使命 ≤2000；历程年份必须 4 位数字；里程碑与资质各 ≤20 条 |
| CONTACT | 标题、描述、成功提示长度限制；`formEnabled` 控制前台是否展示表单 |
| PRIVACY | 更新日期必须是 `yyyy-MM-dd`；正文非空并经过 HTML 白名单清洗 |
| HOME | 首屏标题必填（5-B 再补完整编排） |

## 3. 验证结果

| 场景 | 预期 | 实际 |
|---|---|---|
| SITE 未发布时访问 `/api/public/site-config` | 404 | 404 |
| 保存草稿并发布 SITE | 200 | 200，公开接口返回站名、标语、联系方式、版权、备案号 |
| NAVIGATION 发布后 | 导航有 7 项 | 7 项，顺序与后台一致 |
| CONTACT 未发布时 | 站点信息仍可用，`formEnabled=false` | 通过（曾因空指针 500，已修复） |
| ABOUT 发布 | 公开页返回简介、使命、历程 | 通过 |
| PRIVACY 发布 | 返回清洗后的正文 | 通过，见下 |
| 版本冲突 | 旧 `lockVersion` 发布被拒 | 409 |
| 请求体结构错误 | 400 + fieldErrors | 400，提示缺少 `expectedLockVersion` 与 `draft` |

### XSS 清洗实测

提交的正文包含四种攻击载荷：

```html
<p>我们只收集必要信息。</p>
<script>alert(1)</script>
<p onclick="steal()">邮箱：contact@example.com</p>
<img src="x" onerror="alert(2)">
<a href="javascript:alert(3)">坏链接</a>
```

落库并公开返回的结果：

```html
<p>我们只收集必要信息。</p>
<p>邮箱：contact@example.com</p>
<img><a>坏链接</a>
```

`script` 整段移除，`onclick` / `onerror` 事件属性移除，`javascript:` 协议移除，非 http/https 的图片地址被清空。清洗在**入库时**执行，前端 `v-html` 渲染的是已清洗内容。

## 4. 后台页面

新增五个配置页：全站设置、导航与页脚链接、关于我们、联系我们、隐私说明。共用 `useConfig` 与 `ConfigPanel`，右侧统一显示发布状态、草稿/线上版本与保存/发布按钮。

## 5. 前端接入

`USE_MOCK` 中 `site`、`about`、`contact`、`privacy` 已置为 `false`，改读真实接口：

- 页头导航、页脚版权/备案/链接全部来自数据库
- 关于页正文与时间轴、联系页标题与描述、隐私页正文均来自发布快照
- SITE 未发布时页面顶部提示「站点内容尚未发布」，不再显示空白

同时修复：前台页脚原先固定用主导航当「快速入口」，后台配置的页脚链接不生效。现在优先使用 `footer.links`，未配置时回退到主导航。

## 6. 修复的缺陷

1. **公开站点接口空指针**：`CONTACT` 未发布时 `publishedOrEmpty` 返回 `null`，直接调用 `.get()` 抛 NPE 导致 500。改为按未启用处理。
2. **页脚链接不生效**：后台可配置但前台未使用，属于功能断点。

## 7. 未完成

- **HOME 配置与首页编排**：属 5-B，官网首页仍读 mock。
- **富文本编辑器**：后台仍是 HTML 源码框，清洗已就位，但编辑体验未改善。
- **咨询表单提交**：表单可展示、可前端校验，提交接口属阶段 6。
- **配置预览**：后台没有「预览未发布配置」入口，只能发布后看前台。
