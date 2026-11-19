import { zodResolver } from '@hookform/resolvers/zod'
import { useQuery } from '@tanstack/react-query'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link, useParams } from 'react-router'
import { useAvisos } from '../componentes/avisos'
import { Alerta, Consulta, Vacio } from '../componentes/estados'
import { Modal } from '../componentes/Modal'
import { Badge, Boton, Codigo, Fecha, Selector } from '../componentes/ui'
import { useSesion } from '../features/auth/sesion'
import { artistasQ, eventosQ, useAsociarArtista, usePublicarEvento } from '../features/eventos/consultas'
import { asociarArtista } from '../features/eventos/formularios'
import { Avatar, EtiquetaCategoria } from '../features/eventos/presentacion'
import { recordarEvento } from '../features/eventos/recientes'
import { codigo } from '../lib/contratos'
import type { Artista, Evento } from '../lib/contratos'
import { mensajeError } from '../lib/http'
import { RUTAS } from '../lib/rutas'

function Asociar({ evento, artistas }: { evento: Evento; artistas: Artista[] }) {
  const asociar = useAsociarArtista()
  const { avisar } = useAvisos()
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm({ resolver: zodResolver(asociarArtista) })
  const disponibles = artistas.filter(({ stageName }) => !evento.artists.includes(stageName))

  return (
    <form
      noValidate
      className="space-y-2"
      onSubmit={handleSubmit(({ id }) => {
        const artista = disponibles.find((item) => item.id === id)
        if (!artista) return
        asociar.mutate(
          { id, code: evento.eventCode, nombre: artista.stageName },
          {
            onSuccess: () => {
              reset()
              avisar('Artista asociado al evento.')
            },
          },
        )
      })}
    >
      <div className="grid items-end gap-2 sm:grid-cols-[minmax(0,1fr)_auto]">
        <Selector
          {...register('id', { valueAsNumber: true })}
          etiqueta="Agregar artista"
          error={errors.id?.message}
          disabled={!disponibles.length}
        >
          <option value="">
            {disponibles.length ? 'Selecciona un artista' : 'No hay artistas disponibles'}
          </option>
          {disponibles.map((artista) => (
            <option key={artista.id} value={artista.id}>
              {artista.stageName}
            </option>
          ))}
        </Selector>
        <Boton
          variante="secundario"
          type="submit"
          cargando={asociar.isPending}
          disabled={!disponibles.length}
        >
          Asociar
        </Boton>
      </div>
      {asociar.isError && <Alerta>{mensajeError(asociar.error)}</Alerta>}
    </form>
  )
}

function Gestion({ evento }: { evento: Evento }) {
  const [confirmar, setConfirmar] = useState(false)
  const publicar = usePublicarEvento()
  const artistas = useQuery(artistasQ.activos())
  const { avisar } = useAvisos()
  const { sesion } = useSesion()

  return (
    <div className="space-y-4">
      <header className="space-y-2">
        <div className="flex flex-wrap items-start justify-between gap-2">
          <h1 className="text-3xl font-bold break-words">{evento.name}</h1>
          <Badge estado={evento.status} />
        </div>
        <div className="secundario flex flex-wrap items-center gap-2 text-sm">
          <Codigo>{evento.eventCode}</Codigo>
          <EtiquetaCategoria categoria={evento.category} />
          <Fecha valor={evento.eventDate} />
          <span>Edad mínima: {evento.minimumAge}</span>
          <span>{evento.venueName}</span>
        </div>
      </header>
      <section className="superficie flex flex-wrap items-center justify-between gap-3 p-5">
        <div>
          <h2 className="text-lg font-semibold">Publicación</h2>
          <p className="secundario mt-1 text-sm">
            {evento.status === 'DRAFT'
              ? 'El evento será visible en la cartelera una vez publicado.'
              : 'Consulta el estado actual en la cabecera del evento.'}
          </p>
        </div>
        <Boton
          disabled={evento.status !== 'DRAFT'}
          onClick={() => {
            publicar.reset()
            setConfirmar(true)
          }}
        >
          Publicar evento
        </Boton>
      </section>
      <section className="superficie space-y-3 p-5">
        <h2 className="text-lg font-semibold">Artistas asociados</h2>
        {evento.artists.length ? (
          <ul className="flex flex-wrap gap-3">
            {evento.artists.map((nombre, orden) => (
              <li key={nombre} className="inline-flex items-center gap-2 text-sm font-medium">
                <Avatar nombre={nombre} orden={orden} />
                {nombre}
              </li>
            ))}
          </ul>
        ) : (
          <p className="secundario text-sm">Aún no hay artistas asociados.</p>
        )}
        <Consulta consulta={artistas}>{(data) => <Asociar evento={evento} artistas={data} />}</Consulta>
      </section>
      <Link
        className="enlace inline-flex min-h-11 items-center"
        to={RUTAS.ticketsPagados(evento.eventCode)}
      >
        Tickets pagados
      </Link>
      {confirmar && (
        <Modal
          titulo="Publicar evento"
          ocupado={publicar.isPending}
          onCerrar={() => setConfirmar(false)}
        >
          <p className="secundario">¿Quieres publicar {evento.name} en la cartelera?</p>
          {publicar.isError && (
            <div className="mt-3">
              <Alerta>{mensajeError(publicar.error)}</Alerta>
            </div>
          )}
          <div className="mt-4 flex flex-wrap justify-end gap-2">
            <Boton
              variante="secundario"
              disabled={publicar.isPending}
              onClick={() => setConfirmar(false)}
            >
              Volver
            </Boton>
            <Boton
              cargando={publicar.isPending}
              onClick={() =>
                publicar.mutate(evento.eventCode, {
                  onSuccess: (data) => {
                    if (sesion.estado === 'autenticado') recordarEvento(sesion.credencial.expiraEn, data)
                    setConfirmar(false)
                    avisar('Evento publicado.')
                  },
                })
              }
            >
              Confirmar publicación
            </Boton>
          </div>
        </Modal>
      )}
    </div>
  )
}

export function Component() {
  const code = codigo.safeParse(useParams().eventCode)
  const consulta = useQuery(eventosQ.detalle(code.data ?? '', 'admin'))
  return (
    <div className="estrecho">
      <Link to={RUTAS.admin} className="enlace mb-4 inline-flex min-h-11 items-center text-sm">
        Volver a eventos
      </Link>
      {!code.success ? (
        <Vacio titulo="Evento no encontrado" />
      ) : (
        <Consulta consulta={consulta} noDisponible="Evento no encontrado" prohibido="redirigir">
          {(evento) => <Gestion key={evento.id} evento={evento} />}
        </Consulta>
      )}
    </div>
  )
}
