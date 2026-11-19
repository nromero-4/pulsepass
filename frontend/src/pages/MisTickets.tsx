import { useQuery } from '@tanstack/react-query'
import clsx from 'clsx'
import { useState } from 'react'
import { Consulta, Vacio } from '../componentes/estados'
import { Badge, Boton, Cabecera, Codigo, EnlaceBoton, Fecha } from '../componentes/ui'
import { useSesion } from '../features/auth/sesion'
import { CancelarTicket } from '../features/tickets/CancelarTicket'
import { ticketsQ } from '../features/tickets/consultas'
import type { EstadoTicket, Ticket } from '../lib/contratos'
import { precioCOP, tiposTicket } from '../lib/formatos'
import { RUTAS } from '../lib/rutas'

const filtros = [
  ['todos', 'Todos'],
  ['PAID', 'Pagados'],
  ['USED', 'Usados'],
  ['CANCELLED', 'Cancelados'],
  ['RESERVED', 'Reservados'],
] as const

function TicketFila({ ticket }: { ticket: Ticket }) {
  return (
    <article className="superficie space-y-3 p-4 sm:p-5">
      <div className="flex flex-wrap items-start justify-between gap-2">
        <h2 className="text-lg font-semibold">{ticket.eventName}</h2>
        <Badge estado={ticket.status} />
      </div>
      <div className="secundario flex flex-wrap items-center gap-x-3 gap-y-1 text-sm">
        <Codigo>{ticket.ticketCode}</Codigo>
        <span>{tiposTicket[ticket.type]}</span>
        <span>{precioCOP(ticket.price)}</span>
        <Fecha valor={ticket.purchaseDate} />
      </div>
      <div className="flex flex-wrap justify-end gap-2">
        <EnlaceBoton variante="secundario" to={RUTAS.ticket(ticket.ticketCode)}>
          Ver detalle
        </EnlaceBoton>
        <CancelarTicket ticket={ticket} label="Cancelar" />
      </div>
    </article>
  )
}

function Lista({ email }: { email: string }) {
  const [seleccion, setSeleccion] = useState<EstadoTicket | 'todos'>('todos')
  const consulta = useQuery(ticketsQ.propios(email))
  return (
    <>
      <Cabecera titulo="Mis tickets" />
      <div className="mb-4 flex flex-wrap gap-2" role="group" aria-label="Filtrar tickets por estado">
        {filtros.map(([value, label]) => (
          <Boton
            key={value}
            variante={value === seleccion ? 'primario' : 'secundario'}
            className={clsx(
              'rounded-full',
              value !== seleccion && 'border-slate-200 dark:border-slate-700',
            )}
            aria-pressed={value === seleccion}
            onClick={() => setSeleccion(value)}
          >
            {label}
          </Boton>
        ))}
      </div>
      <Consulta consulta={consulta} prohibido="redirigir">
        {(data) => {
          if (!data.length)
            return (
              <Vacio titulo="Aún no tienes tickets">
                <EnlaceBoton to={RUTAS.eventos}>Ver cartelera</EnlaceBoton>
              </Vacio>
            )
          const visibles =
            seleccion === 'todos' ? data : data.filter(({ status }) => status === seleccion)
          return visibles.length ? (
            <div className="space-y-3">
              {visibles.map((ticket) => (
                <TicketFila key={ticket.id} ticket={ticket} />
              ))}
            </div>
          ) : (
            <Vacio titulo="No tienes tickets con este estado" />
          )
        }}
      </Consulta>
    </>
  )
}

export function Component() {
  const { sesion } = useSesion()
  return sesion.estado === 'autenticado' ? <Lista email={sesion.usuario.email} /> : null
}
