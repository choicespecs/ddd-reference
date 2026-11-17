import { ref, shallowRef } from 'vue'
import { ApiError } from '../api/http'

/**
 * Wraps an async call with `loading` / `error` state. Keeps views free of try/catch noise.
 *
 *   const { data, loading, error, run } = useAsync(() => catalogApi.list())
 *   onMounted(run)
 */
export function useAsync<T, A extends unknown[] = []>(fn: (...args: A) => Promise<T>) {
  const data = shallowRef<T | null>(null)
  const loading = ref(false)
  const error = ref<string | null>(null)

  async function run(...args: A): Promise<T | null> {
    loading.value = true
    error.value = null
    try {
      data.value = await fn(...args)
      return data.value
    } catch (e) {
      error.value = e instanceof ApiError ? e.message : String(e)
      return null
    } finally {
      loading.value = false
    }
  }

  return { data, loading, error, run }
}
