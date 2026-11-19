import { useQuery } from '@tanstack/react-query'
import { Consulta, Vacio } from '../componentes/estados'
import { Cabecera } from '../componentes/ui'
import { artistasQ } from '../features/eventos/consultas'
import { ArtistaTarjeta } from '../features/eventos/presentacion'

export function Component() {
  const consulta = useQuery(artistasQ.activos())
  return (
    <>
      <Cabecera titulo="Artistas" />
      <Consulta consulta={consulta}>
        {(data) =>
          data.length ? (
            <div className="grid gap-4 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4">
              {data.map((artista, orden) => (
                <ArtistaTarjeta key={artista.id} artista={artista} orden={orden} />
              ))}
            </div>
          ) : (
            <Vacio titulo="Aún no hay artistas activos" />
          )
        }
      </Consulta>
    </>
  )
}
