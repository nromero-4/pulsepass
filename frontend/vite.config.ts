import { defineConfig } from 'vitest/config'
import { loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), 'PULSEPASS_')
  const proxy = {
    // Conserva el Host de Vite para que Spring lo compare con el Origin del navegador.
    '/api': { target: env.PULSEPASS_API_TARGET || 'http://localhost:8080', changeOrigin: false },
  }
  return {
    plugins: [react(), tailwindcss()],
    server: { proxy },
    preview: { proxy },
    test: {
      environment: 'jsdom',
      maxWorkers: 3,
      testTimeout: 15_000,
      environmentOptions: { jsdom: { url: 'http://localhost:5173' } },
      setupFiles: ['./src/test/setup.ts'],
    },
  }
})
