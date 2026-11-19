import { queryOptions } from '@tanstack/react-query'
import { apiAuth } from './api'

export const cuentaQ = {
  porEmail: (email: string) =>
    queryOptions({
      queryKey: ['usuario', 'email', email],
      queryFn: ({ signal }) => apiAuth.porEmail(email, signal),
      staleTime: 60_000,
    }),
  porUsuario: (username: string) =>
    queryOptions({
      queryKey: ['usuario', 'username', username],
      queryFn: ({ signal }) => apiAuth.porUsuario(username, signal),
      staleTime: 60_000,
    }),
}
