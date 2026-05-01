import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// Helper: only proxy XHR/fetch API calls, not browser HTML navigations
function apiOnly(req: { headers: Record<string, string | string[] | undefined> }) {
  const accept = req.headers['accept'] ?? '';
  if (typeof accept === 'string' && accept.includes('text/html')) {
    return '/index.html';
  }
  return undefined;
}

export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
  ],
  server: {
    port: 3000,
    proxy: {
      '/auth': { 
        target: 'http://localhost:8080', 
        changeOrigin: true, 
        bypass: apiOnly,
        secure: false,
        ws: true
      },
      '/applications': { 
        target: 'http://localhost:8080', 
        changeOrigin: true, 
        bypass: apiOnly,
        secure: false,
        ws: true
      },
      '/documents': { 
        target: 'http://localhost:8080', 
        changeOrigin: true, 
        bypass: apiOnly,
        secure: false,
        ws: true
      },
      '/dashboard': { 
        target: 'http://localhost:8080', 
        changeOrigin: true, 
        bypass: apiOnly,
        secure: false,
        ws: true
      },
      '/admin': { 
        target: 'http://localhost:8080', 
        changeOrigin: true, 
        bypass: apiOnly,
        secure: false,
        ws: true
      },
    },
  },
})
