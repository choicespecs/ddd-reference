import { createRouter, createWebHistory } from 'vue-router'

// Routes are grouped by bounded context, mirroring the backend packages.
// Views are lazy-loaded so each context becomes its own JS chunk.
export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/products' },
    { path: '/products', component: () => import('./features/catalog/ProductListView.vue') },
    { path: '/orders', component: () => import('./features/ordering/OrderListView.vue') },
    { path: '/orders/:id', component: () => import('./features/ordering/OrderDetailView.vue'), props: true },
  ],
})
