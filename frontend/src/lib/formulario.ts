import type { FieldValues, Path, UseFormSetError } from 'react-hook-form'
import { ApiError, mensajeError } from './http'

export function errorFormulario<T extends FieldValues>(
  error: unknown,
  setError: UseFormSetError<T>,
  campos: readonly Path<T>[],
) {
  if (error instanceof ApiError && error.status === 400) {
    const detalles = Object.entries(error.details)
    const otros = detalles.filter(([name, message]) => {
      const campo = campos.find((field) => field === name)
      if (!campo) return true
      setError(campo, { type: 'server', message })
      return false
    })
    if (detalles.length && !otros.length) return
  }
  setError('root', { message: mensajeError(error) })
}
