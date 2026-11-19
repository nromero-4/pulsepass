import { z } from 'zod'
import { artista, evento, sede, ticket } from '../../lib/contratos'
import { http, parametro, segmento } from '../../lib/http'
import type { CrearEvento } from './formularios'

export const apiEventos = {
  publicados: (signal: AbortSignal) =>
    http('/events/published', z.array(evento), { signal, anonimo: true }),
  porCodigo: (code: string, signal: AbortSignal, publico: boolean) =>
    http(`/events/${segmento(code)}`, evento, { signal, anonimo: publico }),
  porArtista: (name: string, signal: AbortSignal) =>
    http(`/events/by-artist?${parametro('stageName', name)}`, z.array(evento), {
      signal,
      anonimo: true,
    }),
  crear: (body: CrearEvento) => http('/events', evento, { method: 'POST', body }),
  publicar: (code: string) => http(`/events/${segmento(code)}/publish`, evento, { method: 'PATCH' }),
  asociar: ({ code, id }: { code: string; id: number; nombre: string }) =>
    http(`/events/${segmento(code)}/artists/${id}`, evento, { method: 'POST' }),
  pagados: (code: string, signal: AbortSignal) =>
    http(`/events/${segmento(code)}/tickets/paid`, z.array(ticket), { signal }),
}

export const apiArtistas = {
  activos: (signal: AbortSignal) => http('/artists/active', z.array(artista), { signal, anonimo: true }),
  porId: (id: number, signal: AbortSignal) => http(`/artists/${id}`, artista, { signal, anonimo: true }),
  porNombre: (nombre: string, signal: AbortSignal) =>
    http(`/artists/by-stage-name?${parametro('stageName', nombre)}`, artista, { signal, anonimo: true }),
}

export const apiSedes = {
  activas: (signal: AbortSignal) => http('/venues/active', z.array(sede), { signal, anonimo: true }),
  porCodigo: (code: string, signal: AbortSignal) =>
    http(`/venues/${segmento(code)}`, sede, { signal, anonimo: true }),
}
