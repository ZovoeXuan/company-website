import { ref } from 'vue'
import { clearCsrf, get, request } from '@/api/http'

export interface CurrentUser {
  id: string
  username: string
  displayName: string
  role: 'ADMIN' | 'EDITOR'
}

const current = ref<CurrentUser | null>(null)
const loaded = ref(false)

async function fetchMe(): Promise<CurrentUser | null> {
  try {
    current.value = await get<CurrentUser>('/api/admin/auth/me')
  } catch {
    current.value = null
  } finally {
    loaded.value = true
  }
  return current.value
}

async function login(username: string, password: string): Promise<CurrentUser> {
  const user = await request<CurrentUser>('/api/admin/auth/login', {
    method: 'POST',
    form: { username, password },
  })
  clearCsrf()
  current.value = user
  loaded.value = true
  return user
}

async function logout(): Promise<void> {
  await request('/api/admin/auth/logout', { method: 'POST' })
  clearCsrf()
  current.value = null
  loaded.value = true
}

export function useAuth() {
  return { current, loaded, fetchMe, login, logout }
}
