import { queryOptions, useMutation, useQueryClient } from '@tanstack/react-query'
import { revisionSesion } from '../../lib/http'
import { apiArtistas, apiEventos, apiSedes } from './api'

const minuto = 60_000

export const eventosQ = {
  publicados: () =>
    queryOptions({
      queryKey: ['eventos', 'publicados'],
      queryFn: ({ signal }) => apiEventos.publicados(signal),
      staleTime: minuto,
    }),
  detalle: (code: string, acceso: 'publico' | 'admin' = 'publico') =>
    queryOptions({
      queryKey: ['evento', code, acceso],
      queryFn: ({ signal }) => apiEventos.porCodigo(code, signal, acceso === 'publico'),
      staleTime: 30_000,
      enabled: !!code,
    }),
  porArtista: (name: string) =>
    queryOptions({
      queryKey: ['eventos', 'artista', name],
      queryFn: ({ signal }) => apiEventos.porArtista(name, signal),
      staleTime: minuto,
      enabled: !!name,
    }),
  pagados: (code: string) =>
    queryOptions({
      queryKey: ['tickets', 'pagados', code],
      queryFn: ({ signal }) => apiEventos.pagados(code, signal),
      staleTime: 15_000,
      enabled: !!code,
    }),
}

export const artistasQ = {
  activos: () =>
    queryOptions({
      queryKey: ['artistas', 'activos'],
      queryFn: ({ signal }) => apiArtistas.activos(signal),
      staleTime: 5 * minuto,
    }),
  detalle: (id: number) =>
    queryOptions({
      queryKey: ['artista', id],
      queryFn: ({ signal }) => apiArtistas.porId(id, signal),
      staleTime: 5 * minuto,
    }),
  porNombre: (name: string) =>
    queryOptions({
      queryKey: ['artista', 'nombre', name],
      queryFn: ({ signal }) => apiArtistas.porNombre(name, signal),
      staleTime: 5 * minuto,
      enabled: !!name,
    }),
}

export const sedesQ = {
  activas: () =>
    queryOptions({
      queryKey: ['sedes', 'activas'],
      queryFn: ({ signal }) => apiSedes.activas(signal),
      staleTime: 5 * minuto,
    }),
  detalle: (code: string) =>
    queryOptions({
      queryKey: ['sede', code],
      queryFn: ({ signal }) => apiSedes.porCodigo(code, signal),
      staleTime: 5 * minuto,
      enabled: !!code,
    }),
}

export function useCrearEvento() {
  const client = useQueryClient()
  return useMutation({
    mutationFn: apiEventos.crear,
    onMutate: revisionSesion,
    onSuccess: (data, _evento, revision) => {
      if (revision === revisionSesion())
        client.setQueryData(eventosQ.detalle(data.eventCode, 'admin').queryKey, data)
    },
  })
}

export function usePublicarEvento() {
  const client = useQueryClient()
  return useMutation({
    mutationFn: apiEventos.publicar,
    onMutate: revisionSesion,
    onSuccess: async (data, _code, revision) => {
      if (revision !== revisionSesion()) return
      client.setQueryData(eventosQ.detalle(data.eventCode, 'admin').queryKey, data)
      await Promise.all([
        client.invalidateQueries({ queryKey: ['evento', data.eventCode, 'publico'] }),
        client.invalidateQueries({ queryKey: ['eventos'] }),
      ])
    },
  })
}

export function useAsociarArtista() {
  const client = useQueryClient()
  return useMutation({
    mutationFn: apiEventos.asociar,
    onMutate: revisionSesion,
    onSuccess: async (data, _asociacion, revision) => {
      if (revision !== revisionSesion()) return
      client.setQueryData(eventosQ.detalle(data.eventCode, 'admin').queryKey, data)
      await Promise.all([
        client.invalidateQueries({ queryKey: ['evento', data.eventCode, 'publico'] }),
        client.invalidateQueries({ queryKey: ['eventos'] }),
      ])
    },
  })
}
