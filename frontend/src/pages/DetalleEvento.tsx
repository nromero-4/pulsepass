import { useQuery } from '@tanstack/react-query'
import { CalendarDays, Landmark, UserRound } from 'lucide-react'
import { Link, useParams } from 'react-router'
import { Consulta, Vacio } from '../componentes/estados'
import { Badge, Fecha } from '../componentes/ui'
import { eventosQ, sedesQ } from '../features/eventos/consultas'
import { Avatar, DatoIcono, EtiquetaCategoria, PanelCategoria } from '../features/eventos/presentacion'
import { PanelCompra } from '../features/tickets/PanelCompra'
import type { Evento } from '../lib/contratos'
import { codigo } from '../lib/contratos'
import { RUTAS } from '../lib/rutas'

function SedeEvento({ evento }: { evento: Evento }) {
  const sede = useQuery(sedesQ.detalle(evento.venueCode))
  return (
    <section>
      <h2 className="mb-2 text-lg font-semibold">Sede</h2>
      <Link
        to={RUTAS.sede(evento.venueCode)}
        className="flex items-start gap-3 rounded-lg border border-slate-200 p-3 hover:bg-slate-50 dark:border-slate-700 dark:hover:bg-slate-800"
      >
        <Landmark aria-hidden="true" className="size-5 shrink-0 text-marca-600 dark:text-indigo-300" />
        <div>
          <span className="font-semibold">{evento.venueName}</span>
          {sede.data && (
            <p className="secundario text-sm">
              {sede.data.city} · Capacidad {sede.data.capacity}
            </p>
          )}
        </div>
      </Link>
    </section>
  )
}

function EventoVista({ evento }: { evento: Evento }) {
  return (
    <div className="grid gap-5 md:grid-cols-[minmax(0,2fr)_minmax(0,1fr)]">
      <article className="superficie overflow-hidden">
        <PanelCategoria categoria={evento.category} className="aspect-[2/1] sm:aspect-[3/1]" />
        <div className="space-y-5 p-4 sm:p-6">
          <header className="space-y-3">
            <div className="flex flex-wrap items-center gap-2">
              <EtiquetaCategoria categoria={evento.category} />
              <Badge estado={evento.status} />
            </div>
            <h1 className="text-3xl font-bold tracking-tight break-words sm:text-4xl">{evento.name}</h1>
            <div className="flex flex-wrap gap-x-4 gap-y-2">
              <DatoIcono icono={CalendarDays}>
                <Fecha valor={evento.eventDate} />
              </DatoIcono>
              <DatoIcono icono={UserRound}>Edad mínima: {evento.minimumAge}</DatoIcono>
            </div>
          </header>
          <p className="secundario break-words whitespace-pre-wrap">{evento.description}</p>
          <section>
            <h2 className="mb-2 text-lg font-semibold">Artistas</h2>
            {evento.artists.length ? (
              <ul className="flex flex-wrap gap-3">
                {evento.artists.map((nombre, index) => (
                  <li key={nombre}>
                    <Link
                      to={{
                        pathname: RUTAS.eventos,
                        search: new URLSearchParams({ artista: nombre }).toString(),
                      }}
                      className="inline-flex min-h-11 items-center gap-2 rounded-full text-sm font-semibold hover:underline"
                    >
                      <Avatar nombre={nombre} orden={index} />
                      {nombre}
                    </Link>
                  </li>
                ))}
              </ul>
            ) : (
              <p className="secundario text-sm">Sin artistas anunciados.</p>
            )}
          </section>
          <SedeEvento evento={evento} />
        </div>
      </article>
      <PanelCompra evento={evento} />
    </div>
  )
}

export function Component() {
  const code = codigo.safeParse(useParams().eventCode)
  const consulta = useQuery(eventosQ.detalle(code.data ?? ''))
  return (
    <>
      <Link to={RUTAS.eventos} className="enlace mb-4 inline-flex min-h-11 items-center text-sm">
        Volver a la cartelera
      </Link>
      {!code.success ? (
        <Vacio titulo="Evento no disponible" />
      ) : (
        <Consulta consulta={consulta} noDisponible="Evento no disponible" prohibido="ocultar">
          {(evento) => <EventoVista key={evento.id} evento={evento} />}
        </Consulta>
      )}
    </>
  )
}
