<script setup lang="ts">
import { reactive } from 'vue'
import { catalogApi, type NewProduct } from '../../api/catalog'
import { useAsync } from '../../composables/useAsync'

const emit = defineEmits<{ created: [] }>()

const form = reactive<NewProduct>({
  sku: '', name: '', description: '', price: 0, currency: 'USD', initialStock: 0,
})

// The UI does light validation for UX (required fields). The *rules* — SKU format, no
// negative stock — are enforced by the domain; their messages come back as ProblemDetail.
const { loading, error, run: submit } = useAsync(async () => {
  await catalogApi.create({ ...form, sku: form.sku.toUpperCase() })
  emit('created')
})
</script>

<template>
  <form class="card form" @submit.prevent="submit()">
    <label>SKU <input v-model="form.sku" placeholder="BOOK-0042" required /></label>
    <label>Name <input v-model="form.name" required /></label>
    <label class="wide">Description <input v-model="form.description" /></label>
    <label>Price <input v-model.number="form.price" type="number" step="0.01" min="0" required /></label>
    <label>Currency <input v-model="form.currency" maxlength="3" required /></label>
    <label>Initial stock <input v-model.number="form.initialStock" type="number" min="0" /></label>
    <div class="wide">
      <button type="submit" :disabled="loading">Create</button>
      <span v-if="error" class="error">{{ error }}</span>
    </div>
  </form>
</template>
