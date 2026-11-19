import { z } from 'zod'
import { tieneCaracterDeControl } from './contratos'
import { segmento } from './http'

const eventos = '/eventos'
const artistas = '/artistas'
const sedes = '/sedes'
const tickets = '/mis-tickets'
const confirmacion = '/compra/confirmacion'
const admin = '/admin/eventos'
const staff = '/staff/eventos'

export const RUTAS = {
  inicio: '/',
  eventos,
  artistas,
  sedes,
  login: '/login',
  registro: '/registro',
  cuenta: '/cuenta',
  tickets,
  admin,
  nuevoEvento: `${admin}/nuevo`,
  validar: '/staff/validacion',
  prohibido: '/403',
  pagados: staff,
  patronEvento: `${eventos}/:eventCode`,
  evento: (code: string) => `${eventos}/${segmento(code)}`,
  patronArtista: `${artistas}/:id`,
  artista: (id: number) => `${artistas}/${id}`,
  patronSede: `${sedes}/:code`,
  sede: (code: string) => `${sedes}/${segmento(code)}`,
  patronTicket: `${tickets}/:ticketCode`,
  ticket: (code: string) => `${tickets}/${segmento(code)}`,
  patronConfirmacion: `${confirmacion}/:ticketCode`,
  confirmacion: (code: string) => `${confirmacion}/${segmento(code)}`,
  patronPagados: `${staff}/:eventCode/tickets`,
  ticketsPagados: (code: string) => `${staff}/${segmento(code)}/tickets`,
  patronGestion: `${admin}/:eventCode`,
  gestion: (code: string) => `${admin}/${segmento(code)}`,
} as const

export const estadoLogin = z.object({
  desde: z.string().optional(),
  motivo: z.literal('expirada').optional(),
  registro: z.boolean().optional(),
})

export function retornoSeguro(desde?: string) {
  if (
    !desde?.startsWith('/') ||
    desde.startsWith('//') ||
    desde.includes('\\') ||
    tieneCaracterDeControl(desde)
  )
    return RUTAS.inicio
  const url = new URL(desde, window.location.origin)
  const ruta = url.pathname.replace(/\/+$/, '').toLowerCase()
  if (url.origin !== window.location.origin || ruta === RUTAS.login || ruta === RUTAS.registro)
    return RUTAS.inicio
  return url.pathname + url.search + url.hash
}
