<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ApiError } from '@/api/http'
import {
  createCategory,
  deleteCategory,
  listCategories,
  offlineCategory,
  publishCategory,
  updateCategory,
  type CategoryView,
} from '@/api/content'

const types = [
  { value: 'PRODUCT', label: '产品' },
  { value: 'SOLUTION', label: '解决方案' },
  { value: 'CASE', label: '客户案例' },
  { value: 'NEWS', label: '新闻' },
]

const loading = ref(false)
const rows = ref<CategoryView[]>([])
const total = ref(0)
const query = reactive({ type: 'PRODUCT', page: 1, pageSize: 20 })

const dialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const saving = ref(false)
const form = reactive({ id: '', name: '', sortOrder: 9999, lockVersion: 0 })

async function load() {
  loading.value = true
  try {
    const data = await listCategories({ categoryType: query.type, page: query.page, pageSize: query.pageSize })
    rows.value = data.items
    total.value = data.total
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '分类加载失败')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  dialogMode.value = 'create'
  Object.assign(form, { id: '', name: '', sortOrder: 9999, lockVersion: 0 })
  dialogVisible.value = true
}

function openEdit(row: CategoryView) {
  dialogMode.value = 'edit'
  Object.assign(form, { id: row.id, name: row.name, sortOrder: row.sortOrder, lockVersion: row.lockVersion })
  dialogVisible.value = true
}

async function submit() {
  if (!form.name.trim()) {
    ElMessage.warning('请填写分类名称')
    return
  }
  saving.value = true
  try {
    if (dialogMode.value === 'create') {
      await createCategory({ categoryType: query.type, name: form.name.trim(), sortOrder: form.sortOrder })
      ElMessage.success('分类已创建，发布后前台可见')
    } else {
      await updateCategory(form.id, {
        expectedLockVersion: form.lockVersion,
        name: form.name.trim(),
        sortOrder: form.sortOrder,
      })
      ElMessage.success('分类草稿已保存')
    }
    dialogVisible.value = false
    await load()
  } catch (error) {
    ElMessage.error(error instanceof ApiError ? error.message : '保存失败')
  } finally {
    saving.value = false
  }
}

async function publish(row: CategoryView) {
  try {
    await ElMessageBox.confirm(`发布分类「${row.name}」后，前台筛选会立即出现该分类。`, '确认发布', {
      confirmButtonText: '发布',
      cancelButtonText: '取消',
    })
    await publishCategory(row.id, row.lockVersion, row.draftVersion)
    ElMessage.success('已发布')
    await load()
  } catch (error) {
    if (error instanceof ApiError) ElMessage.error(error.message)
  }
}

async function offline(row: CategoryView) {
  try {
    await ElMessageBox.confirm('该分类下仍有已发布内容时无法下线。', '确认下线', {
      confirmButtonText: '下线',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await offlineCategory(row.id, row.lockVersion)
    ElMessage.success('已下线')
    await load()
  } catch (error) {
    if (error instanceof ApiError) ElMessage.error(error.message)
  }
}

async function remove(row: CategoryView) {
  try {
    await ElMessageBox.confirm('删除后不可恢复；仍被内容引用时会被拒绝。', '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteCategory(row.id, row.lockVersion)
    ElMessage.success('已删除')
    await load()
  } catch (error) {
    if (error instanceof ApiError) ElMessage.error(error.message)
  }
}

onMounted(load)
</script>

<template>
  <div class="page">
    <el-card shadow="never">
      <template #header>
        <div class="head">
          <span>内容分类</span>
          <div class="actions">
            <el-select v-model="query.type" style="width: 140px" @change="query.page = 1; load()">
              <el-option v-for="item in types" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
            <el-button @click="load">刷新</el-button>
            <el-button type="primary" @click="openCreate">新建分类</el-button>
          </div>
        </div>
      </template>

      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="分类是单层的，发布后才会出现在前台筛选里。"
        class="tip"
      />

      <el-table v-loading="loading" :data="rows" border>
        <el-table-column prop="name" label="名称" min-width="180">
          <template #default="{ row }">
            <div class="name-cell">
              <span>{{ row.name }}</span>
              <el-tag v-if="row.hasUnpublishedChanges" size="small" type="warning">有未发布修改</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 'PUBLISHED' ? 'success' : row.status === 'OFFLINE' ? 'warning' : 'info'">
              {{ row.status === 'PUBLISHED' ? '已发布' : row.status === 'OFFLINE' ? '已下线' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="线上名称" width="150">
          <template #default="{ row }">{{ row.publishedName || '—' }}</template>
        </el-table-column>
        <el-table-column label="排序" width="90">
          <template #default="{ row }">{{ row.publishedSortOrder ?? row.sortOrder }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button v-if="row.hasUnpublishedChanges" link type="success" @click="publish(row)">发布</el-button>
            <el-button v-if="row.status === 'PUBLISHED'" link type="warning" @click="offline(row)">下线</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        class="pager"
        layout="total, prev, pager, next"
        :total="total"
        :page-size="query.pageSize"
        :current-page="query.page"
        @current-change="(p: number) => { query.page = p; load() }"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新建分类' : '编辑分类'" width="420px">
      <el-form label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" maxlength="30" show-word-limit />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="9999" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.actions {
  display: flex;
  gap: 8px;
}

.tip {
  margin-bottom: 14px;
}

.name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pager {
  margin-top: 14px;
  justify-content: flex-end;
}
</style>
