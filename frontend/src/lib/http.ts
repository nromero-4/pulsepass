import { z } from 'zod'

const respuestaError = z.object({
  timestamp: z.iso.datetime({ local: true }),
  status: z.number().int(),
  error: z.string(),
  message: z.string().min(1),
  details: z.record(z.string(), z.string()).nullish(),
})

const inesperado = 'Ocurrió un error inesperado. Inténtalo más tarde.'
const mensajes: Record<number, string> = {
  0: 'No pudimos conectar con PulsePass. Revisa tu conexión e inténtalo de nuevo.',
  400: 'Revisa los datos ingresados.',
  401: 'Tu sesión expiró. Vuelve a iniciar sesión.',
  403: 'No tienes permisos para acceder a este recurso.',
  404: 'No encontramos lo que buscas.',
  409: 'No pudimos completar la operación en el estado actual.',
  500: inesperado,
  502: 'La API de PulsePass no está disponible. Inténtalo más tarde.',
  503: 'La API de PulsePass no está disponible. Inténtalo más tarde.',
}

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    message = mensajes[status] ?? inesperado,
    public readonly details: Readonly<Record<string, string>> = {},
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

export function mensajeError(error: unknown) {
  return error instanceof ApiError ? error.message : inesperado
}

let token: string | undefined
let revision = 0
let alExpirar: (() => void) | undefined

export function conectarSesion(value: string | undefined) {
  if (token !== value) revision += 1
  token = value
}

export const revisionSesion = () => revision

export function observarExpiracion(listener: () => void) {
  alExpirar = listener
  return () => {
    if (alExpirar === listener) alExpirar = undefined
  }
}

interface Opciones {
  method?: 'GET' | 'POST' | 'PATCH'
  body?: object
  signal?: AbortSignal
  anonimo?: boolean
  tokenInicial?: string
}

export async function http<T>(path: string, schema: z.ZodType<T>, options: Opciones = {}): Promise<T> {
  const enviado = options.anonimo ? undefined : (options.tokenInicial ?? token)
  const headers = new Headers({ Accept: 'application/json' })
  if (enviado) headers.set('Authorization', `Bearer ${enviado}`)
  if (options.body) headers.set('Content-Type', 'application/json')

  let response: Response
  try {
    response = await fetch(new URL(`/api${path}`, window.location.origin), {
      method: options.method ?? 'GET',
      headers,
      signal: options.signal,
      body: options.body ? JSON.stringify(options.body) : undefined,
    })
  } catch (error) {
    if (options.signal?.aborted) throw error
    throw new ApiError(0)
  }

  const payload: unknown = await response.json().catch(() => undefined)
  if (!response.ok) {
    const status = response.status
    if (status === 401 && enviado && enviado === token && !options.tokenInicial) alExpirar?.()
    const parsed = respuestaError.safeParse(payload)
    if (parsed.success && parsed.data.status === status) {
      const message = status === 409 ? parsed.data.message : undefined
      throw new ApiError(status, message, status === 400 ? (parsed.data.details ?? {}) : {})
    }
    throw new ApiError(status)
  }

  const parsed = schema.safeParse(payload)
  if (!parsed.success)
    throw new ApiError(502, 'Recibimos una respuesta de la API en un formato inesperado.')
  return parsed.data
}

export const segmento = encodeURIComponent

export function parametro(nombre: string, valor: string) {
  return new URLSearchParams({ [nombre]: valor }).toString()
}
