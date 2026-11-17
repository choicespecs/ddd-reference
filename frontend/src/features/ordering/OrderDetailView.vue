<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { catalogApi } from '../../api/catalog'
import { orderingApi } from '../../api/ordering'
import type { Order, Product } from '../../api/types'
import { useAsync } from '../../composables/useAsync'
import { dateTime, money } from '../../format'
import StatusBadge from './StatusBadge.vue'

const props = defineProps<{ id: string }>()

const order = ref<Order | null>(null)
const products = ref<Product[]>([])
const selectedProduct = ref('')
const quantity = ref(1)

const isDraft = computed(() => order.value?.status === 'DRAFT')

onMounted(async () => {
  ;[order.value, products.value] = await Promise.all([orderingApi.get(props.id), catalogApi.list()])
  selectedProduct.value = products.value[0]?.id ?? ''
})

// Every command returns the updated OrderView, so the UI simply replaces its copy.
// No client-side recomputation of totals or status — the aggregate is the source of truth.
const { loading, error, run: exec } = useAsync(async (command: () => Promise<Order>) => {
  order.value = await command()
})

const addLine = () => exec(() => orderingApi.addLine(props.id, selectedProduct.value, quantity.value))
const setQty = (productId: string, q: number) => exec(() => orderingApi.changeQuantity(props.id, productId, q))
const remove = (productId: string) => exec(() => orderingApi.removeLine(props.id, productId))
const place = () => exec(() => orderingApi.place(props.id))
const ship = () => exec(() => orderingApi.ship(props.id))
const cancel = () => exec(() => orderingApi.cancel(props.id))
</script>

<template>
  <section v-if="order">
    <div class="section-head">
      <h1>Order <code class="muted">{{ order.id.slice(0, 8) }}</code></h1>
      <StatusBadge :status="order.status" />
    </div>
    <p class="muted">
      {{ order.customerEmail }} · created {{ dateTime(order.createdAt) }}
      <template v-if="order.placedAt"> · placed {{ dateTime(order.placedAt) }}</template>
      · version {{ order.version }}
    </p>

    <p v-if="error" class="error">{{ error }}</p>

    <table class="grid">
      <thead>
        <tr><th>Product</th><th class="num">Unit price</th><th class="num">Qty</th><th class="num">Line total</th><th></th></tr>
      </thead>
      <tbody>
        <tr v-for="l in order.lines" :key="l.productId">
          <td>{{ l.productName }}</td>
          <td class="num">{{ money(l.unitPrice, order.currency) }}</td>
          <td class="num">
            <input v-if="isDraft" class="qty" type="number" min="1" max="99" :value="l.quantity"
                   @change="setQty(l.productId, Number(($event.target as HTMLInputElement).value))" />
            <span v-else>{{ l.quantity }}</span>
          </td>
          <td class="num">{{ money(l.lineTotal, order.currency) }}</td>
          <td class="actions"><button v-if="isDraft" class="secondary" @click="remove(l.productId)">Remove</button></td>
        </tr>
        <tr v-if="order.lines.length === 0"><td colspan="5" class="muted">No lines yet.</td></tr>
      </tbody>
      <tfoot>
        <tr><th colspan="3" class="num">Total</th><th class="num">{{ money(order.total, order.currency) }}</th><th></th></tr>
      </tfoot>
    </table>

    <form v-if="isDraft" class="inline card" @submit.prevent="addLine">
      <select v-model="selectedProduct">
        <option v-for="p in products" :key="p.id" :value="p.id">
          {{ p.name }} — {{ money(p.price, p.currency) }} ({{ p.stockOnHand }} in stock)
        </option>
      </select>
      <input v-model.number="quantity" class="qty" type="number" min="1" max="99" />
      <button type="submit" :disabled="loading">Add line</button>
    </form>

    <!-- Buttons are hints; the domain decides. Try "Place" on an empty order to see the rule fire. -->
    <div class="toolbar">
      <button v-if="order.status === 'DRAFT'" :disabled="loading" @click="place">Place order</button>
      <button v-if="order.status === 'PLACED'" :disabled="loading" @click="ship">Mark shipped</button>
      <button v-if="order.status === 'DRAFT' || order.status === 'PLACED'" class="danger" :disabled="loading" @click="cancel">Cancel</button>
      <RouterLink to="/orders">← All orders</RouterLink>
    </div>
  </section>
  <p v-else>Loading…</p>
</template>
