import { z } from 'zod'

export const categoria = z.enum([
  'MUSIC',
  'SPORTS',
  'TECHNOLOGY',
  'EDUCATION',
  'CULTURE',
  'ENTERTAINMENT',
])
export const estadoEvento = z.enum(['DRAFT', 'PUBLISHED', 'SOLD_OUT', 'CANCELLED', 'FINISHED'])
export const tipoTicket = z.enum(['GENERAL', 'VIP', 'BACKSTAGE', 'STUDENT'])
export const estadoTicket = z.enum(['RESERVED', 'PAID', 'CANCELLED', 'USED'])
export const rol = z.enum(['USER', 'STAFF', 'ADMIN'])
export type Rol = z.infer<typeof rol>

const id = z.int().positive()
const texto = z.string().min(1)
const opcional = z.string().nullish()
const fechaHora = z.iso.datetime({ local: true })

export function tieneCaracterDeControl(value: string) {
  return Array.from(value).some((character) => character.charCodeAt(0) < 32)
}

export const codigo = z
  .string()
  .trim()
  .min(1, 'Ingresa un código')
  .refine(
    (value) =>
      value !== '.' &&
      value !== '..' &&
      !value.includes('/') &&
      !value.includes('\\') &&
      !tieneCaracterDeControl(value),
    'Ingresa un código válido',
  )
export const idRuta = z.string().regex(/^\d+$/).transform(Number).pipe(id)
export const sede = z.object({
  id,
  code: texto,
  name: texto,
  city: texto,
  address: texto,
  capacity: z.int(),
  active: z.boolean(),
})
export const artista = z.object({
  id,
  stageName: texto,
  country: texto,
  genre: texto,
  active: z.boolean(),
})
export const evento = z.object({
  id,
  eventCode: texto,
  name: texto,
  description: z.string(),
  category: categoria,
  status: estadoEvento,
  eventDate: fechaHora,
  minimumAge: z.int().nonnegative(),
  venueCode: texto,
  venueName: texto,
  artists: z.array(texto),
})
export const ticket = z.object({
  id,
  ticketCode: texto,
  type: tipoTicket,
  status: estadoTicket,
  price: z.number().nonnegative(),
  purchaseDate: fechaHora,
  userEmail: z.email(),
  eventCode: texto,
  eventName: texto,
})
export const usuario = z.object({
  id,
  username: texto,
  email: z.email(),
  firstName: opcional,
  lastName: opcional,
  phone: opcional,
  city: opcional,
  birthDate: z.iso.date().nullish(),
  active: z.boolean(),
})
export const identidad = z.object({ userId: id, username: texto, email: z.email(), roles: z.array(rol) })
export const credencial = z.object({
  accessToken: texto,
  tokenType: z.literal('Bearer'),
  expiresIn: z.int().positive(),
})

export type Evento = z.infer<typeof evento>
export type Artista = z.infer<typeof artista>
export type Sede = z.infer<typeof sede>
export type Ticket = z.infer<typeof ticket>
export type Identidad = z.infer<typeof identidad>
export type Categoria = z.infer<typeof categoria>
export type EstadoTicket = z.infer<typeof estadoTicket>
