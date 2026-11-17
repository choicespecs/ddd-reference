<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { orderingApi } from '../../api/ordering'
import { useAsync } from '../../composables/useAsync'
import { dateTime, money } from '../../format'
import StatusBadge from './StatusBadge.vue'

const { data: orders, loading, error, run: load } = useAsync(() => orderingApi.recent())
onMounted(load)

const email = ref('ada@example.com')
const router = useRouter()
const { error: startError, run: start } = useAsync(async () => {
  const order = await orderingApi.start(email.value)
  router.push(`/orders/${order.id}`)
})
</script>

<template>
  <section>
    <div class="section-head">
      <h1>Orders</h1>
      <form class="inline" @submit.prevent="start()">
        <input v-model="email" type="email" placeholder="customer@example.com" required />
        <button type="submit">Start order</button>
      </form>
    </div>
    <p v-if="startError" class="error">{{ startError }}</p>
    <p v-if="loading">Loading…</p>
    <p v-if="error" class="error">{{ error }}</p>

    <p v-if="orders && orders.length === 0" class="muted">No orders yet. Start one above or click “Order” in the catalog.</p>

    <table v-if="orders && orders.length" class="grid">
      <thead>
        <tr><th>Created</th><th>Customer</th><th>Status</th><th class="num">Lines</th><th class="num">Total</th></tr>
      </thead>
      <tbody>
        <tr v-for="o in orders" :key="o.id" class="clickable" @click="router.push(`/orders/${o.id}`)">
          <td>{{ dateTime(o.createdAt) }}</td>
          <td>{{ o.customerEmail }}</td>
          <td><StatusBadge :status="o.status" /></td>
          <td class="num">{{ o.lines.length }}</td>
          <td class="num">{{ money(o.total, o.currency) }}</td>
        </tr>
      </tbody>
    </table>
  </section>
</template>
