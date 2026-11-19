import { HttpResponse, http } from 'msw'
import type { Artista, Evento, Identidad, Rol, Sede, Ticket } from '../lib/contratos'

export const base = 'http://localhost:5173/api'
export const tokenDemo = 'jwt-de-prueba'
export const email = 'andrea@pulsepass.test'
export const artistaDemo: Artista = {
  id: 5,
  stageName: 'Solar Beat',
  country: 'Colombia',
  genre: 'Electrónica',
  active: true,
}
export const sedeDemo: Sede = {
  id: 1,
  code: 'VEN-SMR-01',
  name: 'Marina Convention Center',
  city: 'Santa Marta',
  address: 'Carrera 1 #22',
  capacity: 3,
  active: true,
}
export const eventoDemo: Evento = {
  id: 1,
  eventCode: 'CMF-2026',
  name: 'Caribbean Music Fest 2026',
  description: 'Tres noches de música caribeña.',
  category: 'MUSIC',
  status: 'PUBLISHED',
  eventDate: '2026-12-12T20:00:00',
  minimumAge: 18,
  venueCode: sedeDemo.code,
  venueName: sedeDemo.name,
  artists: [artistaDemo.stageName],
}
export const ticketDemo: Ticket = {
  id: 1,
  ticketCode: 'TKT-8F2A1C',
  type: 'VIP',
  status: 'PAID',
  price: 250000,
  purchaseDate: '2026-11-05T14:32:00',
  userEmail: email,
  eventCode: eventoDemo.eventCode,
  eventName: eventoDemo.name,
}
export const perfilDemo = {
  id: 2,
  username: 'andrea',
  email,
  firstName: 'Andrea',
  lastName: 'Ruiz',
  phone: null,
  city: null,
  birthDate: '1998-04-12',
  active: true,
}
export const identidadDemo = (role: Rol = 'USER'): Identidad => ({
  userId: 2,
  username: 'andrea',
  email,
  roles: [role],
})
export const authDemo = { accessToken: tokenDemo, expiresIn: 900, tokenType: 'Bearer' }

export function fallo(
  status: number,
  message = 'Detalle interno',
  details: Record<string, string> = {},
) {
  return HttpResponse.json(
    {
      timestamp: '2026-11-05T14:30:00',
      status,
      error: 'Error',
      message,
      details,
    },
    { status },
  )
}

export const handlers = [
  http.get(base + '/events/published', () => HttpResponse.json([eventoDemo])),
  http.get(base + '/events/by-artist', () => HttpResponse.json([eventoDemo])),
  http.get(base + '/events/' + eventoDemo.eventCode, () => HttpResponse.json(eventoDemo)),
  http.get(base + '/artists/active', () => HttpResponse.json([artistaDemo])),
  http.get(base + '/artists/by-stage-name', () => HttpResponse.json(artistaDemo)),
  http.get(base + '/artists/' + artistaDemo.id, () => HttpResponse.json(artistaDemo)),
  http.get(base + '/venues/active', () => HttpResponse.json([sedeDemo])),
  http.get(base + '/venues/' + sedeDemo.code, () => HttpResponse.json(sedeDemo)),
  http.get(base + '/auth/me', ({ request }) =>
    request.headers.get('Authorization') === 'Bearer ' + tokenDemo
      ? HttpResponse.json(identidadDemo())
      : fallo(401),
  ),
  http.post(base + '/auth/login', () => HttpResponse.json(authDemo)),
  http.post(base + '/users', () => HttpResponse.json(perfilDemo, { status: 201 })),
  http.get(base + '/users/by-email', () => HttpResponse.json(perfilDemo)),
  http.get(base + '/users/by-username', () => HttpResponse.json(perfilDemo)),
  http.get(base + '/tickets/by-user', () => HttpResponse.json([ticketDemo])),
  http.get(base + '/tickets/' + ticketDemo.ticketCode, () => HttpResponse.json(ticketDemo)),
  http.get(base + '/events/' + eventoDemo.eventCode + '/tickets/paid', () =>
    HttpResponse.json([ticketDemo]),
  ),
]
