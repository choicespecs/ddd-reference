import { del, get, post, put } from './http'
import type { Order } from './types'

// Commands are explicit verbs (place, cancel, ship) — the UI never PATCHes a status field.
export const orderingApi = {
  recent: () => get<Order[]>('/api/orders'),
  get: (id: string) => get<Order>(`/api/orders/${id}`),
  start: (customerEmail: string) => post<Order>('/api/orders', { customerEmail }),
  addLine: (id: string, productId: string, quantity: number) =>
    post<Order>(`/api/orders/${id}/lines`, { productId, quantity }),
  changeQuantity: (id: string, productId: string, quantity: number) =>
    put<Order>(`/api/orders/${id}/lines/${productId}`, { quantity }),
  removeLine: (id: string, productId: string) => del<Order>(`/api/orders/${id}/lines/${productId}`),
  place: (id: string) => post<Order>(`/api/orders/${id}/place`),
  ship: (id: string) => post<Order>(`/api/orders/${id}/ship`),
  cancel: (id: string) => post<Order>(`/api/orders/${id}/cancel`),
}
