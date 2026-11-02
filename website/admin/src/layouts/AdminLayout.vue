<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const currentTitle = computed(() => String(route.meta.title ?? '管理后台'))
</script>

<template>
  <el-container class="shell">
    <el-aside width="212px" class="aside">
      <div class="brand">
        <span class="logo">示</span>
        <span class="brand-name">内容管理后台</span>
      </div>

      <el-menu :default-active="route.path" router class="menu">
        <el-menu-item index="/dashboard">工作台</el-menu-item>

        <el-sub-menu index="site">
          <template #title>站点管理</template>
          <el-menu-item index="/settings/site">全站设置</el-menu-item>
          <el-menu-item index="/settings/navigation">导航与页脚</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/home">首页管理</el-menu-item>

        <el-sub-menu index="contents">
          <template #title>内容管理</template>
          <el-menu-item index="/contents/products">产品</el-menu-item>
          <el-menu-item index="/contents/solutions">解决方案</el-menu-item>
          <el-menu-item index="/contents/cases">客户案例</el-menu-item>
          <el-menu-item index="/contents/news">新闻</el-menu-item>
          <el-menu-item index="/contents/customers">客户标识</el-menu-item>
          <el-menu-item index="/contents/categories">内容分类</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="pages">
          <template #title>单页管理</template>
          <el-menu-item index="/pages/about">关于我们</el-menu-item>
          <el-menu-item index="/pages/contact">联系我们</el-menu-item>
          <el-menu-item index="/pages/privacy">隐私说明</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/media">素材管理</el-menu-item>
        <el-menu-item index="/leads">咨询管理</el-menu-item>

        <el-sub-menu index="system">
          <template #title>系统管理</template>
          <el-menu-item index="/system/users">账号管理</el-menu-item>
          <el-menu-item index="/system/audit-logs">操作日志</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header height="56px" class="header">
        <div class="header-left">
          <span class="page-title">{{ currentTitle }}</span>
        </div>
        <div class="header-right">
          <span class="user">未登录</span>
          <RouterLink to="/login" class="link">去登录</RouterLink>
        </div>
      </el-header>

      <el-main class="main">
        <el-alert
          type="warning"
          :closable="false"
          show-icon
          title="骨架阶段：登录鉴权尚未接入"
          description="按开发计划，登录、CSRF、角色与审计在阶段 2 完成。当前可直接访问后台页面，仅用于验证路由与布局，不得作为可交付状态。"
          class="banner"
        />
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.shell {
  height: 100vh;
}

.aside {
  background: #fff;
  border-right: 1px solid var(--a-border);
  display: flex;
  flex-direction: column;
  overflow-y: auto;
}

.brand {
  display: flex;
  align-items: center;
  gap: 9px;
  height: 56px;
  padding: 0 16px;
  border-bottom: 1px solid var(--a-border);
}

.logo {
  width: 26px;
  height: 26px;
  border-radius: 6px;
  background: #e6f1fb;
  color: var(--a-brand);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
}

.brand-name {
  font-size: 13px;
  font-weight: 500;
}

.menu {
  border-right: none;
  padding: 8px;
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid var(--a-border);
}

.page-title {
  font-size: 15px;
  font-weight: 500;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
  font-size: 13px;
  color: var(--a-text-muted);
}

.link {
  color: var(--a-brand);
}

.main {
  padding: 18px 20px;
  overflow-y: auto;
}

.banner {
  margin-bottom: 16px;
}
</style>
