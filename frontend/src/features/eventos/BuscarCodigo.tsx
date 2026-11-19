import { zodResolver } from '@hookform/resolvers/zod'
import { Search } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { Boton, Campo } from '../../componentes/ui'
import { buscarCodigo } from './formularios'

interface Props {
  etiqueta: string
  accion: string
  inicial?: string
  ocupado?: boolean
  onBuscar: (code: string) => void
}

export function BuscarCodigo({ etiqueta, accion, inicial = '', ocupado, onBuscar }: Props) {
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    resolver: zodResolver(buscarCodigo),
    defaultValues: { code: inicial },
  })
  return (
    <form
      noValidate
      className="grid min-w-0 items-end gap-2 sm:grid-cols-[minmax(0,1fr)_auto]"
      onSubmit={handleSubmit(({ code }) => onBuscar(code))}
    >
      <Campo
        {...register('code')}
        etiqueta={etiqueta}
        autoComplete="off"
        autoCapitalize="characters"
        spellCheck={false}
        error={errors.code?.message}
      />
      <Boton type="submit" cargando={ocupado}>
        <Search aria-hidden="true" className="size-4" />
        {accion}
      </Boton>
    </form>
  )
}
