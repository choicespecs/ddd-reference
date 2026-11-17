<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { catalogApi } from '../../api/catalog'
import { orderingApi } from '../../api/ordering'
import { useAsync } from '../../composables/useAsync'
import { money } from '../../format'
import NewProductForm from './NewProductForm.vue'

// Data flow: DB → JPA → Product aggregate → ProductView (JSON) → this ref → template.
const { data: products, loading, error, run: load } = useAsync(() => catalogApi.list())
onMounted(load)

const showForm = ref(false)
const router = useRouter()
const actionError = ref<string | null>(null)

async function receive(id: string) {
  actionError.value = null
  try {
    await catalogApi.receiveStock(id, 10)
    await load()
  } catch (e) {
    actionError.value = (e as Error).message
  }
}

// "Buy now": start a draft order with one line, then jump to it.
async function quickOrder(productId: string) {
  actionError.value = null
  try {
    const order = await orderingApi.start('demo@example.com')
    await orderingApi.addLine(order.id, productId, 1)
    router.push(`/orders/${order.id}`)
  } catch (e) {
    actionError.value = (e as Error).message
  }
}
</script>

<template>
  <section>
    <div class="section-head">
      <h1>Catalog</h1>
      <button @click="showForm = !showForm">{{ showForm ? 'Close' : 'New product' }}</button>
    </div>

    <NewProductForm v-if="showForm" @created="showForm = false; load()" />

    <p v-if="loading">Loading…</p>
    <p v-if="error || actionError" class="error">{{ error || actionError }}</p>

    <table v-if="products" class="grid">
      <thead>
        <tr><th>SKU</th><th>Name</th><th class="num">Price</th><th class="num">Stock</th><th></th></tr>
      </thead>
      <tbody>
        <tr v-for="p in products" :key="p.id">
          <td><code>{{ p.sku }}</code></td>
          <td>
            <div>{{ p.name }}</div>
            <small class="muted">{{ p.description }}</small>
          </td>
          <td class="num">{{ money(p.price, p.currency) }}</td>
          <td class="num" :class="{ low: p.stockOnHand < 5 }">{{ p.stockOnHand }}</td>
          <td class="actions">
            <button class="secondary" @click="receive(p.id)">+10 stock</button>
            <button :disabled="p.stockOnHand === 0" @click="quickOrder(p.id)">Order</button>
          </td>
        </tr>
      </tbody>
    </table>
  </section>
</template>
