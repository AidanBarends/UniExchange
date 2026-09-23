import { fileURLToPath, URL } from 'node:url'

import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  // No dev proxy needed: the backend already allows http://localhost:5173 as a
  // CORS origin (app.cors.allowed-origins), so the browser talks to it directly.
  server: {
    port: 5173,
    /*
      Fail loudly if 5173 is taken rather than quietly moving to 5174.

      A silent move breaks two things at once: the backend only allows
      http://localhost:5173 and :3000 as CORS origins, so every API call fails
      with an opaque console error, and .vscode/launch.json opens Chrome at a
      hard-coded :5173. An "address already in use" message is far easier to act
      on than either of those.
    */
    strictPort: true,
  },
})
