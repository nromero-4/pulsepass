import { screen } from '@testing-library/react'
import axe from 'axe-core'
import { expect, it } from 'vitest'
import { RUTAS } from '../lib/rutas'
import { app } from './renderApp'
import { eventoDemo } from './fixtures'

it.each([
  [RUTAS.inicio, 'Descubre los mejores eventos'],
  [RUTAS.eventos, 'Cartelera'],
  [RUTAS.evento(eventoDemo.eventCode), eventoDemo.name],
  [RUTAS.login, 'Inicia sesión'],
  [RUTAS.registro, 'Crea tu cuenta'],
])('comprueba la semántica accesible de %s', async (ruta, titulo) => {
  const { container } = app(ruta)
  await screen.findByRole('heading', { name: titulo })
  const resultado = await axe.run(container, {
    runOnly: { type: 'tag', values: ['wcag2a', 'wcag2aa', 'wcag21a', 'wcag21aa'] },
    // jsdom no calcula un contraste fiable; esta prueba comprueba el DOM y los nombres accesibles.
    rules: { 'color-contrast': { enabled: false } },
  })
  expect(
    resultado.violations.map(({ id, nodes }) => ({ id, elementos: nodes.map((node) => node.target) })),
  ).toEqual([])
})
