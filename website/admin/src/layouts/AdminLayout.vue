<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuth } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const { current, logout } = useAuth()

const currentTitle = computed(() => String(route.meta.title ?? '管理后台'))
const isAdmin = computed(() => current.value?.role === 'ADMIN')

async function handleLogout() {
  try {
    await logout()
  } finally {
    await router.replace('/login')
  }
}
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

        <el-menu-item v-if="isAdmin" index="/leads">咨询管理</el-menu-item>

        <el-sub-menu v-if="isAdmin" index="system">
          <template #title>系统管理</template>
          <el-menu-item index="/system/users">账号管理</el-menu-item>
          <el-menu-item index="/system/audit-logs">操作日志</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header height="56px" class="header">
        <span class="page-title">{{ currentTitle }}</span>
        <div class="header-right">
          <span>{{ current?.displayName || current?.username }}</span>
          <el-tag size="small" :type="isAdmin ? 'danger' : 'info'">{{ current?.role }}</el-tag>
          <RouterLink to="/profile/password" class="link">修改密码</RouterLink>
          <el-button link type="primary" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>

      <el-main class="main">
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
  border-radius: 7px;
  background: linear-gradient(135deg, #7a6bff, #5b4be8);
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  box-shadow: 0 4px 14px rgba(91, 75, 232, 0.3);
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
  gap: 12px;
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
</style>
