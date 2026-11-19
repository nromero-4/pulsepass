import { useState } from 'react'
import { Alerta } from '../../componentes/estados'
import { Modal } from '../../componentes/Modal'
import { Boton, Codigo } from '../../componentes/ui'
import type { Ticket } from '../../lib/contratos'
import { mensajeError } from '../../lib/http'
import { useActualizarTicket } from './consultas'

export function CancelarTicket({
  ticket,
  label = 'Cancelar ticket',
}: {
  ticket: Ticket
  label?: string
}) {
  const [abierto, setAbierto] = useState(false)
  const accion = useActualizarTicket('cancelar', 'Ticket cancelado.')
  if (ticket.status !== 'PAID' && !abierto) return null

  return (
    <>
      <Boton
        variante="peligro"
        aria-label={label === 'Cancelar' ? `Cancelar ticket ${ticket.ticketCode}` : undefined}
        onClick={() => {
          accion.reset()
          setAbierto(true)
        }}
      >
        {label}
      </Boton>
      {abierto && (
        <Modal titulo="Cancelar ticket" ocupado={accion.isPending} onCerrar={() => setAbierto(false)}>
          <p className="secundario mb-3">
            ¿Quieres cancelar el ticket <Codigo>{ticket.ticketCode}</Codigo> de {ticket.eventName}?
          </p>
          {accion.isError && <Alerta>{mensajeError(accion.error)}</Alerta>}
          <div className="mt-4 flex flex-wrap justify-end gap-2">
            <Boton variante="secundario" disabled={accion.isPending} onClick={() => setAbierto(false)}>
              Volver
            </Boton>
            <Boton
              variante="peligro"
              cargando={accion.isPending}
              onClick={() =>
                accion.mutate(ticket.ticketCode, {
                  onSuccess: () => setAbierto(false),
                })
              }
            >
              Cancelar ticket
            </Boton>
          </div>
        </Modal>
      )}
    </>
  )
}
