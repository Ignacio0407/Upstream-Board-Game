import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  build: {
    outDir: 'build',
  },
  define: {
    global: 'globalThis',
  },
  server: {
    port: 3000,
    host: true,
    proxy: {
      "/api": {
        target: "https://localhost:8080",
        changeOrigin: true,
        secure: false
      },
      "/ws-upstream": {
        target: "wss://localhost:8080",
        ws: true,
        secure: false
      }
    }
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/setupTests.ts',
    coverage: {
      exclude: [
        'node_modules/**',
        'build/**',
        'src/mocks/**',
        'src/__mocks__/**',
        '**/*.d.ts',
        '**/*.css',
        '**/*.svg',
        '**/*.png',
        '**/*.jpg',
        '**/*.jpeg',
        '**/*.gif',
        '**/*.pdf',
        'src/reportWebVitals.ts',
        'src/setupTests.ts',
        'src/index.tsx',
        'src/test-utils.tsx',
      ]
    }
  },
});