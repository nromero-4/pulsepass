import { useQuery } from '@tanstack/react-query'
import { Check } from 'lucide-react'
import { useParams } from 'react-router'
import { Consulta, Vacio } from '../componentes/estados'
import { Badge, Codigo, EnlaceBoton } from '../componentes/ui'
import { ticketsQ } from '../features/tickets/consultas'
import { codigo } from '../lib/contratos'
import { precioCOP, tiposTicket } from '../lib/formatos'
import { RUTAS } from '../lib/rutas'

export function Component() {
  const code = codigo.safeParse(useParams().ticketCode)
  const consulta = useQuery(ticketsQ.detalle(code.data ?? ''))
  return (
    <div className="mx-auto max-w-lg">
      {!code.success ? (
        <Vacio titulo="Ticket no disponible" />
      ) : (
        <Consulta consulta={consulta} noDisponible="Ticket no disponible" prohibido="ocultar">
          {(ticket) => (
            <section className="superficie p-6 text-center sm:p-9">
              <span className="mx-auto mb-4 grid size-14 place-items-center rounded-full bg-emerald-100 text-emerald-900">
                <Check aria-hidden="true" className="size-7" />
              </span>
              <h1 className="text-2xl font-bold">¡Compra realizada con éxito!</h1>
              <p className="mt-4 font-semibold">{ticket.eventName}</p>
              <div className="secundario my-3 flex flex-wrap items-center justify-center gap-2 text-sm">
                <Codigo>{ticket.ticketCode}</Codigo> <span>{tiposTicket[ticket.type]}</span>{' '}
                <span>{precioCOP(ticket.price)}</span>
              </div>
              <Badge estado={ticket.status} />
              <div className="mt-5 flex flex-wrap justify-center gap-2">
                <EnlaceBoton to={RUTAS.tickets}>Ver mis tickets</EnlaceBoton>
                <EnlaceBoton to={RUTAS.eventos} variante="secundario">
                  Seguir explorando
                </EnlaceBoton>
              </div>
            </section>
          )}
        </Consulta>
      )}
    </div>
  )
}
