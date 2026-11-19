import { Navigate, Outlet, useLocation } from 'react-router'
import type { Rol } from '../../lib/contratos'
import { estadoLogin, retornoSeguro, RUTAS } from '../../lib/rutas'
import { Cargando, EstadoError } from '../../componentes/estados'
import { useSesion } from './sesion'

export function RutaProtegida({ roles }: { roles?: readonly Rol[] }) {
  const { sesion, tieneRol, reintentar } = useSesion()
  const location = useLocation()
  if (sesion.estado === 'cargando') return <Cargando mensaje="Comprobando tu sesión…" />
  if (sesion.estado === 'error') return <EstadoError error={sesion.error} reintentar={reintentar} />
  if (sesion.estado === 'anonimo') {
    const desde = location.pathname + location.search + location.hash
    return <Navigate to={RUTAS.login} replace state={{ desde, motivo: sesion.motivo }} />
  }
  if (roles && !tieneRol(...roles)) return <Navigate to={RUTAS.prohibido} replace />
  return <Outlet />
}

export function SoloAnonimos() {
  const { sesion, reintentar, cerrarSesion } = useSesion()
  const location = useLocation()
  if (sesion.estado === 'cargando') return <Cargando mensaje="Comprobando tu sesión…" />
  if (sesion.estado === 'error')
    return <EstadoError error={sesion.error} reintentar={reintentar} cancelar={cerrarSesion} />
  if (sesion.estado === 'autenticado') {
    const state = estadoLogin.safeParse(location.state)
    return <Navigate replace to={retornoSeguro(state.data?.desde)} />
  }
  return <Outlet />
}
