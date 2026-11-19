import { zodResolver } from '@hookform/resolvers/zod'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { useLocation, useNavigate } from 'react-router'
import { z } from 'zod'
import { useAvisos } from '../../componentes/avisos'
import { Detalles } from '../../componentes/Detalles'
import { Alerta } from '../../componentes/estados'
import { Modal } from '../../componentes/Modal'
import { Boton, EnlaceBoton } from '../../componentes/ui'
import { tipoTicket } from '../../lib/contratos'
import type { Evento, Ticket } from '../../lib/contratos'
import { tiposTicket } from '../../lib/formatos'
import { mensajeError } from '../../lib/http'
import { RUTAS } from '../../lib/rutas'
import { useSesion } from '../auth/sesion'
import { useComprarTicket } from './consultas'

const formulario = z.object({ type: tipoTicket })

export function PanelCompra({ evento }: { evento: Evento }) {
  const { sesion, tieneRol } = useSesion()
  const location = useLocation()
  const navigate = useNavigate()
  const { avisar } = useAvisos()
  const compra = useComprarTicket()
  const [elegido, setElegido] = useState<Ticket['type']>()
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<z.infer<typeof formulario>>({
    resolver: zodResolver(formulario),
    defaultValues: { type: 'GENERAL' },
  })

  if (sesion.estado === 'autenticado' && !tieneRol('USER')) return null
  const publicado = evento.status === 'PUBLISHED'
  const desde = location.pathname + location.search + location.hash

  function confirmar() {
    if (!elegido || sesion.estado !== 'autenticado' || compra.isPending) return
    compra.mutate(
      { type: elegido, eventCode: evento.eventCode, userEmail: sesion.usuario.email },
      {
        onSuccess: (ticket) => {
          setElegido(undefined)
          avisar('Compra realizada con éxito.')
          void navigate(RUTAS.confirmacion(ticket.ticketCode), { replace: true })
        },
      },
    )
  }

  const accion =
    publicado && sesion.estado === 'anonimo' ? (
      <EnlaceBoton to={RUTAS.login} state={{ desde }} className="w-full">
        Inicia sesión para comprar
      </EnlaceBoton>
    ) : (
      <Boton type="submit" disabled={!publicado || sesion.estado !== 'autenticado'} className="w-full">
        Comprar entrada
      </Boton>
    )

  return (
    <aside className="superficie self-start p-4 md:sticky md:top-24">
      <h2 className="text-xl font-semibold">Entradas</h2>
      <form
        noValidate
        className="mt-3 space-y-3"
        onSubmit={handleSubmit(({ type }) => {
          compra.reset()
          setElegido(type)
        })}
      >
        <fieldset disabled={!publicado} className="space-y-2">
          <legend className="secundario mb-2 text-sm">Tipo de entrada</legend>
          {tipoTicket.options.map((type) => (
            <label
              key={type}
              className="flex min-h-11 items-center gap-2 rounded-lg border border-slate-300 p-2.5 text-sm font-semibold has-checked:border-marca-500 has-checked:bg-marca-50 dark:border-slate-700 dark:has-checked:bg-indigo-950"
            >
              <input
                type="radio"
                value={type}
                {...register('type')}
                className="size-4 accent-marca-600"
              />{' '}
              {tiposTicket[type]}
            </label>
          ))}
          {errors.type?.message && <Alerta>{errors.type.message}</Alerta>}
        </fieldset>
        {publicado ? (
          <div className="barra-compra fixed inset-x-0 bottom-0 z-20 border-t border-slate-200 bg-white p-3 md:static md:border-0 md:bg-transparent md:p-0 dark:border-slate-800 dark:bg-slate-900 md:dark:bg-transparent">
            {accion}
          </div>
        ) : (
          accion
        )}
        <p className="secundario text-xs">
          {publicado
            ? 'El sistema define el precio al confirmar.'
            : 'Este evento no admite compras en este momento.'}
        </p>
      </form>
      {elegido && sesion.estado === 'autenticado' && (
        <Modal
          titulo="Confirmar compra"
          ocupado={compra.isPending}
          onCerrar={() => setElegido(undefined)}
        >
          <Detalles
            filas={[
              { etiqueta: 'Evento', valor: evento.name },
              { etiqueta: 'Tipo', valor: tiposTicket[elegido] },
              { etiqueta: 'Cuenta', valor: sesion.usuario.email },
            ]}
          />
          {compra.isError && <Alerta>{mensajeError(compra.error)}</Alerta>}
          <div className="mt-4 flex flex-wrap justify-end gap-2">
            <Boton
              variante="secundario"
              disabled={compra.isPending}
              onClick={() => setElegido(undefined)}
            >
              Cancelar
            </Boton>
            <Boton cargando={compra.isPending} onClick={confirmar}>
              Confirmar
            </Boton>
          </div>
        </Modal>
      )}
    </aside>
  )
}
