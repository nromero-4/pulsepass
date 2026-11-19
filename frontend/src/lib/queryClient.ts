import { QueryClient } from '@tanstack/react-query'
import { ApiError } from './http'

export function crearQueryClient() {
  return new QueryClient({
    defaultOptions: {
      queries: {
        retry: (intento, error) =>
          intento < 1 && error instanceof ApiError && (error.status === 0 || error.status >= 500),
      },
      mutations: { retry: false },
    },
  })
}
