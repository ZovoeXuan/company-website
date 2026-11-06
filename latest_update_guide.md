# 公司官网 (`ZovoeXuan/company-website`) 全站视频 1:1 复刻与全局 CMS 升级完整指南

本更新包已将您提供的 **4 段实录视频**（《首页.mp4》《客户案例.mp4》《新闻中心.mp4》《关于我们.mp4》）与 **2 张高清截图**（《产品与解决方案.jpg》《预约产品展示.png》）中的所有模块结构、动效细节、弹窗形式、小七 AI 抽屉以及后台全局自定义编辑能力全部整合完毕。

## 一、一键覆盖更新步骤

1. 下载本轮生成的 `apply_latest_update.py`，放到您本地项目根目录（即包含 `website/` 文件夹的目录，例如 `D:\中转站\公司官网\apply_latest_update.py`）。
2. 在项目根目录打开终端运行：

```bash
python apply_latest_update.py
```

3. 脚本会自动清理已废弃的 `RoiCalculatorModal.vue`，自动备份原文件至 `.update_backup_时间戳/` 目录，并一键写入本次升级的 **20 个前后端与后台管理核心文件**。
4. 重启或刷新前端（`website/web`、`website/admin`）与后端（`website/server`）即可查看完整效果。

## 二、本次完整整合的 20 个核心文件与功能对照表

| 序号 | 文件路径 | 所属端 | 核心升级与 1:1 复刻内容 |
| :--- | :--- | :--- | :--- |
| 1 | `website/web/src/directives/reveal.ts` | 前台 Web | 全局流动式 `v-reveal` 指令：滑到哪里缓慢上浮淡入弹出 |
| 2 | `website/web/src/data/types.ts` | 前台 Web | 完整扩展小七 AI、双二维码、多邮箱、首屏深浅主题、11条折线引线、三圆点舞台、CEO信、高管弹窗、资料库等类型 |
| 3 | `website/web/src/composables/consult.ts` | 前台 Web | 拆分 `openConsult`（唤起居中「预约产品展示」弹窗）与 `openXiaoQi`（唤起右侧「小七 AI」抽屉） |
| 4 | `website/web/src/components/SiteHeader.vue` | 前台 Web | 一级菜单品牌紫高亮（无下划线）、全宽 `#eaedf2` 5 列下拉通栏大菜单、右侧「小七 AI」两行循环打字机与水滴机器人微浮动画、常驻黑色「产品试用」按钮 |
| 5 | `website/web/src/components/ConsultDialog.vue` | 前台 Web | 1:1 还原《预约产品展示.png》居中白底弹窗（右上角 `⊗` 关闭图标 + 居中标题） |
| 6 | `website/web/src/components/ConsultForm.vue` | 前台 Web | 1:1 还原 5 项浅灰无边框表单（姓名、联系电话、手机验证码 + 右侧黑色「点击获取验证码」60s倒计时按钮、公司名称、需求描述）+ 全宽黑色「立即预约」按钮 |
| 7 | `website/web/src/components/XiaoQiDrawer.vue` | 前台 Web | 1:1 还原《首页.mp4》00:34–00:41 右侧小七 AI 抽屉：历史会话菜单 `☰`、关闭窗口、蓝紫身份卡（我是 小七 + 专业认证 + 5 个能力胶囊）、「↻ 换一批」轮换 5 条高频问题、流式打字回答、「深度思考 DeepSeek R1」推理过程展示 + 外部大模型 API 接入 |
| 8 | `website/web/src/components/SiteFooter.vue` | 前台 Web | 1:1 还原深色 6 栏页脚：左侧斜体 `G` 标、4 列导航（「关于我们」子项直达 `/about?tab=xxx`）、「联系我们」双邮箱附灰色职能小字、「关注我们」公众号/视频号上下垂直双二维码、「网站语言」与备案版权同列对齐 |
| 9 | `website/web/src/components/FloatingActions.vue` | 前台 Web | 移除右下角遮挡画面的悬浮工具条，保持全屏视觉干净 |
| 10 | `website/web/src/layouts/SiteLayout.vue` | 前台 Web | 全局挂载顶部导航、深色页脚、居中预约弹窗、右侧小七 AI 抽屉与视频播放弹窗 |
| 11 | `website/web/src/views/HomeView.vue` | 前台 Web | 1:1 还原《首页.mp4》：3 屏全屏轮播（深浅双主题+左下角 3 根动态进度条）、「G 大数据」（8 项右上角上标 `+` 指标轮流紫色高亮 + 地面声波环 + 11 条 90度折线引线车体图）、左侧三圆点纵向切换舞台（`IoT服务` 雷达跳数预警、`交易服务` 扫码加油 ¥202 小票、`产业物流数字化` 4 项切换与正面吊 RFID 引线标签弹出） |
| 12 | `website/web/src/views/CasesView.vue` | 前台 Web | 1:1 还原《客户案例.mp4》：立交透视 Banner + 悬浮 6 分类条 + 4×2 案例卡片（封面居中联名水印 Logo、悬停胶囊变深灰底白字、了解更多变紫色）+ 黑色圆圈分页器 + 底部 2×2「客户成功故事」视频证言卡（左下角白色方形 `▶` 播放视频） |
| 13 | `website/web/src/views/NewsView.vue` | 前台 Web | 1:1 还原《新闻中心.mp4》：数字地球粒子 Banner + 悬浮 7 分类条（右侧带「更多标签 v」下拉菜单）+ 50/50 浅灰蓝左文右图置顶头条卡 + 4×2 新闻网格 + 黑色圆圈分页器 |
| 14 | `website/web/src/views/AboutView.vue` | 前台 Web | 1:1 还原《关于我们.mp4》5 大独立切换子页面与双弹窗：`企业简介`（含 CEO的一封信与全文滚动弹窗、发展历程循环时间轴、7 城内部滚动容器与地图大厦实景联动）、`管理团队`（4 列高管肖像与左右分栏详情弹窗）、`媒体中心`（4 卡轮播）、`资料共享`（4 分类切换 + 4 卡轮播 + `↓` 下载/播放按钮）、`加入我们`（海报横幅 + 使命/愿景/含「主人翁精神」的 5 大价值观） |
| 15 | `website/admin/src/views/SiteSettingsView.vue` | 后台 Admin | 新增「页脚双二维码与多邮箱职能说明」及「🤖 小七 AI 助手与大模型 API 接入」可视化配置页签 |
| 16 | `website/admin/src/views/AboutPageView.vue` | 后台 Admin | 升级为 5 大页签编辑器：支持后台直接修改企业简介、CEO信件摘录与全文、管理团队高管履历、资料库 4 分类卡片、客户成功故事视频、加入我们与 5 大价值观、发展历程及 7 城地图坐标/实景图 |
| 17 | `website/admin/src/views/HomeManageView.vue` | 后台 Admin | 支持首屏每张轮播独立设置「深色/浅色主题、署名行、免费下载提示、背景水印」、G 大数据 8 项指标与 11 条折线引线标签文字、三圆点舞台文案与弹出标签配置 |
| 18 | `website/admin/src/views/PreviewHomeView.vue` | 后台 Admin | 全站实景交互预演工作台（Visual Live Studio），支持画布级所见即所得直改、模块排序与一键全局发布 |
| 19 | `website/server/.../SiteConfigValidator.java` | 后端 Server | 放行并持久化 `xiaoqiConfig`、`contactEmails`、`qrCodes`、`ceoLetter`、`teamMembers`、`resources`、`customerStories`、`showcaseStages`、`hotspots` 等全部扩展 JSON 字段 |
| 20 | `website/server/.../HomeAggregateService.java` | 后端 Server | 聚合输出首屏深浅主题与水印字段、G 大数据前缀与 11 条折线热点、三圆点纵向切换舞台数据 |

## 三、后台如何全局修改为您自己公司的业务内容

由于贵司仅采用视频中的视觉模块、动效与弹窗形式（而非物流公司），您可以在后台管理系统中将所有默认文案与配图一键替换为您自己的行业内容：

* **修改站点名称、页脚双二维码、多邮箱与「小七 AI」**：进入后台 **「全站设置」**，在「3. 🤖 小七 AI 助手与大模型接入」页签中，可将“小七 / 数字物流专家”改为贵司专属的 AI 名称与行业专家头衔，自由增删 5 个能力胶囊与“换一批”题库，或填入 DeepSeek / OpenAI 兼容接口地址与 API Key。
* **修改首页 3 屏轮播、大数据 8 项指标与 11 条折线引线、三圆点舞台**：进入后台 **「首页管理」** 或 **「实景交互预演台」**，可直接修改每一屏的深浅主题与标题、修改 8 项指标数值、修改 11 个折线引线节点名称，以及修改三圆点舞台的标题、描述和右侧弹出的胶囊标签。
* **修改关于我们 5 大子页面与客户成功故事**：进入后台 **「关于我们」**，5 个页签分别对应前台的「企业简介 & CEO信」「管理团队」「资料库 & 客户成功故事」「加入我们 & 5大价值观」「发展历程 & 7城地图」，修改后点击「保存草稿」并「发布」即可实时生效。