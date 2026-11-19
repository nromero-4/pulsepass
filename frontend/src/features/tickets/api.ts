import { z } from 'zod'
import { ticket } from '../../lib/contratos'
import type { Ticket } from '../../lib/contratos'
import { http, parametro, segmento } from '../../lib/http'

interface Compra {
  userEmail: string
  eventCode: string
  type: Ticket['type']
}

export const apiTickets = {
  comprar: (body: Compra) => http('/tickets', ticket, { method: 'POST', body }),
  porCodigo: (code: string, signal: AbortSignal) =>
    http(`/tickets/${segmento(code)}`, ticket, { signal }),
  propios: (email: string, signal: AbortSignal) =>
    http(`/tickets/by-user?${parametro('email', email)}`, z.array(ticket), { signal }),
  cancelar: (code: string) => http(`/tickets/${segmento(code)}/cancel`, ticket, { method: 'PATCH' }),
  usar: (code: string) => http(`/tickets/${segmento(code)}/use`, ticket, { method: 'PATCH' }),
}
