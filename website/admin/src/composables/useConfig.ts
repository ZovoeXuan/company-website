import { ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ApiError } from '@/api/http'
import { getConfig, publishConfig, saveConfigDraft, type ConfigKey, type ConfigView } from '@/api/site'

/**
 * 配置页的通用状态：加载 / 保存草稿 / 发布。
 * 草稿保存采用整份覆盖，锁版本不匹配时提示刷新。
 */
export function useConfig<T extends Record<string, unknown>>(key: ConfigKey) {
  const view = ref<ConfigView<T> | null>(null)
  const loading = ref(false)
  const saving = ref(false)
  const publishing = ref(false)
  const loadError = ref('')

  function report(error: unknown) {
    if (error instanceof ApiError) {
      const fields = error.fieldErrors?.map((item) => `${item.field}：${item.message}`).join('；')
      ElMessage.error(fields ? `${error.message}（${fields}）` : error.message)
    } else {
      ElMessage.error('操作失败，请重试')
    }
  }

  async function load() {
    loading.value = true
    loadError.value = ''
    try {
      view.value = await getConfig<T>(key)
      return view.value
    } catch (error) {
      loadError.value = error instanceof ApiError ? error.message : '配置加载失败'
      return null
    } finally {
      loading.value = false
    }
  }

  async function save(draft: T): Promise<boolean> {
    if (!view.value) return false
    saving.value = true
    try {
      view.value = await saveConfigDraft<T>(key, draft, view.value.lockVersion)
      ElMessage.success('草稿已保存，发布后前台生效')
      return true
    } catch (error) {
      report(error)
      return false
    } finally {
      saving.value = false
    }
  }

  async function publish(): Promise<boolean> {
    if (!view.value) return false
    try {
      await ElMessageBox.confirm('发布后前台立即生效，确认发布？', '确认发布', {
        confirmButtonText: '发布',
        cancelButtonText: '取消',
      })
    } catch {
      return false
    }
    publishing.value = true
    try {
      await publishConfig(key, view.value.lockVersion, view.value.draftVersion)
      ElMessage.success('已发布')
      await load()
      return true
    } catch (error) {
      report(error)
      return false
    } finally {
      publishing.value = false
    }
  }

  return { view, loading, saving, publishing, loadError, load, save, publish }
}
