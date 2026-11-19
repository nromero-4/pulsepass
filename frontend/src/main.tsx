import '@fontsource-variable/inter'
import '@fontsource/poppins/600.css'
import '@fontsource/poppins/700.css'
import { QueryClientProvider } from '@tanstack/react-query'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { RouterProvider } from 'react-router/dom'
import { AvisosProvider } from './componentes/AvisosProvider'
import { AuthProvider } from './features/auth/AuthProvider'
import { crearQueryClient } from './lib/queryClient'
import { crearRouter } from './router'
import './index.css'

const root = document.getElementById('root')
if (!root) throw new Error('Falta el contenedor de PulsePass.')

createRoot(root).render(
  <StrictMode>
    <QueryClientProvider client={crearQueryClient()}>
      <AuthProvider>
        <AvisosProvider>
          <RouterProvider router={crearRouter()} />
        </AvisosProvider>
      </AuthProvider>
    </QueryClientProvider>
  </StrictMode>,
)
