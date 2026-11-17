import { get, post, put } from './http'
import type { Product } from './types'

export interface NewProduct {
  sku: string
  name: string
  description?: string
  price: number
  currency: string
  initialStock: number
}

// One module per bounded context; function names follow the backend's use cases.
export const catalogApi = {
  list: () => get<Product[]>('/api/products'),
  get: (id: string) => get<Product>(`/api/products/${id}`),
  create: (p: NewProduct) => post<Product>('/api/products', p),
  changePrice: (id: string, price: number, currency: string) =>
    put<Product>(`/api/products/${id}/price`, { price, currency }),
  receiveStock: (id: string, quantity: number) => post<Product>(`/api/products/${id}/stock`, { quantity }),
}
