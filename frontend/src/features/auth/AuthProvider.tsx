import { useQueryClient } from '@tanstack/react-query'
import { useCallback, useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { ApiError, conectarSesion, observarExpiracion } from '../../lib/http'
import { apiAuth } from './api'
import { limpiarRecientes } from '../eventos/recientes'
import type { Login } from './formularios'
import { AuthContext, guardarCredencial, leerCredencial } from './sesion'
import type { Sesion } from './sesion'

interface Props {
  children: ReactNode
}

export function AuthProvider({ children }: Props) {
  const client = useQueryClient()
  const [sesion, setSesion] = useState<Sesion>(() => ({
    estado: leerCredencial() ? 'cargando' : 'anonimo',
  }))
  const [intento, setIntento] = useState(0)

  const finalizar = useCallback(
    (motivo?: 'expirada') => {
      conectarSesion(undefined)
      guardarCredencial()
      limpiarRecientes()
      client.clear()
      setSesion({ estado: 'anonimo', motivo })
    },
    [client],
  )

  const reconocerExpiracion = useCallback(() => {
    setSesion((actual) =>
      actual.estado === 'anonimo' && actual.motivo ? { estado: 'anonimo' } : actual,
    )
  }, [])

  useEffect(() => observarExpiracion(() => finalizar('expirada')), [finalizar])

  useEffect(() => {
    const credencial = leerCredencial()
    if (!credencial) {
      guardarCredencial()
      conectarSesion(undefined)
      setSesion({ estado: 'anonimo' })
      return
    }
    if (credencial.expiraEn <= Date.now()) {
      finalizar('expirada')
      return
    }

    const controller = new AbortController()
    setSesion({ estado: 'cargando' })
    apiAuth
      .me(credencial.token, controller.signal)
      .then((usuario) => {
        if (controller.signal.aborted) return
        if (credencial.expiraEn <= Date.now()) {
          finalizar('expirada')
          return
        }
        conectarSesion(credencial.token)
        setSesion({ estado: 'autenticado', credencial, usuario })
      })
      .catch((error: unknown) => {
        if (controller.signal.aborted) return
        if (error instanceof ApiError && error.status === 401) finalizar('expirada')
        else setSesion({ estado: 'error', error })
      })
    return () => controller.abort()
  }, [finalizar, intento])

  useEffect(() => {
    if (sesion.estado !== 'autenticado') return
    const timer = window.setTimeout(
      () => finalizar('expirada'),
      Math.max(0, sesion.credencial.expiraEn - Date.now()),
    )
    return () => window.clearTimeout(timer)
  }, [finalizar, sesion])

  async function iniciarSesion(data: Login) {
    const inicio = Date.now()
    const respuesta = await apiAuth.login(data)
    const credencial = { token: respuesta.accessToken, expiraEn: inicio + respuesta.expiresIn * 1000 }
    const usuario = await apiAuth.me(credencial.token)
    if (credencial.expiraEn <= Date.now()) throw new ApiError(401)
    conectarSesion(credencial.token)
    guardarCredencial(credencial)
    setSesion({ estado: 'autenticado', credencial, usuario })
  }

  return (
    <AuthContext.Provider
      value={{
        sesion,
        iniciarSesion,
        cerrarSesion: () => finalizar(),
        reconocerExpiracion,
        reintentar: () => setIntento((count) => count + 1),
        tieneRol: (...roles) =>
          sesion.estado === 'autenticado' && roles.some((role) => sesion.usuario.roles.includes(role)),
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}
