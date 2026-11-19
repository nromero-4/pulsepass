import { screen, waitFor, within } from '@testing-library/react'
import { HttpResponse, http } from 'msw'
import { describe, expect, it, vi } from 'vitest'
import { RUTAS } from '../lib/rutas'
import { base, email, eventoDemo, fallo, ticketDemo, tokenDemo } from './fixtures'
import { app } from './renderApp'
import { server } from './setup'

const evento = RUTAS.evento(eventoDemo.eventCode)
const apiTicket = base + '/tickets/' + ticketDemo.ticketCode

describe('Compra y tickets propios', () => {
  it('confirma la última entrada aunque el detalle deje de ser público', async () => {
    let agotado = false
    server.use(
      http.get(base + '/events/' + eventoDemo.eventCode, () =>
        agotado ? fallo(404) : HttpResponse.json(eventoDemo),
      ),
      http.post(base + '/tickets', () => {
        agotado = true
        return HttpResponse.json(ticketDemo, { status: 201 })
      }),
    )
    const { usuario, client } = app(evento, { rol: 'USER' })
    await usuario.click(await screen.findByRole('button', { name: 'Comprar entrada' }))
    await usuario.click(within(screen.getByRole('dialog')).getByRole('button', { name: 'Confirmar' }))
    await screen.findByRole('heading', { name: '¡Compra realizada con éxito!' })
    expect(client.getQueryState(['evento', eventoDemo.eventCode, 'publico'])?.isInvalidated).toBe(true)
  })

  it('manda al visitante al login con el evento como destino', async () => {
    const { usuario, router } = app(evento)
    await usuario.click(await screen.findByRole('link', { name: 'Inicia sesión para comprar' }))
    await screen.findByRole('heading', { name: 'Inicia sesión' })
    expect(router.state.location.state.desde).toBe(evento)
  })

  it('envía el email de la sesión y navega a confirmación tras comprar', async () => {
    const comprar = vi.fn(async ({ request }: { request: Request }) => {
      expect(request.headers.get('Authorization')).toBe('Bearer ' + tokenDemo)
      expect(await request.json()).toEqual({
        userEmail: email,
        eventCode: eventoDemo.eventCode,
        type: 'VIP',
      })
      return HttpResponse.json(ticketDemo, { status: 201 })
    })
    server.use(http.post(base + '/tickets', comprar))
    const { usuario, router, client } = app(evento, { rol: 'USER' })
    await usuario.click(await screen.findByRole('radio', { name: 'VIP' }))
    await usuario.click(screen.getByRole('button', { name: 'Comprar entrada' }))
    const dialogo = screen.getByRole('dialog', { name: 'Confirmar compra' })
    expect(within(dialogo).getByText(email)).toBeInTheDocument()
    await usuario.click(within(dialogo).getByRole('button', { name: 'Confirmar' }))
    await screen.findByRole('heading', { name: '¡Compra realizada con éxito!' })
    expect(router.state.location.pathname).toBe(RUTAS.confirmacion(ticketDemo.ticketCode))
    expect(client.getQueryData(['ticket', ticketDemo.ticketCode])).toEqual(ticketDemo)
    expect(comprar).toHaveBeenCalledOnce()
  })

  it('mantiene el modal y muestra la regla de negocio cuando la compra falla', async () => {
    server.use(http.post(base + '/tickets', () => fallo(409, 'El evento alcanzó su capacidad.')))
    const { usuario } = app(evento, { rol: 'USER' })
    await usuario.click(await screen.findByRole('button', { name: 'Comprar entrada' }))
    const dialogo = screen.getByRole('dialog')
    await usuario.click(within(dialogo).getByRole('button', { name: 'Confirmar' }))
    await within(dialogo).findByText('El evento alcanzó su capacidad.')
    expect(dialogo).toBeVisible()
  })

  it('lista y filtra tickets con el email de la sesión', async () => {
    const listar = vi.fn(({ request }: { request: Request }) => {
      expect(new URL(request.url).searchParams.get('email')).toBe(email)
      return HttpResponse.json([
        ticketDemo,
        { ...ticketDemo, id: 2, ticketCode: 'TKT-USED', status: 'USED' },
      ])
    })
    server.use(http.get(base + '/tickets/by-user', listar))
    const { usuario } = app(RUTAS.tickets, { rol: 'USER' })
    await screen.findByText('TKT-USED')
    await usuario.click(screen.getByRole('button', { name: 'Pagados' }))
    expect(screen.getByText(ticketDemo.ticketCode)).toBeInTheDocument()
    expect(screen.queryByText('TKT-USED')).not.toBeInTheDocument()
    expect(listar).toHaveBeenCalledOnce()
  })

  it('muestra el estado vacío de tickets propios', async () => {
    server.use(http.get(base + '/tickets/by-user', () => HttpResponse.json([])))
    app(RUTAS.tickets, { rol: 'USER' })
    await screen.findByRole('heading', { name: 'Aún no tienes tickets' })
  })

  it('confirma, cancela y actualiza la lista', async () => {
    let data = ticketDemo
    const listar = vi.fn(() => HttpResponse.json([data]))
    server.use(
      http.get(base + '/tickets/by-user', listar),
      http.patch(apiTicket + '/cancel', () => {
        data = { ...ticketDemo, status: 'CANCELLED' }
        return HttpResponse.json(data)
      }),
    )
    const { usuario } = app(RUTAS.tickets, { rol: 'USER' })
    await usuario.click(
      await screen.findByRole('button', { name: 'Cancelar ticket ' + ticketDemo.ticketCode }),
    )
    await usuario.click(
      within(screen.getByRole('dialog')).getByRole('button', { name: 'Cancelar ticket' }),
    )
    await screen.findByText('Ticket cancelado.')
    expect(screen.getByText('Cancelado')).toBeInTheDocument()
    expect(listar).toHaveBeenCalledTimes(2)
  })

  it('muestra el conflicto de cancelación y conserva el modal', async () => {
    server.use(http.patch(apiTicket + '/cancel', () => fallo(409, 'El ticket ya no se puede cancelar.')))
    const { usuario } = app(RUTAS.ticket(ticketDemo.ticketCode), { rol: 'USER' })
    await usuario.click(await screen.findByRole('button', { name: 'Cancelar ticket' }))
    const modal = screen.getByRole('dialog')
    await usuario.click(within(modal).getByRole('button', { name: 'Cancelar ticket' }))
    await within(modal).findByText('El ticket ya no se puede cancelar.')
    expect(modal).toBeVisible()
  })

  it.each([403, 404])('oculta datos del ticket cuando la API devuelve %s', async (status) => {
    server.use(http.get(apiTicket, () => fallo(status, ticketDemo.eventName)))
    app(RUTAS.ticket(ticketDemo.ticketCode), { rol: 'USER' })
    await screen.findByRole('heading', { name: 'Ticket no disponible' })
    expect(screen.queryByText(ticketDemo.eventName)).not.toBeInTheDocument()
  })

  it('oculta el botón de cancelación cuando el ticket no está pagado', async () => {
    server.use(http.get(apiTicket, () => HttpResponse.json({ ...ticketDemo, status: 'USED' })))
    app(RUTAS.ticket(ticketDemo.ticketCode), { rol: 'USER' })
    await screen.findByText('Usado')
    expect(screen.queryByRole('button', { name: 'Cancelar ticket' })).not.toBeInTheDocument()
  })
})

describe('Operación STAFF', () => {
  it('busca el ticket y registra el ingreso', async () => {
    server.use(
      http.patch(apiTicket + '/use', () => HttpResponse.json({ ...ticketDemo, status: 'USED' })),
    )
    const { usuario } = app(RUTAS.validar, { rol: 'STAFF' })
    await usuario.type(await screen.findByLabelText('Código de ticket'), ticketDemo.ticketCode)
    await usuario.keyboard('{Enter}')
    await usuario.click(await screen.findByRole('button', { name: 'Marcar como usado' }))
    await screen.findByText('Ingreso registrado.')
    expect(screen.getByText('Usado')).toBeInTheDocument()
  })

  it('muestra el 409 si el ticket se usó tras la consulta', async () => {
    server.use(
      http.patch(apiTicket + '/use', () => fallo(409, 'El ticket ya fue utilizado por otra persona.')),
    )
    const { usuario } = app(RUTAS.validar, { rol: 'STAFF' })
    await usuario.type(await screen.findByLabelText('Código de ticket'), ticketDemo.ticketCode)
    await usuario.click(screen.getByRole('button', { name: 'Buscar' }))
    await usuario.click(await screen.findByRole('button', { name: 'Marcar como usado' }))
    await screen.findByText('El ticket ya fue utilizado por otra persona.')
  })

  it('muestra que el ticket no existe tras buscarlo', async () => {
    server.use(http.get(apiTicket, () => fallo(404)))
    const { usuario } = app(RUTAS.validar, { rol: 'STAFF' })
    await usuario.type(await screen.findByLabelText('Código de ticket'), ticketDemo.ticketCode)
    await usuario.click(screen.getByRole('button', { name: 'Buscar' }))
    await screen.findByRole('heading', { name: 'Ticket no encontrado' })
  })

  it('lista los tickets pagados de un evento y actualiza la tabla al usar uno', async () => {
    let pagados = [ticketDemo]
    server.use(
      http.get(base + '/events/' + eventoDemo.eventCode + '/tickets/paid', () =>
        HttpResponse.json(pagados),
      ),
      http.patch(apiTicket + '/use', () => {
        pagados = []
        return HttpResponse.json({ ...ticketDemo, status: 'USED' })
      }),
    )
    const { usuario } = app(RUTAS.pagados, { rol: 'STAFF' })
    await usuario.type(await screen.findByLabelText('Código del evento'), eventoDemo.eventCode)
    await usuario.click(screen.getByRole('button', { name: 'Consultar' }))
    const tabla = await screen.findByRole('table', { name: 'Tickets pagados' })
    expect(within(tabla).getByRole('row', { name: /TKT-8F2A1C.*VIP/ })).toBeInTheDocument()
    await usuario.click(
      within(tabla).getByRole('button', { name: 'Usar ticket ' + ticketDemo.ticketCode }),
    )
    await waitFor(() =>
      expect(screen.getByRole('heading', { name: 'No hay tickets pagados' })).toBeInTheDocument(),
    )
  })
})
