import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import { Link, useLocation } from 'react-router'
import { Alerta } from '../componentes/estados'
import { Boton, Campo } from '../componentes/ui'
import { Contrasena, FormularioCuenta } from '../features/auth/FormularioCuenta'
import { login } from '../features/auth/formularios'
import { useSesion } from '../features/auth/sesion'
import { errorFormulario } from '../lib/formulario'
import { ApiError } from '../lib/http'
import { estadoLogin, RUTAS } from '../lib/rutas'

export function Component() {
  const { iniciarSesion } = useSesion()
  const state = estadoLogin.safeParse(useLocation().state).data
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm({
    resolver: zodResolver(login),
    defaultValues: { identifier: '', password: '' },
  })

  return (
    <FormularioCuenta titulo="Inicia sesión">
      <form
        noValidate
        className="space-y-4"
        onSubmit={handleSubmit(async (data) => {
          try {
            await iniciarSesion(data)
          } catch (error) {
            if (error instanceof ApiError && error.status === 401)
              setError('root', { message: 'Credenciales inválidas.' })
            else errorFormulario(error, setError, ['identifier', 'password'])
          }
        })}
      >
        {state?.registro && <Alerta exito>Cuenta creada. Inicia sesión para continuar.</Alerta>}
        {state?.motivo === 'expirada' && <Alerta>Tu sesión expiró. Vuelve a iniciar sesión.</Alerta>}
        <Campo
          {...register('identifier')}
          etiqueta="Usuario o correo"
          autoComplete="username"
          error={errors.identifier?.message}
        />
        <Contrasena
          {...register('password')}
          etiqueta="Contraseña"
          autoComplete="current-password"
          error={errors.password?.message}
        />
        {errors.root?.message && <Alerta>{errors.root.message}</Alerta>}
        <Boton type="submit" className="w-full" cargando={isSubmitting}>
          Ingresar
        </Boton>
      </form>
      <p className="secundario mt-5 text-center text-sm">
        ¿No tienes cuenta?{' '}
        <Link
          to={RUTAS.registro}
          state={state?.desde ? { desde: state.desde } : undefined}
          className="enlace"
        >
          Regístrate
        </Link>
      </p>
    </FormularioCuenta>
  )
}
