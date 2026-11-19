import { useQuery } from '@tanstack/react-query'
import { Link, useParams } from 'react-router'
import { Consulta, Vacio } from '../componentes/estados'
import { artistasQ, eventosQ } from '../features/eventos/consultas'
import { Avatar, RejillaEventos } from '../features/eventos/presentacion'
import type { Artista } from '../lib/contratos'
import { idRuta } from '../lib/contratos'
import { RUTAS } from '../lib/rutas'

function Perfil({ artista }: { artista: Artista }) {
  const eventos = useQuery(eventosQ.porArtista(artista.stageName))
  return (
    <>
      <header className="superficie mb-7 flex flex-wrap items-center gap-4 p-5 sm:p-7">
        <Avatar nombre={artista.stageName} grande />
        <div className="min-w-0">
          <h1 className="text-3xl font-bold break-words">{artista.stageName}</h1>
          <p className="secundario mt-1">
            {artista.country} · {artista.genre}
          </p>
        </div>
      </header>
      <h2 className="mb-4 text-xl font-semibold">Próximos eventos de {artista.stageName}</h2>
      <Consulta consulta={eventos}>
        {(data) =>
          data.length ? (
            <RejillaEventos eventos={data} nivel={3} />
          ) : (
            <Vacio titulo="Sin eventos próximos" />
          )
        }
      </Consulta>
    </>
  )
}

function ArtistaConsultado({ id }: { id: number }) {
  const consulta = useQuery(artistasQ.detalle(id))
  return (
    <Consulta consulta={consulta} noDisponible="Artista no encontrado">
      {(data) => <Perfil artista={data} />}
    </Consulta>
  )
}

export function Component() {
  const id = idRuta.safeParse(useParams().id)
  return (
    <>
      <Link to={RUTAS.artistas} className="enlace mb-4 inline-flex min-h-11 items-center text-sm">
        Volver a artistas
      </Link>
      {id.success ? <ArtistaConsultado id={id.data} /> : <Vacio titulo="Artista no encontrado" />}
    </>
  )
}
