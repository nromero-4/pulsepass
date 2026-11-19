import { QueryClientProvider } from '@tanstack/react-query'
import { render } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { HttpResponse, http } from 'msw'
import { createMemoryRouter, parsePath } from 'react-router'
import type { RouteObject } from 'react-router'
import { RouterProvider } from 'react-router/dom'
import { AvisosProvider } from '../componentes/AvisosProvider'
import { AuthProvider } from '../features/auth/AuthProvider'
import { guardarCredencial } from '../features/auth/sesion'
import type { Rol } from '../lib/contratos'
import { crearQueryClient } from '../lib/queryClient'
import { rutas } from '../router'
import { base, identidadDemo, tokenDemo } from './fixtures'
import { server } from './setup'

export const FECHA_SESION = 1_795_137_600_000

interface Opciones {
  rol?: Rol
  router?: RouteObject[]
  expira?: number
  state?: object
}

export function app(pathname: string, options: Opciones = {}) {
  const { rol, expira = Date.now() + 900_000, state } = options
  if (rol) {
    guardarCredencial({ token: tokenDemo, expiraEn: expira })
    server.use(http.get(base + '/auth/me', () => HttpResponse.json(identidadDemo(rol))))
  }
  const router = createMemoryRouter(options.router ?? rutas, {
    initialEntries: [{ ...parsePath(pathname), state }],
  })
  const client = crearQueryClient()
  const usuario = userEvent.setup()
  const vista = render(
    <QueryClientProvider client={client}>
      <AuthProvider>
        <AvisosProvider>
          <RouterProvider router={router} />
        </AvisosProvider>
      </AuthProvider>
    </QueryClientProvider>,
  )
  return { ...vista, router, usuario, client }
}
