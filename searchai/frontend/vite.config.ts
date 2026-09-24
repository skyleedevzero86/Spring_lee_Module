import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5178,
    proxy: {
      '/auth': 'http://localhost:8088',
      '/chat': 'http://localhost:8088',
      '/rag': 'http://localhost:8088',
      '/sse': 'http://localhost:8088',
      '/admin': 'http://localhost:8088',
    },
  },
})
