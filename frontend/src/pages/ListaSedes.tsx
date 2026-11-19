import { useQuery } from '@tanstack/react-query'
import { Consulta, Vacio } from '../componentes/estados'
import { Cabecera } from '../componentes/ui'
import { sedesQ } from '../features/eventos/consultas'
import { SedeTarjeta } from '../features/eventos/presentacion'

export function Component() {
  const consulta = useQuery(sedesQ.activas())
  return (
    <>
      <Cabecera titulo="Sedes" />
      <Consulta consulta={consulta}>
        {(data) =>
          data.length ? (
            <div className="grid gap-4 sm:grid-cols-2 md:grid-cols-3">
              {data.map((sede) => (
                <SedeTarjeta key={sede.id} sede={sede} />
              ))}
            </div>
          ) : (
            <Vacio titulo="Aún no hay sedes activas" />
          )
        }
      </Consulta>
    </>
  )
}
