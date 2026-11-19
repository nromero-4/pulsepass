import { useQuery } from '@tanstack/react-query'
import { Copy } from 'lucide-react'
import { Link, useParams } from 'react-router'
import { useAvisos } from '../componentes/avisos'
import { Detalles } from '../componentes/Detalles'
import { Consulta, Vacio } from '../componentes/estados'
import { Badge, Boton, Codigo, Fecha } from '../componentes/ui'
import { CancelarTicket } from '../features/tickets/CancelarTicket'
import { ticketsQ } from '../features/tickets/consultas'
import { codigo } from '../lib/contratos'
import type { Ticket } from '../lib/contratos'
import { precioCOP, tiposTicket } from '../lib/formatos'
import { RUTAS } from '../lib/rutas'

function TicketDetalle({ ticket }: { ticket: Ticket }) {
  const { avisar } = useAvisos()
  async function copiar() {
    try {
      await navigator.clipboard.writeText(ticket.ticketCode)
      avisar('Código copiado.')
    } catch {
      avisar('No se pudo copiar. Puedes seleccionar el código manualmente.', 'error')
    }
  }

  return (
    <>
      <article className="superficie p-5 sm:p-7">
        <div className="mb-4 flex flex-wrap justify-between gap-2">
          <h1 className="text-2xl font-bold">{ticket.eventName}</h1>
          <Badge estado={ticket.status} />
        </div>
        <Detalles
          filas={[
            {
              etiqueta: 'Código',
              valor: (
                <span className="inline-flex flex-wrap items-center gap-2">
                  <Codigo>{ticket.ticketCode}</Codigo>
                  <Boton variante="secundario" onClick={copiar}>
                    <Copy aria-hidden="true" className="size-4" /> Copiar
                  </Boton>
                </span>
              ),
            },
            { etiqueta: 'Tipo', valor: tiposTicket[ticket.type] },
            { etiqueta: 'Precio', valor: precioCOP(ticket.price) },
            { etiqueta: 'Comprado el', valor: <Fecha valor={ticket.purchaseDate} /> },
            { etiqueta: 'Titular', valor: ticket.userEmail },
          ]}
        />
        {ticket.status === 'PAID' && (
          <p className="secundario mt-3 text-sm">Presenta este código en el ingreso.</p>
        )}
      </article>
      <div className="mt-4 flex justify-end">
        <CancelarTicket ticket={ticket} />
      </div>
    </>
  )
}

export function Component() {
  const code = codigo.safeParse(useParams().ticketCode)
  const consulta = useQuery(ticketsQ.detalle(code.data ?? ''))
  return (
    <div className="estrecho">
      <Link to={RUTAS.tickets} className="enlace mb-4 inline-flex min-h-11 items-center text-sm">
        Volver a mis tickets
      </Link>
      {!code.success ? (
        <Vacio titulo="Ticket no disponible" />
      ) : (
        <Consulta consulta={consulta} noDisponible="Ticket no disponible" prohibido="ocultar">
          {(ticket) => <TicketDetalle ticket={ticket} />}
        </Consulta>
      )}
    </div>
  )
}
