const BASE = import.meta.env.VITE_API_BASE ?? ''

export interface FieldErrorItem {
  field: string
  message: string
}

export class ApiError extends Error {
  status: number
  code: string
  requestId?: string
  fieldErrors?: FieldErrorItem[]

  constructor(status: number, code: string, message: string, requestId?: string, fieldErrors?: FieldErrorItem[]) {
    super(message)
    this.status = status
    this.code = code
    this.requestId = requestId
    this.fieldErrors = fieldErrors
  }
}

let csrfToken: string | null = null

export async function ensureCsrf(): Promise<string> {
  if (csrfToken) return csrfToken
  const response = await fetch(`${BASE}/api/admin/auth/csrf`, {
    credentials: 'include',
    cache: 'no-store',
    headers: { Accept: 'application/json' },
  })
  const body = await response.json().catch(() => null)
  if (!response.ok || body?.code !== 'OK') {
    throw new ApiError(response.status, body?.code ?? 'CSRF_UNAVAILABLE', '无法获取请求令牌')
  }
  csrfToken = body.data?.token ?? null
  if (!csrfToken) throw new ApiError(500, 'CSRF_UNAVAILABLE', '请求令牌为空')
  return csrfToken
}

export function clearCsrf() {
  csrfToken = null
}

interface RequestOptions {
  method?: 'GET' | 'POST' | 'PUT' | 'DELETE'
  body?: unknown
  form?: Record<string, string>
}

export async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const method = options.method ?? 'GET'
  const headers: Record<string, string> = { Accept: 'application/json' }
  let payload: BodyInit | undefined

  if (options.form) {
    headers['Content-Type'] = 'application/x-www-form-urlencoded'
    payload = new URLSearchParams(options.form).toString()
  } else if (options.body !== undefined) {
    headers['Content-Type'] = 'application/json'
    payload = typeof options.body === 'string' ? options.body : JSON.stringify(options.body)
  }

  if (method !== 'GET') {
    headers['X-CSRF-TOKEN'] = await ensureCsrf()
  }

  const response = await fetch(`${BASE}${path}`, {
    method,
    headers,
    body: payload,
    credentials: 'include',
    cache: 'no-store',
  })

  if (response.status === 401 || response.status === 403) {
    clearCsrf()
  }

  return handle<T>(response, path)
}

/** multipart 上传：浏览器自动设置 boundary，不能手动指定 Content-Type。 */
export async function upload<T>(path: string, formData: FormData): Promise<T> {
  const response = await fetch(`${BASE}${path}`, {
    method: 'POST',
    headers: { Accept: 'application/json', 'X-CSRF-TOKEN': await ensureCsrf() },
    body: formData,
    credentials: 'include',
    cache: 'no-store',
  })
  if (response.status === 401 || response.status === 403) {
    clearCsrf()
  }
  return handle<T>(response, path)
}

async function handle<T>(response: Response, path = ''): Promise<T> {
  const text = await response.text()
  let body: any = null
  try {
    body = text ? JSON.parse(text) : null
  } catch {
    throw new ApiError(response.status, 'BAD_RESPONSE', '服务返回了无法解析的内容')
  }

  if (!response.ok || body?.code !== 'OK') {
    if (
      response.status === 401 &&
      typeof window !== 'undefined' &&
      !path.includes('/api/admin/auth/me') &&
      !path.includes('/api/admin/auth/login') &&
      !window.location.pathname.endsWith('/login')
    ) {
      const currentPath = window.location.pathname.replace(/^\/admin/, '') + window.location.search
      window.location.href = `/admin/login?redirect=${encodeURIComponent(currentPath)}`
    }
    throw new ApiError(
      response.status,
      body?.code ?? 'UNKNOWN',
      body?.message ?? `请求失败 (${response.status})`,
      body?.requestId,
      body?.fieldErrors,
    )
  }

  return body.data as T
}

export function get<T>(path: string): Promise<T> {
  return request<T>(path, { method: 'GET' })
}

export function post<T>(path: string, body?: unknown): Promise<T> {
  return request<T>(path, { method: 'POST', body })
}

export function put<T>(path: string, body?: unknown): Promise<T> {
  return request<T>(path, { method: 'PUT', body })
}

export function del<T>(path: string): Promise<T> {
  return request<T>(path, { method: 'DELETE' })
}
