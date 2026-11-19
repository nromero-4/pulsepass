import { HttpResponse, http as msw } from 'msw'
import { describe, expect, it, vi } from 'vitest'
import { z } from 'zod'
import { precioCOP } from '../lib/formatos'
import { ApiError, conectarSesion, http, observarExpiracion } from '../lib/http'
import { retornoSeguro } from '../lib/rutas'
import { base, fallo, tokenDemo } from './fixtures'
import { server } from './setup'

const ok = z.object({ ok: z.boolean() })

describe('Cliente HTTP', () => {
  it('envía el JWT solo en Authorization y valida la respuesta', async () => {
    conectarSesion(tokenDemo)
    server.use(
      msw.get(base + '/verify', ({ request }) => {
        expect(request.url).toBe(base + '/verify')
        expect(request.headers.get('Authorization')).toBe('Bearer ' + tokenDemo)
        return HttpResponse.json({ ok: true })
      }),
    )
    await expect(http('/verify', ok)).resolves.toEqual({ ok: true })
  })

  it('valida ErrorResponse y conserva los detalles de campo de un 400', async () => {
    server.use(
      msw.post(base + '/verify', () => fallo(400, 'Validation failed', { name: 'Campo requerido' })),
    )
    await expect(http('/verify', ok, { method: 'POST' })).rejects.toMatchObject({
      name: 'ApiError',
      status: 400,
      details: { name: 'Campo requerido' },
    })
  })

  it('maneja un cuerpo inválido y oculta los detalles de un 500', async () => {
    server.use(msw.get(base + '/verify', () => new HttpResponse('Private stack', { status: 500 })))
    await expect(http('/verify', ok)).rejects.toMatchObject({
      status: 500,
      message: 'Ocurrió un error inesperado. Inténtalo más tarde.',
    })
  })

  it('la credencial vigente expira con un 401, pero un login anónimo no la modifica', async () => {
    conectarSesion(tokenDemo)
    const expirar = vi.fn()
    const dejarDeObservar = observarExpiracion(expirar)
    server.use(msw.get(base + '/verify', () => fallo(401)))
    await expect(http('/verify', ok, { anonimo: true })).rejects.toBeInstanceOf(ApiError)
    expect(expirar).not.toHaveBeenCalled()
    await expect(http('/verify', ok)).rejects.toMatchObject({ status: 401 })
    expect(expirar).toHaveBeenCalledOnce()
    dejarDeObservar()
  })

  it('rechaza respuestas con estructura inesperada y distingue fallos de red', async () => {
    server.use(msw.get(base + '/verify', () => HttpResponse.json({ ok: 'sí' })))
    await expect(http('/verify', ok)).rejects.toMatchObject({ status: 502 })
    server.use(msw.get(base + '/verify', () => HttpResponse.error()))
    await expect(http('/verify', ok)).rejects.toMatchObject({ status: 0 })
  })

  it('usa el mensaje del backend para 409 y un mensaje genérico para 403/404', async () => {
    for (const [status, expected] of [
      [409, 'Conflicto específico'],
      [403, 'No tienes permisos para acceder a este recurso.'],
      [404, 'No encontramos lo que buscas.'],
    ] as const) {
      server.use(msw.get(base + '/verify', () => fallo(status, 'Conflicto específico')))
      await expect(http('/verify', ok)).rejects.toMatchObject({ status, message: expected })
    }
  })
})

it('formatea precios en COP y valida rutas de retorno internas', () => {
  expect(precioCOP(250000)).toMatch(/\$\s?250\.000/)
  expect(retornoSeguro('/eventos?q=rock#info')).toBe('/eventos?q=rock#info')
  expect(retornoSeguro('//otro.example/')).toBe('/')
  expect(retornoSeguro('/login?x=1')).toBe('/')
})
