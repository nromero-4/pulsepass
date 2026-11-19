import { zodResolver } from '@hookform/resolvers/zod'
import { useMutation } from '@tanstack/react-query'
import { useForm, useWatch } from 'react-hook-form'
import { Link, useLocation, useNavigate } from 'react-router'
import { Alerta } from '../componentes/estados'
import { Boton, Campo } from '../componentes/ui'
import { apiAuth } from '../features/auth/api'
import { Contrasena, FormularioCuenta } from '../features/auth/FormularioCuenta'
import { registro } from '../features/auth/formularios'
import { errorFormulario } from '../lib/formulario'
import { ApiError } from '../lib/http'
import { estadoLogin, RUTAS } from '../lib/rutas'

const campos = [
  'username',
  'email',
  'password',
  'firstName',
  'lastName',
  'phone',
  'city',
  'birthDate',
] as const

export function Component() {
  const navigate = useNavigate()
  const state = estadoLogin.safeParse(useLocation().state).data
  const alta = useMutation({ mutationFn: apiAuth.registrar })
  const {
    register,
    handleSubmit,
    control,
    setError,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(registro),
    defaultValues: {
      username: '',
      email: '',
      password: '',
      firstName: '',
      lastName: '',
      phone: '',
      city: '',
      birthDate: '',
    },
  })
  const clave = useWatch({ control, name: 'password' })
  const fortaleza = Math.min(
    4,
    Number(clave.length >= 12) +
      Number(clave.length >= 16) +
      Number(/[A-Z]/.test(clave) && /[a-z]/.test(clave)) +
      Number(/[^\p{L}\s]/u.test(clave)),
  )
  const nivel = fortaleza < 2 ? 'baja' : fortaleza < 4 ? 'media' : 'alta'

  return (
    <FormularioCuenta titulo="Crea tu cuenta">
      <form
        noValidate
        className="space-y-4"
        onSubmit={handleSubmit(async (data) => {
          try {
            await alta.mutateAsync(data)
            void navigate(RUTAS.login, { replace: true, state: { registro: true, desde: state?.desde } })
          } catch (error) {
            if (error instanceof ApiError && error.status === 409)
              setError('root', { message: 'El usuario o correo ya está registrado.' })
            else errorFormulario(error, setError, campos)
          }
        })}
      >
        <Campo
          {...register('username')}
          etiqueta="Nombre de usuario"
          autoComplete="username"
          error={errors.username?.message}
        />
        <Campo
          {...register('email')}
          etiqueta="Correo electrónico"
          type="email"
          autoComplete="email"
          error={errors.email?.message}
        />
        <div className="grid gap-3 sm:grid-cols-2">
          <Campo
            {...register('firstName')}
            etiqueta="Nombres"
            autoComplete="given-name"
            error={errors.firstName?.message}
          />
          <Campo
            {...register('lastName')}
            etiqueta="Apellidos"
            autoComplete="family-name"
            error={errors.lastName?.message}
          />
        </div>
        <Campo
          {...register('birthDate')}
          etiqueta="Fecha de nacimiento"
          type="date"
          autoComplete="bday"
          error={errors.birthDate?.message}
        />
        <Contrasena
          {...register('password')}
          etiqueta="Contraseña (mínimo 12 caracteres)"
          autoComplete="new-password"
          error={errors.password?.message}
        >
          {clave && (
            <div className="mt-1 flex items-center gap-2">
              <meter
                className="h-2 w-28"
                min={0}
                max={4}
                value={fortaleza}
                aria-label={`Fortaleza: ${nivel}`}
              />
              <p className="secundario text-xs">Fortaleza: {nivel}</p>
            </div>
          )}
        </Contrasena>
        <details>
          <summary className="secundario flex min-h-11 cursor-pointer items-center text-sm">
            Datos opcionales (teléfono y ciudad)
          </summary>
          <div className="grid gap-3 pt-1 sm:grid-cols-2">
            <Campo
              {...register('phone')}
              etiqueta="Teléfono"
              type="tel"
              autoComplete="tel"
              error={errors.phone?.message}
            />
            <Campo
              {...register('city')}
              etiqueta="Ciudad"
              autoComplete="address-level2"
              error={errors.city?.message}
            />
          </div>
        </details>
        {errors.root?.message && <Alerta>{errors.root.message}</Alerta>}
        <Boton type="submit" cargando={isSubmitting} className="w-full">
          Crear cuenta
        </Boton>
      </form>
      <p className="secundario mt-5 text-center text-sm">
        ¿Ya tienes cuenta?{' '}
        <Link to={RUTAS.login} state={state} className="enlace">
          Inicia sesión
        </Link>
      </p>
    </FormularioCuenta>
  )
}
