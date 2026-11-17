import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// In dev, the browser talks only to Vite (http://localhost:5173). Requests to /api are
// proxied to Spring Boot on :8080, so there is no CORS and the frontend code can use
// relative URLs ("/api/products") that also work when Spring Boot serves the built app.
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
  build: {
    outDir: 'dist',
  },
})
