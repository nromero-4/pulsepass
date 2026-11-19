import { useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { useNavigate } from 'react-router'
import { Consulta } from '../componentes/estados'
import { Boton, Cabecera, EnlaceBoton, Fecha } from '../componentes/ui'
import { cuentaQ } from '../features/auth/consultas'
import { useSesion } from '../features/auth/sesion'
import { Activo, Avatar } from '../features/eventos/presentacion'
import type { Identidad } from '../lib/contratos'
import { RUTAS } from '../lib/rutas'

function Perfil({ identidad }: { identidad: Identidad }) {
  const [porUsuario, cambiarConsulta] = useState(false)
  const { cerrarSesion, tieneRol } = useSesion()
  const navigate = useNavigate()
  const consulta = useQuery(
    porUsuario ? cuentaQ.porUsuario(identidad.username) : cuentaQ.porEmail(identidad.email),
  )

  return (
    <>
      <Cabecera titulo="Mi cuenta" />
      <Consulta consulta={consulta} noDisponible="Perfil no encontrado" prohibido="redirigir">
        {(data) => {
          const nombre = [data.firstName, data.lastName].filter(Boolean).join(' ') || data.username
          return (
            <section className="superficie flex flex-wrap items-center gap-4 p-5 sm:p-7">
              <Avatar nombre={nombre} grande />
              <div className="min-w-0 flex-1">
                <h2 className="text-xl font-semibold break-words">{nombre}</h2>
                <p className="secundario break-words">
                  @{data.username} · {data.email}
                </p>
                {data.birthDate && (
                  <p className="secundario mt-1 text-sm">
                    Nacimiento: <Fecha valor={data.birthDate} />
                  </p>
                )}
              </div>
              <div className="space-y-2">
                <div className="flex flex-wrap gap-1">
                  {identidad.roles.map((role) => (
                    <span
                      key={role}
                      className="rounded bg-indigo-100 px-2 py-1 text-xs font-semibold text-indigo-900"
                    >
                      {role}
                    </span>
                  ))}
                </div>
                <Activo valor={data.active} />
              </div>
            </section>
          )
        }}
      </Consulta>
      <div className="mt-4 flex flex-wrap gap-2">
        {tieneRol('USER') && <EnlaceBoton to={RUTAS.tickets}>Ver mis tickets</EnlaceBoton>}
        <Boton variante="fantasma" onClick={() => cambiarConsulta(!porUsuario)}>
          {porUsuario ? 'Consultar por correo' : 'Consultar por nombre de usuario'}
        </Boton>
        <Boton
          variante="secundario"
          className="sm:ml-auto"
          onClick={() => {
            void navigate(RUTAS.inicio, { replace: true, flushSync: true })
            cerrarSesion()
          }}
        >
          Cerrar sesión
        </Boton>
      </div>
    </>
  )
}

export function Component() {
  const { sesion } = useSesion()
  return sesion.estado === 'autenticado' ? <Perfil identidad={sesion.usuario} /> : null
}
