import { fireEvent, screen, waitFor } from '@testing-library/react'
import { HttpResponse, delay, http } from 'msw'
import { describe, expect, it, vi } from 'vitest'
import { RUTAS } from '../lib/rutas'
import { artistaDemo, base, eventoDemo, fallo, sedeDemo } from './fixtures'
import { app } from './renderApp'
import { server } from './setup'

describe('Vistas públicas', () => {
  it('muestra secciones de eventos y artistas en Inicio', async () => {
    app(RUTAS.inicio)
    expect(
      await screen.findByRole('heading', { name: 'Descubre los mejores eventos' }),
    ).toBeInTheDocument()
    expect(await screen.findByRole('heading', { name: eventoDemo.name })).toBeInTheDocument()
    expect(await screen.findByRole('link', { name: artistaDemo.stageName })).toBeInTheDocument()
  })

  it('muestra carga y luego lista vacía en cartelera', async () => {
    server.use(
      http.get(base + '/events/published', async () => {
        await delay(80)
        return HttpResponse.json([])
      }),
    )
    app(RUTAS.eventos)
    await screen.findByRole('heading', { name: 'Cartelera' })
    expect(screen.getByText('Cargando…')).toBeInTheDocument()
    await screen.findByRole('heading', { name: 'Sin resultados para tu búsqueda' })
  })

  it('persiste filtros de texto y categoría en la URL y permite limpiarlos', async () => {
    const { router, usuario } = app(RUTAS.eventos)
    await screen.findByRole('heading', { name: eventoDemo.name })
    await usuario.type(screen.getByRole('searchbox', { name: 'Buscar por nombre' }), 'Caribbean')
    await usuario.selectOptions(screen.getByRole('combobox', { name: 'Categoría' }), 'TECHNOLOGY')
    expect(router.state.location.search).toBe('?q=Caribbean&categoria=TECHNOLOGY')
    expect(screen.getByRole('heading', { name: 'Sin resultados para tu búsqueda' })).toBeInTheDocument()
    await usuario.click(screen.getByRole('button', { name: 'Limpiar' }))
    expect(await screen.findByRole('heading', { name: eventoDemo.name })).toBeInTheDocument()
  })

  it('consulta eventos y artista por nombre artístico', async () => {
    const consulta = vi.fn(({ request }: { request: Request }) => {
      expect(new URL(request.url).searchParams.get('stageName')).toBe(artistaDemo.stageName)
      return HttpResponse.json([eventoDemo])
    })
    const perfil = vi.fn(() => HttpResponse.json(artistaDemo))
    server.use(
      http.get(base + '/events/by-artist', consulta),
      http.get(base + '/artists/by-stage-name', perfil),
    )
    app(RUTAS.eventos + '?artista=Solar+Beat')
    await screen.findByRole('heading', { name: eventoDemo.name })
    await waitFor(() => expect(perfil).toHaveBeenCalledOnce())
    expect(consulta).toHaveBeenCalledOnce()
  })

  it('muestra detalle de evento, artistas y capacidad de sede', async () => {
    app(RUTAS.evento(eventoDemo.eventCode))
    await screen.findByRole('heading', { name: eventoDemo.name })
    expect(screen.getByRole('link', { name: artistaDemo.stageName })).toBeInTheDocument()
    await screen.findByText('Santa Marta · Capacidad 3')
  })

  it.each([403, 404])('muestra evento no disponible con %s', async (status) => {
    server.use(http.get(base + '/events/' + eventoDemo.eventCode, () => fallo(status)))
    app(RUTAS.evento(eventoDemo.eventCode))
    await screen.findByRole('heading', { name: 'Evento no disponible' })
  })

  it('deshabilita compra si el estado devuelto no es PUBLISHED', async () => {
    server.use(
      http.get(base + '/events/' + eventoDemo.eventCode, () =>
        HttpResponse.json({ ...eventoDemo, status: 'SOLD_OUT' }),
      ),
    )
    app(RUTAS.evento(eventoDemo.eventCode), { rol: 'USER' })
    expect(await screen.findByRole('button', { name: 'Comprar entrada' })).toBeDisabled()
    expect(screen.getByText('Agotado')).toBeInTheDocument()
  })

  it('lista artistas activos y abre su perfil con eventos', async () => {
    const { usuario } = app(RUTAS.artistas)
    await usuario.click(await screen.findByRole('link', { name: /Solar Beat/ }))
    await screen.findByRole('heading', { name: artistaDemo.stageName, level: 1 })
    await screen.findByRole('heading', { name: eventoDemo.name })
  })

  it('rechaza un ID de artista inválido sin consultar la API', async () => {
    const buscar = vi.fn(() => fallo(404))
    server.use(http.get(base + '/artists/:id', buscar))
    app(RUTAS.artistas + '/abc')
    await screen.findByRole('heading', { name: 'Artista no encontrado' })
    expect(buscar).not.toHaveBeenCalled()
  })

  it('lista y abre una sede activa', async () => {
    const { usuario } = app(RUTAS.sedes)
    await usuario.click(await screen.findByRole('link', { name: /Marina Convention Center/ }))
    await screen.findByRole('heading', { name: sedeDemo.name, level: 1 })
    expect(screen.getByText(sedeDemo.address)).toBeInTheDocument()
  })

  it('maneja una sede que no existe', async () => {
    server.use(http.get(base + '/venues/FALTA', () => fallo(404)))
    app(RUTAS.sede('FALTA'))
    await screen.findByRole('heading', { name: 'Sede no encontrada' })
  })

  it('muestra una ruta desconocida y abre y cierra el menú con teclado', async () => {
    const { usuario } = app('/ruta-que-no-existe')
    await screen.findByRole('heading', { name: '404' })
    const menu = screen.getByRole('button', { name: 'Abrir menú' })
    menu.focus()
    await usuario.keyboard('{Enter}')
    expect(menu).toHaveAttribute('aria-expanded', 'true')
    await usuario.keyboard('{Escape}')
    expect(menu).toHaveAttribute('aria-expanded', 'false')
    expect(menu).toHaveFocus()
  })

  it('muestra una página de error sin revelar el detalle interno de una carga', async () => {
    app('/fallo', {
      router: [
        {
          path: '/',
          hydrateFallbackElement: <p>Cargando…</p>,
          lazy: async () => ({ ErrorBoundary: (await import('../componentes/estados')).ErrorPagina }),
          children: [
            {
              path: 'fallo',
              loader: () => {
                throw new Error('Stack interno de prueba')
              },
            },
          ],
        },
      ],
    })
    await screen.findByRole('heading', { name: '¡Ups! Algo salió mal.' })
    expect(screen.queryByText('Stack interno de prueba')).not.toBeInTheDocument()
  })

  it('permite alternar el tema', async () => {
    app(RUTAS.inicio)
    fireEvent.click(await screen.findByRole('button', { name: 'Activar modo oscuro' }))
    expect(document.documentElement).toHaveClass('dark')
    fireEvent.click(screen.getByRole('button', { name: 'Activar modo claro' }))
    expect(document.documentElement).not.toHaveClass('dark')
  })
})
