import '@testing-library/jest-dom/vitest'
import { cleanup, configure } from '@testing-library/react'
import { setupServer } from 'msw/node'
import { afterAll, afterEach, beforeAll, vi } from 'vitest'
import { conectarSesion } from '../lib/http'
import { handlers } from './fixtures'

export const server = setupServer(...handlers)
configure({ asyncUtilTimeout: 5000 })

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
afterEach(() => {
  cleanup()
  conectarSesion(undefined)
  document.documentElement.classList.remove('dark')
  sessionStorage.clear()
  server.resetHandlers()
  vi.restoreAllMocks()
})
afterAll(() => server.close())

if (!HTMLDialogElement.prototype.showModal) {
  Object.defineProperties(HTMLDialogElement.prototype, {
    showModal: {
      value: function (this: HTMLDialogElement) {
        this.setAttribute('open', '')
      },
    },
    close: {
      value: function (this: HTMLDialogElement) {
        this.removeAttribute('open')
      },
    },
  })
}
