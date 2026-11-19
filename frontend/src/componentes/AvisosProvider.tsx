import { CheckCircle2, CircleAlert, X } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import type { ReactNode } from 'react'
import { AvisosContext } from './avisos'

export function AvisosProvider({ children }: { children: ReactNode }) {
  const [aviso, setAviso] = useState<{ mensaje: string; tipo: 'exito' | 'error' }>()
  const avisar = useCallback(
    (mensaje: string, tipo: 'exito' | 'error' = 'exito') => setAviso({ mensaje, tipo }),
    [],
  )
  useEffect(() => {
    if (!aviso) return
    const timer = window.setTimeout(() => setAviso(undefined), 6000)
    return () => window.clearTimeout(timer)
  }, [aviso])
  const Icono = aviso?.tipo === 'error' ? CircleAlert : CheckCircle2

  return (
    <AvisosContext.Provider value={{ avisar }}>
      {children}
      <div
        aria-live="polite"
        aria-atomic="true"
        className="pointer-events-none fixed right-3 bottom-24 z-50 w-[calc(100%-1.5rem)] max-w-sm md:right-5 md:bottom-5"
      >
        {aviso && (
          <div className="superficie pointer-events-auto flex items-center gap-3 p-3 shadow-lg">
            <Icono aria-hidden="true" className="size-5 shrink-0 text-marca-600 dark:text-indigo-300" />
            <p className="flex-1 text-sm font-semibold break-words">{aviso.mensaje}</p>
            <button
              type="button"
              className="grid size-11 shrink-0 place-items-center rounded-lg"
              aria-label="Cerrar aviso"
              onClick={() => setAviso(undefined)}
            >
              <X className="size-4" aria-hidden="true" />
            </button>
          </div>
        )}
      </div>
    </AvisosContext.Provider>
  )
}
