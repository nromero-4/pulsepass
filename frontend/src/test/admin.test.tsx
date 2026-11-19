import { fireEvent, screen, waitFor, within } from '@testing-library/react'
import type { UserEvent } from '@testing-library/user-event'
import { HttpResponse, http } from 'msw'
import { describe, expect, it, vi } from 'vitest'
import { RUTAS } from '../lib/rutas'
import { artistaDemo, base, eventoDemo, fallo, sedeDemo } from './fixtures'
import { app } from './renderApp'
import { server } from './setup'

const borrador = { ...eventoDemo, status: 'DRAFT', artists: [] }
const gestion = RUTAS.gestion(eventoDemo.eventCode)
const apiEvento = base + '/events/' + eventoDemo.eventCode

async function llenarEvento(usuario: UserEvent) {
  fireEvent.change(await screen.findByLabelText('Código del evento'), {
    target: { value: eventoDemo.eventCode },
  })
  fireEvent.change(screen.getByLabelText('Nombre', { exact: true }), {
    target: { value: eventoDemo.name },
  })
  fireEvent.change(screen.getByLabelText('Descripción'), { target: { value: eventoDemo.description } })
  await usuario.selectOptions(screen.getByLabelText('Categoría'), eventoDemo.category)
  fireEvent.change(screen.getByLabelText('Edad mínima'), {
    target: { value: String(eventoDemo.minimumAge) },
  })
  fireEvent.change(screen.getByLabelText('Fecha y hora'), { target: { value: '2026-12-12T20:00' } })
  await usuario.selectOptions(screen.getByLabelText('Sede'), sedeDemo.code)
  await usuario.click(screen.getByRole('button', { name: 'Crear evento' }))
}

describe('Administración de eventos', () => {
  it('marca los campos vacíos y evita la llamada al backend', async () => {
    const crear = vi.fn(() => fallo(400))
    server.use(http.post(base + '/events', crear))
    const { usuario } = app(RUTAS.nuevoEvento, { rol: 'ADMIN' })
    await usuario.click(await screen.findByRole('button', { name: 'Crear evento' }))
    await screen.findByText('Ingresa el nombre')
    expect(screen.getByText('Ingresa una descripción')).toBeInTheDocument()
    expect(crear).not.toHaveBeenCalled()
  })

  it('crea el borrador, navega a gestión y recuerda el evento por sesión', async () => {
    const crear = vi.fn(async ({ request }: { request: Request }) => {
      expect(await request.json()).toMatchObject({
        venueCode: sedeDemo.code,
        eventCode: eventoDemo.eventCode,
        minimumAge: 18,
      })
      return HttpResponse.json(borrador, { status: 201 })
    })
    server.use(http.post(base + '/events', crear))
    const { usuario, router } = app(RUTAS.nuevoEvento, { rol: 'ADMIN' })
    await llenarEvento(usuario)
    await screen.findByRole('heading', { name: eventoDemo.name })
    expect(router.state.location.pathname).toBe(gestion)
    expect(screen.getByText('Borrador')).toBeInTheDocument()
    expect(crear).toHaveBeenCalledOnce()
    await usuario.click(screen.getByRole('link', { name: 'Volver a eventos' }))
    await screen.findByRole('link', { name: 'Gestionar' })
  })

  it('asocia los detalles de un 400 al campo correspondiente', async () => {
    server.use(
      http.post(base + '/events', () =>
        fallo(400, 'Validation failed', { name: 'Corrige el nombre del evento' }),
      ),
    )
    const { usuario } = app(RUTAS.nuevoEvento, { rol: 'ADMIN' })
    await llenarEvento(usuario)
    await screen.findByText('Corrige el nombre del evento')
    expect(screen.getByLabelText('Nombre', { exact: true })).toHaveAttribute('aria-invalid', 'true')
  })

  it('consulta por código y publica el evento tras confirmar', async () => {
    server.use(
      http.get(apiEvento, () => HttpResponse.json(borrador)),
      http.patch(apiEvento + '/publish', () => HttpResponse.json(eventoDemo)),
    )
    const { usuario, client } = app(RUTAS.admin, { rol: 'ADMIN' })
    await usuario.type(await screen.findByLabelText('Buscar evento por código'), eventoDemo.eventCode)
    await usuario.click(screen.getByRole('button', { name: 'Abrir' }))
    await usuario.click(await screen.findByRole('button', { name: 'Publicar evento' }))
    await usuario.click(
      within(screen.getByRole('dialog')).getByRole('button', { name: 'Confirmar publicación' }),
    )
    await screen.findByText('Evento publicado.')
    expect(screen.getByText('Publicado')).toBeInTheDocument()
    expect(client.getQueryData(['evento', eventoDemo.eventCode, 'admin'])).toEqual(eventoDemo)
  })

  it('muestra un conflicto al publicar', async () => {
    server.use(
      http.get(apiEvento, () => HttpResponse.json(borrador)),
      http.patch(apiEvento + '/publish', () => fallo(409, 'La sede está inactiva.')),
    )
    const { usuario } = app(gestion, { rol: 'ADMIN' })
    await usuario.click(await screen.findByRole('button', { name: 'Publicar evento' }))
    const modal = screen.getByRole('dialog')
    await usuario.click(within(modal).getByRole('button', { name: 'Confirmar publicación' }))
    await within(modal).findByText('La sede está inactiva.')
  })

  it('asocia un artista y actualiza el listado', async () => {
    server.use(
      http.get(apiEvento, () => HttpResponse.json(borrador)),
      http.post(apiEvento + '/artists/' + artistaDemo.id, () =>
        HttpResponse.json({ ...borrador, artists: [artistaDemo.stageName] }),
      ),
    )
    const { usuario } = app(gestion, { rol: 'ADMIN' })
    await usuario.selectOptions(await screen.findByLabelText('Agregar artista'), String(artistaDemo.id))
    await usuario.click(screen.getByRole('button', { name: 'Asociar' }))
    await screen.findByText('Artista asociado al evento.')
    await waitFor(() => expect(screen.getByRole('listitem')).toHaveTextContent(artistaDemo.stageName))
  })

  it('muestra un duplicado de artista devuelto por el backend', async () => {
    server.use(
      http.get(apiEvento, () => HttpResponse.json(borrador)),
      http.post(apiEvento + '/artists/' + artistaDemo.id, () =>
        fallo(409, 'El artista ya está asociado.'),
      ),
    )
    const { usuario } = app(gestion, { rol: 'ADMIN' })
    await usuario.selectOptions(await screen.findByLabelText('Agregar artista'), String(artistaDemo.id))
    await usuario.click(screen.getByRole('button', { name: 'Asociar' }))
    await screen.findByText('El artista ya está asociado.')
  })
})
