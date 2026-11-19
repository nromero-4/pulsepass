import { screen, waitFor, within } from '@testing-library/react'
import type { UserEvent } from '@testing-library/user-event'
import { http } from 'msw'
import { describe, expect, it, vi } from 'vitest'
import { ALMACEN_SESION } from '../features/auth/sesion'
import { RUTAS } from '../lib/rutas'
import { base, fallo } from './fixtures'
import { app, FECHA_SESION } from './renderApp'
import { server } from './setup'

async function completarLogin(user: UserEvent) {
  await user.type(screen.getByLabelText('Usuario o correo'), 'andrea')
  await user.type(screen.getByLabelText('Contraseña'), 'Clave-de-prueba')
  await user.click(within(screen.getByRole('main')).getByRole('button', { name: 'Ingresar' }))
}

async function completarRegistro(user: UserEvent, clave: string) {
  await screen.findByRole('heading', { name: 'Crea tu cuenta' })
  const valores = [
    ['Nombre de usuario', 'andrea'],
    ['Correo electrónico', 'andrea@pulsepass.test'],
    ['Nombres', 'Andrea'],
    ['Apellidos', 'Ruiz'],
    ['Fecha de nacimiento', '1998-04-12'],
    ['Contraseña (mínimo 12 caracteres)', clave],
  ]
  for (const [label, value] of valores) {
    if (label && value) await user.type(screen.getByLabelText(label, { exact: true }), value)
  }
  await user.click(screen.getByRole('button', { name: 'Crear cuenta' }))
}

describe('Autenticación y guardas', () => {
  it('redirige una ruta protegida y vuelve a la ruta de origen tras iniciar sesión', async () => {
    const { router, usuario } = app(RUTAS.tickets)
    await screen.findByRole('heading', { name: 'Inicia sesión' })
    expect(router.state.location.state.desde).toBe(RUTAS.tickets)
    await completarLogin(usuario)
    await screen.findByRole('heading', { name: 'Mis tickets' })
    expect(sessionStorage.getItem(ALMACEN_SESION)).toContain('jwt-de-prueba')
  })

  it('muestra un mensaje genérico para credenciales inválidas', async () => {
    server.use(http.post(base + '/auth/login', () => fallo(401, 'El usuario no existe')))
    const { usuario } = app(RUTAS.login)
    await screen.findByRole('heading', { name: 'Inicia sesión' })
    await completarLogin(usuario)
    await screen.findByText('Credenciales inválidas.')
    expect(screen.queryByText('El usuario no existe')).not.toBeInTheDocument()
  })

  it('valida los campos obligatorios de login antes de enviar', async () => {
    const enviar = vi.fn(() => fallo(400))
    server.use(http.post(base + '/auth/login', enviar))
    const { usuario } = app(RUTAS.login)
    await screen.findByRole('heading', { name: 'Inicia sesión' })
    await usuario.click(screen.getByRole('button', { name: 'Ingresar' }))
    await screen.findByText('Ingresa tu usuario o correo')
    expect(screen.getByText('Ingresa tu contraseña')).toBeInTheDocument()
    expect(enviar).not.toHaveBeenCalled()
  })

  it('valida una contraseña corta antes de registrar', async () => {
    const enviar = vi.fn(() => fallo(400))
    server.use(http.post(base + '/users', enviar))
    const { usuario } = app(RUTAS.registro)
    await completarRegistro(usuario, 'corta')
    await screen.findByText('Usa al menos 12 caracteres')
    expect(enviar).not.toHaveBeenCalled()
  })

  it('redirige a login cuando se crea la cuenta y conserva el destino', async () => {
    const { usuario, router } = app(RUTAS.registro, { state: { desde: RUTAS.tickets } })
    await completarRegistro(usuario, 'Clave-de-prueba')
    await screen.findByText('Cuenta creada. Inicia sesión para continuar.')
    expect(router.state.location.pathname).toBe(RUTAS.login)
    expect(router.state.location.state.desde).toBe(RUTAS.tickets)
  })

  it('muestra el duplicado del registro con un mensaje genérico', async () => {
    server.use(http.post(base + '/users', () => fallo(409, 'Mensaje privado')))
    const { usuario } = app(RUTAS.registro)
    await completarRegistro(usuario, 'Clave-de-prueba')
    await screen.findByText('El usuario o correo ya está registrado.')
    expect(screen.queryByText('Mensaje privado')).not.toBeInTheDocument()
  })

  it.each(['USER', 'STAFF'] as const)('deniega la ruta de administración para %s', async (rol) => {
    app(RUTAS.admin, { rol })
    expect(await screen.findByRole('heading', { name: '403' })).toBeInTheDocument()
  })

  it('permite la ruta administrativa y muestra sus enlaces para ADMIN', async () => {
    app(RUTAS.admin, { rol: 'ADMIN' })
    await screen.findByRole('heading', { name: 'Administración de eventos' })
    const nav = screen.getByRole('navigation', { name: 'Principal' })
    expect(within(nav).getByRole('link', { name: 'Validar ingreso' })).toBeInTheDocument()
    expect(within(nav).getByRole('link', { name: 'Administrar eventos' })).toBeInTheDocument()
    expect(within(nav).queryByRole('link', { name: 'Ingresar' })).not.toBeInTheDocument()
  })

  it('restaura sesión, consulta el perfil por ambas claves y limpia token y caché al salir', async () => {
    const { usuario, router, client } = app(RUTAS.cuenta, { rol: 'USER' })
    await screen.findByRole('heading', { name: 'Andrea Ruiz' })
    expect(client.getQueryData(['usuario', 'email', 'andrea@pulsepass.test'])).toBeDefined()
    await usuario.click(screen.getByRole('button', { name: 'Consultar por nombre de usuario' }))
    await waitFor(() => expect(client.getQueryData(['usuario', 'username', 'andrea'])).toBeDefined())
    await usuario.click(screen.getByRole('button', { name: 'Cerrar sesión' }))
    await waitFor(() => expect(router.state.location.pathname).toBe(RUTAS.inicio))
    expect(sessionStorage.getItem(ALMACEN_SESION)).toBeNull()
    expect(client.getQueryData(['usuario', 'username', 'andrea'])).toBeUndefined()
  })

  it('descarta la sesión almacenada vencida y permite continuar hacia registro', async () => {
    vi.spyOn(Date, 'now').mockReturnValue(FECHA_SESION)
    const { usuario } = app(RUTAS.tickets, { rol: 'USER', expira: FECHA_SESION - 1 })
    await screen.findByText('Tu sesión expiró. Vuelve a iniciar sesión.')
    expect(sessionStorage.getItem(ALMACEN_SESION)).toBeNull()
    await usuario.click(screen.getByRole('link', { name: 'Regístrate' }))
    await screen.findByRole('heading', { name: 'Crea tu cuenta' })
  })

  it('cierra una sesión activa si una petición protegida recibe 401', async () => {
    server.use(http.get(base + '/tickets/by-user', () => fallo(401)))
    const { router } = app(RUTAS.tickets, { rol: 'USER' })
    await screen.findByText('Tu sesión expiró. Vuelve a iniciar sesión.')
    expect(router.state.location.state.desde).toBe(RUTAS.tickets)
  })
})
