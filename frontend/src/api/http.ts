/**
 * Tiny fetch wrapper. Every backend error is an RFC 9457 ProblemDetail
 * ({ type, title, status, detail }); we turn it into a typed ApiError so views can show
 * `error.detail` — which is the domain's own message, e.g. "Cannot place an empty order".
 */
export interface ProblemDetail {
  type?: string
  title: string
  status: number
  detail?: string
  instance?: string
}

export class ApiError extends Error {
  constructor(public readonly problem: ProblemDetail) {
    super(problem.detail ?? problem.title)
  }
}

export async function http<T>(path: string, init: RequestInit = {}): Promise<T> {
  const res = await fetch(path, {
    ...init,
    headers: { 'Content-Type': 'application/json', Accept: 'application/json', ...init.headers },
  })
  if (!res.ok) {
    let problem: ProblemDetail
    try {
      problem = await res.json()
    } catch {
      problem = { title: res.statusText, status: res.status }
    }
    throw new ApiError(problem)
  }
  return res.status === 204 ? (undefined as T) : ((await res.json()) as T)
}

export const get = <T>(path: string) => http<T>(path)
export const post = <T>(path: string, body?: unknown) =>
  http<T>(path, { method: 'POST', body: body === undefined ? undefined : JSON.stringify(body) })
export const put = <T>(path: string, body: unknown) =>
  http<T>(path, { method: 'PUT', body: JSON.stringify(body) })
export const del = <T>(path: string) => http<T>(path, { method: 'DELETE' })
