import { zodResolver } from '@hookform/resolvers/zod'
import { useQuery } from '@tanstack/react-query'
import { useForm, useWatch } from 'react-hook-form'
import { Link, useNavigate } from 'react-router'
import { useAvisos } from '../componentes/avisos'
import { Alerta, Consulta, Vacio } from '../componentes/estados'
import { Area, Boton, Cabecera, Campo, EnlaceBoton, Selector } from '../componentes/ui'
import { useSesion } from '../features/auth/sesion'
import { sedesQ, useCrearEvento } from '../features/eventos/consultas'
import { crearEvento } from '../features/eventos/formularios'
import { recordarEvento } from '../features/eventos/recientes'
import { categoria } from '../lib/contratos'
import type { Sede } from '../lib/contratos'
import { categorias } from '../lib/formatos'
import { errorFormulario } from '../lib/formulario'
import { RUTAS } from '../lib/rutas'

const campos = [
  'eventCode',
  'name',
  'description',
  'category',
  'eventDate',
  'minimumAge',
  'venueCode',
] as const

function FormularioEvento({ sedes }: { sedes: Sede[] }) {
  const crear = useCrearEvento()
  const { sesion } = useSesion()
  const { avisar } = useAvisos()
  const navigate = useNavigate()
  const {
    register,
    handleSubmit,
    control,
    setError,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(crearEvento),
    defaultValues: { eventCode: '', name: '', description: '', eventDate: '', venueCode: '' },
  })
  const descripcion = useWatch({ control, name: 'description' })

  return (
    <form
      noValidate
      className="superficie space-y-4 p-5 sm:p-7"
      onSubmit={handleSubmit(async (data) => {
        try {
          const nuevo = await crear.mutateAsync(data)
          if (sesion.estado === 'autenticado') recordarEvento(sesion.credencial.expiraEn, nuevo, true)
          avisar('Evento creado en borrador.')
          void navigate(RUTAS.gestion(nuevo.eventCode), { replace: true })
        } catch (error) {
          errorFormulario(error, setError, campos)
        }
      })}
    >
      <Campo
        {...register('eventCode')}
        etiqueta="Código del evento"
        placeholder="CMF-2026"
        error={errors.eventCode?.message}
      />
      <Campo {...register('name')} etiqueta="Nombre" error={errors.name?.message} />
      <Area
        {...register('description')}
        etiqueta="Descripción"
        rows={4}
        maxLength={1000}
        error={errors.description?.message}
      />
      <p className="secundario -mt-3 text-right text-xs">{descripcion.length}/1000 caracteres</p>
      <div className="grid gap-3 sm:grid-cols-2">
        <Selector {...register('category')} etiqueta="Categoría" error={errors.category?.message}>
          <option value="">Selecciona una categoría</option>
          {categoria.options.map((value) => (
            <option key={value} value={value}>
              {categorias[value]}
            </option>
          ))}
        </Selector>
        <Campo
          {...register('minimumAge', { valueAsNumber: true })}
          etiqueta="Edad mínima"
          type="number"
          min={0}
          step={1}
          error={errors.minimumAge?.message}
        />
      </div>
      <Campo
        {...register('eventDate')}
        etiqueta="Fecha y hora"
        type="datetime-local"
        error={errors.eventDate?.message}
      />
      <Selector {...register('venueCode')} etiqueta="Sede" error={errors.venueCode?.message}>
        <option value="">Selecciona una sede</option>
        {sedes.map((sede) => (
          <option key={sede.id} value={sede.code}>
            {sede.name} ({sede.code})
          </option>
        ))}
      </Selector>
      {errors.root?.message && <Alerta>{errors.root.message}</Alerta>}
      <div className="flex flex-wrap justify-end gap-2">
        <EnlaceBoton to={RUTAS.admin} variante="secundario">
          Cancelar
        </EnlaceBoton>
        <Boton type="submit" cargando={isSubmitting}>
          Crear evento
        </Boton>
      </div>
    </form>
  )
}

export function Component() {
  const consulta = useQuery(sedesQ.activas())
  return (
    <div className="estrecho">
      <Link to={RUTAS.admin} className="enlace mb-3 inline-flex min-h-11 items-center text-sm">
        Volver a eventos
      </Link>
      <Cabecera titulo="Nuevo evento" />
      <Consulta consulta={consulta}>
        {(sedes) =>
          sedes.length ? (
            <FormularioEvento sedes={sedes} />
          ) : (
            <Vacio
              titulo="No hay sedes activas"
              descripcion="Para crear un evento necesitas una sede activa."
            />
          )
        }
      </Consulta>
    </div>
  )
}
