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

async function handle<T>(response: Response): Promise<T> {
  const text = await response.text()
  let body: any = null
  try {
    body = text ? JSON.parse(text) : null
  } catch {
    throw new ApiError(response.status, 'BAD_RESPONSE', '服务返回了无法解析的内容')
  }

  if (!response.ok || body?.code !== 'OK') {
    throw new ApiError(
      response.status,
      body?.code ?? 'UNKNOWN',
      body?.message ?? `请求失败（${response.status}）`,
      body?.requestId,
      body?.fieldErrors,
    )
  }

  return body.data as T
}

export async function apiGet<T>(path: string): Promise<T> {
  const response = await fetch(`${BASE}${path}`, {
    method: 'GET',
    credentials: 'include',
    headers: { Accept: 'application/json' },
    cache: 'no-store',
  })
  return handle<T>(response)
}

export async function apiPost<T>(path: string, payload: unknown, headers: Record<string, string> = {}): Promise<T> {
  const response = await fetch(`${BASE}${path}`, {
    method: 'POST',
    credentials: 'include',
    headers: { Accept: 'application/json', 'Content-Type': 'application/json', ...headers },
    body: JSON.stringify(payload),
    cache: 'no-store',
  })
  return handle<T>(response)
}
