# 官网前端页面说明

日期：2026-11-02
范围：`website/web`（公开站点），管理后台不在此文范围
状态：九个页面全部实现，数据统一走 `/api/public/*`，示例数据层已删除

## 1. 页面与路由

| 路由 | 页面 | 主要内容 |
|---|---|---|
| `/` | 首页 | 首屏、服务规模、核心产品、客户案例、服务行业、最新动态、行动区 |
| `/products` | 产品列表 | 分类筛选、卡片网格、分页、空态 |
| `/products/:id` | 产品详情 | 封面、能力列表、富文本正文、相关案例、粘性咨询卡 |
| `/solutions` | 方案列表 | 图文方案卡，按分类筛选、分页 |
| `/solutions/:id` | 方案详情 | 业务痛点、正文、价值说明、相关产品、粘性咨询卡 |
| `/cases` | 案例列表 | 分类筛选、结果指标、分页 |
| `/cases/:id` | 案例详情 | 客户与行业、正文、关键结果、客户引言、关联内容 |
| `/news` | 新闻列表 | 缩略图、日期、分类、分页 |
| `/news/:id` | 新闻详情 | 作者与来源、正文、相邻文章、返回入口 |
| `/about` | 关于我们 | 简介、使命、发展时间轴、资质 |
| `/contact` | 联系我们 | 联系信息、咨询表单（前端校验） |
| `/privacy` | 隐私说明 | 正文与更新时间 |
| 其他 | 404 | 返回入口，`noindex` |

## 2. 数据来源

页面只依赖 `src/api/public.ts` 导出的函数，返回结构按后端公开 DTO 定义（见 `src/data/types.ts`）。

- 列表与详情：`GET /api/public/{products|solutions|cases|news}`，支持 `page`、`pageSize`、`categoryId`、`keyword`。
- 首页：`GET /api/public/home`，区块由后台 HOME 配置编排，失效引用由后端过滤。
- 站点与单页：`GET /api/public/site-config`、`/api/public/pages/{ABOUT|PRIVACY|CONTACT}`。
- 页面区分加载、空结果与请求失败；失败不会被静默当作空列表。
- 正文由后端白名单清洗后以 `bodyHtml` 下发，前端 `v-html` 渲染，不做二次过滤。

示例数据文件 `src/data/mock.ts` 已删除，避免演示内容与真实内容混用。

## 3. SEO 实现

| 项 | 做法 | 位置 |
|---|---|---|
| 标题 | 每页唯一，`页面名｜示例科技`，详情页带内容标题 | `composables/usePageMeta.ts` |
| 描述 | 每页独立，20–50 字 | 同上 |
| canonical | 按当前路径生成绝对地址 | 同上 |
| robots | 正常页 `index, follow`；404 与详情页未命中时 `noindex` | 同上 |
| Open Graph | `og:title/description/url/site_name/type` | 同上 |
| 结构化数据 | Organization JSON-LD（名称、地址、联系方式） | `index.html` |
| 爬虫规则 | `robots.txt` 屏蔽 `/admin/`，声明 sitemap | `public/robots.txt` |
| 站点地图 | 主要栏目，详情页待后端生成后补充 | `public/sitemap.xml` |
| 语义结构 | 每页恰好一个 `<h1>`，标题层级连续，`<article>/<section>/<nav>/<time>` | 各页面 |

## 4. 无障碍实现

- 跳过导航：首个可聚焦元素 `跳到主要内容`，指向 `#main`。
- 地标：`header` / `nav[aria-label]` / `main` / `footer`，页脚导航单独标注。
- 键盘：移动端菜单按钮带 `aria-expanded` 与 `aria-controls`，Esc 关闭；焦点样式统一为 2px 描边 + 2px 偏移，不使用 `outline: none`。
- 当前页：路由激活项由 `aria-current` 标识；分页当前页同样标注。
- 图片：全部带 `alt`，装饰性图标 `aria-hidden`，图片写明 `width/height` 避免布局抖动。
- 表单：每个输入有 `<label for>`，错误信息 `role="alert"` 并通过 `aria-describedby` 关联，`aria-invalid` 标记错误态。
- 状态提示：加载、空态、错误均有文字说明，不依赖颜色。
- 动效：`prefers-reduced-motion` 下关闭滚动与过渡。

## 5. 审计结果

脚本：`docs/audit-seo-a11y.mjs`、`docs/audit-layout.mjs`（依赖本机 Chrome 的调试端口）。

| 检查项 | 结果 |
|---|---|
| 13 条路由标题唯一 | 通过 |
| 每页一个 `h1` | 通过 |
| 图片缺 `alt` | 0 |
| 链接无可访问名称 | 0 |
| 表单控件无标签 | 0 |
| 重复 `id` | 0 |
| 404 页 `noindex` | 通过 |
| 390px 横向溢出 | 无（`scrollWidth` = 390） |
| 1440px 横向溢出 | 无 |

构建产物：JS 146.8 KB（gzip 50.0 KB）、CSS 33.3 KB（gzip 5.9 KB），无第三方 UI 框架。

## 6. 待接后端

1. 咨询提交：联系页与详情页表单目前只做前端校验，`POST /api/public/leads` 在阶段 6 接入。
2. 详情页 sitemap：内容发布后由后端生成，当前 `sitemap.xml` 仅含主要栏目。
3. 图片：开发库为一张占位 PNG，上线前替换为素材库上传的真实图片。

## 7. 已知限制

- 首页、列表页首屏为客户端渲染，SEO 依赖搜索引擎执行 JS；若要求更强的收录效果，需在阶段 7 评估预渲染或 SSR。
- `ORIGIN` 为占位域名 `https://www.example.com`，上线前必须替换，否则 canonical 与 OG 地址错误。
- 富文本使用 `v-html` 渲染，安全依赖后端清洗；前端不做二次过滤。
