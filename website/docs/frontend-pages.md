# 官网前端页面说明

日期：2026-11-02
范围：`website/web`（公开站点），管理后台不在此文范围
状态：九个页面全部实现，数据走可替换的 mock 层；公开接口尚未接入

## 1. 页面与路由

| 路由 | 页面 | 主要内容 |
|---|---|---|
| `/` | 首页 | 首屏、服务规模、核心产品、客户案例、服务行业、最新动态、行动区 |
| `/products` | 产品列表 | 分类筛选、卡片网格、分页、空态 |
| `/products/:id` | 产品详情 | 封面、能力标签、富文本正文、相关案例、粘性咨询卡 |
| `/solutions` | 方案列表 | 图文方案卡，按行业 |
| `/solutions/:id` | 方案详情 | 挑战、做法步骤、结果指标、相关产品 |
| `/cases` | 案例列表 | 行业筛选、结果指标、分页 |
| `/cases/:id` | 案例详情 | 关键结果、背景、动作步骤、客户引言、相关产品 |
| `/news` | 新闻列表 | 缩略图、日期、分类、分页 |
| `/news/:id` | 新闻详情 | 正文、相邻文章、返回入口 |
| `/about` | 关于我们 | 简介、使命、发展时间轴、资质 |
| `/contact` | 联系我们 | 联系信息、咨询表单（前端校验） |
| `/privacy` | 隐私说明 | 正文与更新时间 |
| 其他 | 404 | 返回入口，`noindex` |

## 2. 数据来源与切换

页面只依赖 `src/api/public.ts` 导出的函数，返回结构按后端公开 DTO 定义（见 `src/data/types.ts`）。

```ts
const USE_MOCK = {
  site: false,      // 已接真实接口
  home: true,       // 首页编排待做（5-B）
  products: false,  // 已接
  solutions: true,
  cases: true,
  news: true,
  about: false,     // 已接
  contact: false,   // 已接
  privacy: false,   // 已接
}
```

- `true`：读取 `src/data/mock.ts`，前端可独立开发与演示。
- `false`：走 `GET /api/public/*`，函数体内已写好真实请求与参数。

逐资源切换，避免整站被未完成接口卡住。mock 内容全部为示例，不代表真实业绩，上线前必须由后台内容替换。

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

构建产物：JS 154 KB（gzip 55 KB）、CSS 30 KB（gzip 5.4 KB），无第三方 UI 框架。

## 6. 待接后端

1. 公开接口：`/api/public/site-config`、`/home`、`/products`、`/solutions`、`/cases`、`/news`、`/pages/{ABOUT|PRIVACY}`。
2. 咨询提交：联系页表单目前只做前端校验，提交后提示「演示模式，未真实发送」，接口在阶段 6 接入。
3. 图片：当前为占位 SVG，需替换为素材库上传的真实图片。
4. 详情页 slug：首版用稳定 ID，`canonical` 与 sitemap 需在内容发布后生成。

## 7. 已知限制

- 首页、列表页首屏为客户端渲染，SEO 依赖搜索引擎执行 JS；若要求更强的收录效果，需在阶段 7 评估预渲染或 SSR。
- `ORIGIN` 为占位域名 `https://www.example.com`，上线前必须替换，否则 canonical 与 OG 地址错误。
- 富文本使用 `v-html` 渲染，安全依赖后端清洗；前端不做二次过滤。
