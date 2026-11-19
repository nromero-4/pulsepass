import { credencial, identidad, usuario } from '../../lib/contratos'
import { http, parametro } from '../../lib/http'
import type { Login, Registro } from './formularios'

export const apiAuth = {
  login: (body: Login) => http('/auth/login', credencial, { method: 'POST', body, anonimo: true }),
  me: (token?: string, signal?: AbortSignal) =>
    http('/auth/me', identidad, { tokenInicial: token, signal }),
  registrar: (body: Registro) => http('/users', usuario, { method: 'POST', body, anonimo: true }),
  porEmail: (email: string, signal: AbortSignal) =>
    http(`/users/by-email?${parametro('email', email)}`, usuario, { signal }),
  porUsuario: (username: string, signal: AbortSignal) =>
    http(`/users/by-username?${parametro('username', username)}`, usuario, { signal }),
}
