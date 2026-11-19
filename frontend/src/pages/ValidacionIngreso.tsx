import { useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { Alerta, Consulta } from '../componentes/estados'
import { Badge, Boton, Cabecera } from '../componentes/ui'
import { BuscarCodigo } from '../features/eventos/BuscarCodigo'
import { ticketsQ, useActualizarTicket } from '../features/tickets/consultas'
import type { Ticket } from '../lib/contratos'
import { tiposTicket } from '../lib/formatos'
import { mensajeError } from '../lib/http'

function Resultado({ ticket }: { ticket: Ticket }) {
  const usar = useActualizarTicket('usar')
  return (
    <div className="mt-4 space-y-3">
      <article className="superficie space-y-3 p-4 sm:p-5">
        <div className="flex flex-wrap items-start justify-between gap-2">
          <h2 className="text-lg font-semibold">{ticket.eventName}</h2>
          <Badge estado={ticket.status} />
        </div>
        <p className="secundario break-words">
          {tiposTicket[ticket.type]} · Titular: {ticket.userEmail}
        </p>
        {ticket.status === 'PAID' && (
          <Boton
            className="w-full"
            cargando={usar.isPending}
            onClick={() => usar.mutate(ticket.ticketCode)}
          >
            Marcar como usado
          </Boton>
        )}
      </article>
      {usar.isSuccess && <Alerta exito>Ingreso registrado.</Alerta>}
      {usar.isError && <Alerta>{mensajeError(usar.error)}</Alerta>}
      {ticket.status === 'USED' && !usar.isSuccess && !usar.isPending && !usar.isError && (
        <Alerta>El ticket ya fue utilizado.</Alerta>
      )}
      {ticket.status !== 'USED' && ticket.status !== 'PAID' && (
        <Alerta>El ticket no está pagado.</Alerta>
      )}
    </div>
  )
}

export function Component() {
  const [code, setCode] = useState('')
  const [busqueda, setBusqueda] = useState(0)
  const consulta = useQuery(ticketsQ.detalle(code))
  return (
    <div className="estrecho">
      <Cabecera titulo="Validación de ingreso" />
      <BuscarCodigo
        etiqueta="Código de ticket"
        accion="Buscar"
        ocupado={!!code && consulta.isFetching}
        onBuscar={(value) => {
          setBusqueda((current) => current + 1)
          if (code === value) void consulta.refetch()
          else setCode(value)
        }}
      />
      {code && (
        <div className="mt-4">
          <Consulta consulta={consulta} noDisponible="Ticket no encontrado" prohibido="redirigir">
            {(ticket) => <Resultado key={busqueda} ticket={ticket} />}
          </Consulta>
        </div>
      )}
    </div>
  )
}
