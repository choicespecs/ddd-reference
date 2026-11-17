// Mirrors the backend read models (ProductView, OrderView). In a larger project generate these
// from the OpenAPI spec instead of hand-writing them:
//   npx openapi-typescript http://localhost:8080/v3/api-docs -o src/api/schema.d.ts

export interface Product {
  id: string
  sku: string
  name: string
  description: string | null
  price: number
  currency: string
  stockOnHand: number
  active: boolean
}

export type OrderStatus = 'DRAFT' | 'PLACED' | 'SHIPPED' | 'CANCELLED'

export interface OrderLine {
  productId: string
  productName: string
  unitPrice: number
  quantity: number
  lineTotal: number
}

export interface Order {
  id: string
  customerEmail: string
  status: OrderStatus
  lines: OrderLine[]
  total: number
  currency: string
  createdAt: string
  placedAt: string | null
  version: number
}
