import { RUTAS } from '../lib/rutas'
import { EnlaceBoton } from '../componentes/ui'

export function PaginaEstado({ status }: { status: 403 | 404 }) {
  const titulo =
    status === 403 ? 'No tienes permisos para acceder a esta sección.' : 'No encontramos esta página.'
  return (
    <section className="superficie mx-auto max-w-lg px-5 py-10 text-center">
      <h1 className="font-mono text-6xl font-bold text-marca-700 dark:text-indigo-300">{status}</h1>
      <p className="secundario my-5">{titulo}</p>
      <EnlaceBoton to={RUTAS.inicio}>Volver al inicio</EnlaceBoton>
    </section>
  )
}
