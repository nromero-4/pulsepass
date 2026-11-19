import { Plus } from 'lucide-react'
import { useNavigate } from 'react-router'
import { Vacio } from '../componentes/estados'
import { Badge, Cabecera, Codigo, EnlaceBoton } from '../componentes/ui'
import { useSesion } from '../features/auth/sesion'
import { BuscarCodigo } from '../features/eventos/BuscarCodigo'
import { leerRecientes } from '../features/eventos/recientes'
import { RUTAS } from '../lib/rutas'

export function Component() {
  const { sesion } = useSesion()
  const navigate = useNavigate()
  const recientes = sesion.estado === 'autenticado' ? leerRecientes(sesion.credencial.expiraEn) : []
  return (
    <div className="estrecho">
      <Cabecera
        titulo="Administración de eventos"
        accion={
          <EnlaceBoton to={RUTAS.nuevoEvento}>
            <Plus aria-hidden="true" className="size-4" /> Nuevo evento
          </EnlaceBoton>
        }
      />
      <BuscarCodigo
        etiqueta="Buscar evento por código"
        accion="Abrir"
        onBuscar={(code) => {
          void navigate(RUTAS.gestion(code))
        }}
      />
      <section className="mt-6 space-y-3">
        <h2 className="text-lg font-semibold">Creados en esta sesión</h2>
        {recientes.length ? (
          <ul className="space-y-2">
            {recientes.map((event) => (
              <li key={event.eventCode} className="superficie flex flex-wrap items-center gap-2 p-3">
                <Codigo>{event.eventCode}</Codigo>
                <span className="min-w-0 grow font-semibold break-words">{event.name}</span>
                <Badge estado={event.status} />
                <EnlaceBoton to={RUTAS.gestion(event.eventCode)} variante="secundario">
                  Gestionar
                </EnlaceBoton>
              </li>
            ))}
          </ul>
        ) : (
          <Vacio titulo="No has creado eventos en esta sesión" />
        )}
        <p className="rounded-lg bg-marca-50 p-3 text-sm text-marca-700 dark:bg-indigo-950 dark:text-indigo-200">
          Para abrir un evento de otra sesión, búscalo por su código.
        </p>
      </section>
    </div>
  )
}
