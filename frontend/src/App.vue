<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <header class="topbar">
    <strong class="brand">Shop</strong>
    <nav>
      <!-- One nav entry per bounded context -->
      <RouterLink to="/products">Catalog</RouterLink>
      <RouterLink to="/orders">Orders</RouterLink>
    </nav>
    <a class="api-link" href="http://localhost:8080/swagger-ui.html" target="_blank" rel="noopener">API docs ↗</a>
  </header>
  <main class="container">
    <RouterView />
  </main>
</template>
