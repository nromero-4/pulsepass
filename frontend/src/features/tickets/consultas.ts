import { queryOptions, useMutation, useQueryClient } from '@tanstack/react-query'
import { useAvisos } from '../../componentes/avisos'
import { revisionSesion } from '../../lib/http'
import { eventosQ } from '../eventos/consultas'
import { apiTickets } from './api'

export const ticketsQ = {
  propios: (email: string) =>
    queryOptions({
      queryKey: ['tickets', 'usuario', email],
      queryFn: ({ signal }) => apiTickets.propios(email, signal),
      staleTime: 15_000,
    }),
  detalle: (code: string) =>
    queryOptions({
      queryKey: ['ticket', code],
      queryFn: ({ signal }) => apiTickets.porCodigo(code, signal),
      staleTime: 15_000,
      enabled: !!code,
    }),
}

export function useComprarTicket() {
  const client = useQueryClient()
  return useMutation({
    mutationFn: apiTickets.comprar,
    onMutate: revisionSesion,
    onSuccess: async (data, _compra, revision) => {
      if (revision !== revisionSesion()) return
      client.setQueryData(ticketsQ.detalle(data.ticketCode).queryKey, data)
      await Promise.all([
        client.invalidateQueries({ queryKey: ticketsQ.propios(data.userEmail).queryKey }),
        // Al venderse la última entrada, el detalle público puede devolver 404.
        // La siguiente visita lo vuelve a consultar sin interrumpir la confirmación actual.
        client.invalidateQueries({ queryKey: ['evento', data.eventCode], refetchType: 'none' }),
        client.invalidateQueries({ queryKey: ['eventos'] }),
        client.invalidateQueries({ queryKey: eventosQ.pagados(data.eventCode).queryKey }),
      ])
    },
  })
}

export function useActualizarTicket(accion: 'cancelar' | 'usar', mensajeExito?: string) {
  const client = useQueryClient()
  const { avisar } = useAvisos()
  return useMutation({
    mutationFn: apiTickets[accion],
    onMutate: revisionSesion,
    onSuccess: async (data, _code, revision) => {
      if (revision !== revisionSesion()) return
      client.setQueryData(ticketsQ.detalle(data.ticketCode).queryKey, data)
      await Promise.all([
        client.invalidateQueries({ queryKey: ticketsQ.propios(data.userEmail).queryKey }),
        client.invalidateQueries({ queryKey: eventosQ.pagados(data.eventCode).queryKey }),
      ])
      if (mensajeExito && revision === revisionSesion()) avisar(mensajeExito)
    },
  })
}
