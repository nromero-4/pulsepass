import { useQuery } from '@tanstack/react-query'
import { useNavigate, useParams } from 'react-router'
import { useAvisos } from '../componentes/avisos'
import { Consulta, Vacio } from '../componentes/estados'
import { Tabla } from '../componentes/Tabla'
import { Badge, Boton, Cabecera, Codigo } from '../componentes/ui'
import { BuscarCodigo } from '../features/eventos/BuscarCodigo'
import { eventosQ } from '../features/eventos/consultas'
import { useActualizarTicket } from '../features/tickets/consultas'
import { codigo } from '../lib/contratos'
import type { Ticket } from '../lib/contratos'
import { precioCOP, tiposTicket } from '../lib/formatos'
import { mensajeError } from '../lib/http'
import { RUTAS } from '../lib/rutas'

function Usar({ ticket, ancho = false }: { ticket: Ticket; ancho?: boolean }) {
  const usar = useActualizarTicket('usar', 'Ingreso registrado.')
  const { avisar } = useAvisos()
  return (
    <Boton
      className={ancho ? 'w-full' : undefined}
      cargando={usar.isPending}
      aria-label={`Usar ticket ${ticket.ticketCode}`}
      onClick={() =>
        usar.mutate(ticket.ticketCode, {
          onError: (error) => avisar(mensajeError(error), 'error'),
        })
      }
    >
      Usar
    </Boton>
  )
}

function Resultados({ code }: { code: string }) {
  const consulta = useQuery(eventosQ.pagados(code))
  return (
    <Consulta consulta={consulta} noDisponible="Evento no encontrado" prohibido="redirigir">
      {(tickets) => (
        <>
          <p className="secundario mb-4 text-sm" aria-live="polite">
            Total: {tickets.length}
          </p>
          {!tickets.length ? (
            <Vacio titulo="No hay tickets pagados" />
          ) : (
            <>
              <div className="hidden md:block">
                <Tabla
                  titulo="Tickets pagados"
                  clave={(ticket) => ticket.id}
                  filas={tickets}
                  columnas={[
                    { encabezado: 'Código', celda: (ticket) => <Codigo>{ticket.ticketCode}</Codigo> },
                    { encabezado: 'Tipo', celda: (ticket) => tiposTicket[ticket.type] },
                    { encabezado: 'Titular', celda: (ticket) => ticket.userEmail },
                    { encabezado: 'Precio', celda: (ticket) => precioCOP(ticket.price) },
                    { encabezado: 'Estado', celda: (ticket) => <Badge estado={ticket.status} /> },
                    { encabezado: 'Acción', celda: (ticket) => <Usar ticket={ticket} /> },
                  ]}
                />
              </div>
              <div className="grid gap-3 md:hidden">
                {tickets.map((ticket) => (
                  <article key={ticket.id} className="superficie space-y-2 p-4">
                    <div className="flex flex-wrap items-center justify-between gap-2">
                      <Codigo>{ticket.ticketCode}</Codigo>
                      <Badge estado={ticket.status} />
                    </div>
                    <p className="secundario text-sm break-words">
                      {tiposTicket[ticket.type]} · {ticket.userEmail} · {precioCOP(ticket.price)}
                    </p>
                    <Usar ticket={ticket} ancho />
                  </article>
                ))}
              </div>
            </>
          )}
        </>
      )}
    </Consulta>
  )
}

export function Component() {
  const param = useParams().eventCode
  const code = codigo.safeParse(param).data
  const navigate = useNavigate()

  return (
    <>
      <Cabecera titulo={code ? `Tickets pagados · ${code}` : 'Tickets pagados'} />
      <div className="mb-4 max-w-sm">
        <BuscarCodigo
          key={param ?? 'nuevo'}
          etiqueta="Código del evento"
          accion="Consultar"
          inicial={code}
          onBuscar={(value) => {
            void navigate(RUTAS.ticketsPagados(value))
          }}
        />
      </div>
      {code ? (
        <Resultados code={code} />
      ) : param ? (
        <Vacio titulo="Evento no encontrado" />
      ) : (
        <Vacio
          titulo="Selecciona un evento"
          descripcion="Ingresa el código para consultar sus tickets pagados."
        />
      )}
    </>
  )
}
