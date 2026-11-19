import { createBrowserRouter } from 'react-router'
import type { RouteObject } from 'react-router'
import { Cargando, ErrorPagina } from './componentes/estados'
import { Layout } from './componentes/Layout'
import { RutaProtegida, SoloAnonimos } from './features/auth/guardas'
import { RUTAS } from './lib/rutas'
import { Inicio } from './pages/Inicio'

export const rutas: RouteObject[] = [
  {
    path: RUTAS.inicio,
    Component: Layout,
    hydrateFallbackElement: (
      <div className="contenedor py-8">
        <Cargando />
      </div>
    ),
    children: [
      {
        ErrorBoundary: ErrorPagina,
        children: [
          { index: true, Component: Inicio },
          { path: RUTAS.eventos, lazy: () => import('./pages/CatalogoEventos') },
          { path: RUTAS.patronEvento, lazy: () => import('./pages/DetalleEvento') },
          { path: RUTAS.artistas, lazy: () => import('./pages/ListaArtistas') },
          { path: RUTAS.patronArtista, lazy: () => import('./pages/DetalleArtista') },
          { path: RUTAS.sedes, lazy: () => import('./pages/ListaSedes') },
          { path: RUTAS.patronSede, lazy: () => import('./pages/DetalleSede') },
          {
            Component: SoloAnonimos,
            children: [
              { path: RUTAS.login, lazy: () => import('./pages/Login') },
              { path: RUTAS.registro, lazy: () => import('./pages/Registro') },
            ],
          },
          {
            element: <RutaProtegida />,
            children: [{ path: RUTAS.cuenta, lazy: () => import('./pages/Cuenta') }],
          },
          {
            element: <RutaProtegida roles={['USER']} />,
            children: [
              { path: RUTAS.tickets, lazy: () => import('./pages/MisTickets') },
              { path: RUTAS.patronTicket, lazy: () => import('./pages/DetalleTicket') },
              { path: RUTAS.patronConfirmacion, lazy: () => import('./pages/ConfirmacionCompra') },
            ],
          },
          {
            element: <RutaProtegida roles={['STAFF', 'ADMIN']} />,
            children: [
              { path: RUTAS.validar, lazy: () => import('./pages/ValidacionIngreso') },
              { path: RUTAS.pagados, lazy: () => import('./pages/TicketsPagadosEvento') },
              { path: RUTAS.patronPagados, lazy: () => import('./pages/TicketsPagadosEvento') },
            ],
          },
          {
            element: <RutaProtegida roles={['ADMIN']} />,
            children: [
              { path: RUTAS.admin, lazy: () => import('./pages/AdminEventos') },
              { path: RUTAS.nuevoEvento, lazy: () => import('./pages/AdminCrearEvento') },
              { path: RUTAS.patronGestion, lazy: () => import('./pages/AdminGestionEvento') },
            ],
          },
          { path: RUTAS.prohibido, lazy: async () => ({ Component: await paginaEstado(403) }) },
          { path: '*', lazy: async () => ({ Component: await paginaEstado(404) }) },
        ],
      },
    ],
  },
]

async function paginaEstado(status: 403 | 404) {
  const { PaginaEstado } = await import('./pages/PaginaEstado')
  return function Estado() {
    return <PaginaEstado status={status} />
  }
}

export const crearRouter = () => createBrowserRouter(rutas)
