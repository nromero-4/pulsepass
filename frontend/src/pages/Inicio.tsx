import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router'
import { Consulta, Vacio } from '../componentes/estados'
import { EnlaceBoton } from '../componentes/ui'
import { useSesion } from '../features/auth/sesion'
import { artistasQ, eventosQ } from '../features/eventos/consultas'
import { Avatar, RejillaEventos } from '../features/eventos/presentacion'
import { RUTAS } from '../lib/rutas'

function CargandoEventos() {
  return (
    <div role="status" className="grid gap-4 sm:grid-cols-2 md:grid-cols-3">
      <span className="sr-only">Cargando próximos eventos…</span>
      {[1, 2, 3].map((posicion) => (
        <div
          key={posicion}
          aria-hidden="true"
          className="superficie overflow-hidden motion-safe:animate-pulse"
        >
          <div className="aspect-[2.45/1] bg-slate-200 dark:bg-slate-800" />
          <div className="h-42 space-y-3 p-4">
            <div className="h-2 w-16 rounded bg-slate-200 dark:bg-slate-800" />
            <div className="h-4 w-3/4 rounded bg-slate-200 dark:bg-slate-800" />
            <div className="h-3 w-1/2 rounded bg-slate-200 dark:bg-slate-800" />
            <div className="h-3 w-2/5 rounded bg-slate-200 dark:bg-slate-800" />
          </div>
        </div>
      ))}
    </div>
  )
}

export function Inicio() {
  const eventos = useQuery(eventosQ.publicados())
  const artistas = useQuery(artistasQ.activos())
  const { sesion } = useSesion()

  return (
    <>
      <section className="bg-linear-to-r from-marca-700 via-marca-700 to-rose-700 text-white">
        <div className="contenedor py-10 sm:py-16">
          <h1 className="max-w-2xl text-3xl leading-tight font-bold tracking-tight sm:text-5xl lg:text-6xl">
            Descubre los mejores eventos
          </h1>
          <p className="mt-3 max-w-xl text-indigo-50 sm:text-lg">
            Música, cultura, deporte y tecnología en un solo lugar.
          </p>
          <div className="mt-6 grid gap-2 sm:flex">
            <EnlaceBoton to={RUTAS.eventos} variante="blanco">
              Ver cartelera
            </EnlaceBoton>
            {sesion.estado === 'anonimo' && (
              <EnlaceBoton to={RUTAS.registro} variante="transparente">
                Crear cuenta
              </EnlaceBoton>
            )}
          </div>
        </div>
      </section>
      <div className="contenedor space-y-10 py-8 sm:py-10">
        <section className="space-y-4" aria-labelledby="proximos">
          <div className="flex flex-wrap items-center justify-between gap-x-3">
            <h2 id="proximos" className="text-2xl font-semibold">
              Próximos eventos
            </h2>
            <Link to={RUTAS.eventos} className="enlace inline-flex min-h-11 items-center text-sm">
              Ver todos <span className="sr-only">los eventos</span>
            </Link>
          </div>
          <Consulta consulta={eventos} cargando={<CargandoEventos />}>
            {(data) =>
              data.length ? (
                <RejillaEventos eventos={data.slice(0, 3)} nivel={3} />
              ) : (
                <Vacio titulo="Aún no hay eventos publicados" />
              )
            }
          </Consulta>
        </section>
        <section className="space-y-3" aria-labelledby="destacados">
          <div className="flex flex-wrap items-center justify-between gap-x-3">
            <h2 id="destacados" className="text-2xl font-semibold">
              Artistas destacados
            </h2>
            <Link to={RUTAS.artistas} className="enlace inline-flex min-h-11 items-center text-sm">
              Ver todos <span className="sr-only">los artistas</span>
            </Link>
          </div>
          <Consulta consulta={artistas}>
            {(data) =>
              data.length ? (
                <ul className="flex flex-wrap gap-x-5 gap-y-2">
                  {data.slice(0, 6).map((artista, orden) => (
                    <li key={artista.id}>
                      <Link
                        to={RUTAS.artista(artista.id)}
                        className="flex min-h-11 items-center gap-2 rounded-full text-sm font-semibold hover:underline"
                      >
                        <Avatar nombre={artista.stageName} orden={orden} />
                        {artista.stageName}
                      </Link>
                    </li>
                  ))}
                </ul>
              ) : (
                <Vacio titulo="Aún no hay artistas activos" />
              )
            }
          </Consulta>
        </section>
      </div>
    </>
  )
}
