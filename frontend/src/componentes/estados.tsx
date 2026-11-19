import { AlertCircle, Inbox, LoaderCircle } from 'lucide-react'
import type { ReactNode } from 'react'
import { Navigate } from 'react-router'
import { ApiError, mensajeError } from '../lib/http'
import { RUTAS } from '../lib/rutas'
import { Boton, EnlaceBoton } from './ui'

export function Cargando({ mensaje = 'Cargando…' }: { mensaje?: string }) {
  return (
    <div
      role="status"
      className="superficie secundario flex min-h-28 items-center justify-center gap-3 p-5"
    >
      <LoaderCircle
        aria-hidden="true"
        className="size-5 text-marca-600 motion-safe:animate-spin dark:text-indigo-300"
      />
      {mensaje}
    </div>
  )
}

export function Vacio({
  titulo,
  descripcion,
  children,
}: {
  titulo: string
  descripcion?: string
  children?: ReactNode
}) {
  return (
    <section className="superficie flex min-h-48 flex-col items-center justify-center gap-2 p-6 text-center">
      <Inbox aria-hidden="true" className="mb-1 size-8 text-marca-600 dark:text-indigo-300" />
      <h2 className="text-lg font-semibold">{titulo}</h2>
      {descripcion && <p className="secundario">{descripcion}</p>}
      {children && <div className="mt-2">{children}</div>}
    </section>
  )
}

export function Alerta({ children, exito = false }: { children: ReactNode; exito?: boolean }) {
  return exito ? (
    <p
      role="status"
      className="rounded-lg bg-emerald-50 p-3 text-sm font-medium text-emerald-900 dark:bg-emerald-950 dark:text-emerald-200"
    >
      {children}
    </p>
  ) : (
    <p
      role="alert"
      className="flex gap-2 rounded-lg bg-red-50 p-3 text-sm font-medium text-red-900 dark:bg-red-950 dark:text-red-200"
    >
      <AlertCircle aria-hidden="true" className="size-5 shrink-0" />
      <span>{children}</span>
    </p>
  )
}

interface ErrorProps {
  error: unknown
  reintentar?: () => void
  cancelar?: () => void
  noDisponible?: string
  prohibido?: 'redirigir' | 'ocultar'
}
export function EstadoError({ error, reintentar, cancelar, noDisponible, prohibido }: ErrorProps) {
  const status = error instanceof ApiError ? error.status : undefined
  if (status === 403 && prohibido === 'redirigir') return <Navigate to={RUTAS.prohibido} replace />
  if (noDisponible && (status === 404 || (status === 403 && prohibido === 'ocultar'))) {
    return (
      <Vacio titulo={noDisponible}>
        <EnlaceBoton to={RUTAS.inicio} variante="secundario">
          Volver al inicio
        </EnlaceBoton>
      </Vacio>
    )
  }
  return (
    <div className="space-y-3">
      <Alerta>{mensajeError(error)}</Alerta>
      {reintentar && (status === undefined || status === 0 || status >= 500) && (
        <Boton variante="secundario" onClick={reintentar}>
          Reintentar
        </Boton>
      )}
      {cancelar && (
        <Boton variante="fantasma" onClick={cancelar}>
          Cerrar sesión
        </Boton>
      )}
    </div>
  )
}

interface ConsultaProps<T> {
  consulta: { status: 'pending' | 'error' | 'success'; data?: T; error: unknown; refetch: () => unknown }
  children: (data: T) => ReactNode
  cargando?: ReactNode
  noDisponible?: string
  prohibido?: ErrorProps['prohibido']
}
export function Consulta<T>({
  consulta,
  children,
  cargando,
  noDisponible,
  prohibido,
}: ConsultaProps<T>) {
  if (consulta.status === 'pending') return cargando ?? <Cargando />
  if (consulta.status === 'error')
    return (
      <EstadoError
        error={consulta.error}
        reintentar={consulta.refetch}
        noDisponible={noDisponible}
        prohibido={prohibido}
      />
    )
  return consulta.data === undefined ? null : children(consulta.data)
}

export function ErrorPagina() {
  return (
    <section className="superficie mx-auto max-w-xl space-y-4 p-5 sm:p-7" role="alert">
      <h1 className="text-2xl font-bold">¡Ups! Algo salió mal.</h1>
      <p className="secundario">{mensajeError(undefined)}</p>
      <div className="flex flex-wrap gap-2">
        <Boton onClick={() => window.location.reload()}>Reintentar</Boton>
        <EnlaceBoton variante="secundario" to={RUTAS.inicio} replace>
          Volver al inicio
        </EnlaceBoton>
      </div>
    </section>
  )
}
