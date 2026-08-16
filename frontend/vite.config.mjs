import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

const backend = 'http://localhost:8080'

export default defineConfig({
  plugins: [vue()],
  resolve: { alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) } },
  server: {
    port: 8081,
    proxy: Object.fromEntries(
      ['/api', '/login', '/logout', '/users', '/captcha', '/images', '/packages']
        .map(path => [path, { target: backend, changeOrigin: true }])
    )
  }
})
