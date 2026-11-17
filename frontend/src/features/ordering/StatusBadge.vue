<script setup lang="ts">
import type { OrderStatus } from '../../api/types'
defineProps<{ status: OrderStatus }>()
</script>

<template>
  <span class="badge" :class="status.toLowerCase()">{{ status }}</span>
</template>
