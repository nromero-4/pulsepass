import { createContext, useContext } from 'react'
import { z } from 'zod'
import type { Identidad, Rol } from '../../lib/contratos'
import type { Login } from './formularios'

export const ALMACEN_SESION = 'pulsepass.sesion'
const credencialLocal = z.object({ token: z.string().min(1), expiraEn: z.int().positive() })
export type CredencialLocal = z.infer<typeof credencialLocal>

export type Sesion =
  | { estado: 'cargando' }
  | { estado: 'anonimo'; motivo?: 'expirada' }
  | { estado: 'error'; error: unknown }
  | { estado: 'autenticado'; credencial: CredencialLocal; usuario: Identidad }

interface Auth {
  sesion: Sesion
  iniciarSesion: (data: Login) => Promise<void>
  cerrarSesion: () => void
  reconocerExpiracion: () => void
  reintentar: () => void
  tieneRol: (...roles: Rol[]) => boolean
}

export const AuthContext = createContext<Auth | null>(null)

export function useSesion() {
  const auth = useContext(AuthContext)
  if (!auth) throw new Error('La sesión requiere AuthProvider')
  return auth
}

export function leerCredencial() {
  try {
    const raw = sessionStorage.getItem(ALMACEN_SESION)
    const data: unknown = raw ? JSON.parse(raw) : undefined
    const result = credencialLocal.safeParse(data)
    return result.success ? result.data : undefined
  } catch {
    return undefined
  }
}

export function guardarCredencial(credencial?: CredencialLocal) {
  try {
    if (credencial) sessionStorage.setItem(ALMACEN_SESION, JSON.stringify(credencial))
    else sessionStorage.removeItem(ALMACEN_SESION)
  } catch {
    // La sesión en memoria sigue disponible cuando el navegador bloquea el almacenamiento.
  }
}
