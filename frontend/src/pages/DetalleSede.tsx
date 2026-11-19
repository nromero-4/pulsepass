import { useQuery } from '@tanstack/react-query'
import { Link, useParams } from 'react-router'
import { Detalles } from '../componentes/Detalles'
import { Consulta, Vacio } from '../componentes/estados'
import { Codigo } from '../componentes/ui'
import { sedesQ } from '../features/eventos/consultas'
import { Activo } from '../features/eventos/presentacion'
import { codigo } from '../lib/contratos'
import { RUTAS } from '../lib/rutas'

export function Component() {
  const code = codigo.safeParse(useParams().code)
  const consulta = useQuery(sedesQ.detalle(code.data ?? ''))
  return (
    <div className="estrecho">
      <Link to={RUTAS.sedes} className="enlace mb-4 inline-flex min-h-11 items-center text-sm">
        Volver a sedes
      </Link>
      {!code.success ? (
        <Vacio titulo="Sede no encontrada" />
      ) : (
        <Consulta consulta={consulta} noDisponible="Sede no encontrada">
          {(sede) => (
            <article className="superficie p-5 sm:p-7">
              <div className="mb-4 flex flex-wrap items-center justify-between gap-3">
                <h1 className="text-2xl font-bold">{sede.name}</h1>
                <Activo valor={sede.active} />
              </div>
              <Detalles
                filas={[
                  { etiqueta: 'Código', valor: <Codigo>{sede.code}</Codigo> },
                  { etiqueta: 'Ciudad', valor: sede.city },
                  { etiqueta: 'Dirección', valor: sede.address },
                  { etiqueta: 'Capacidad', valor: `${sede.capacity} personas` },
                ]}
              />
            </article>
          )}
        </Consulta>
      )}
    </div>
  )
}
