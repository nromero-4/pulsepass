import { X } from 'lucide-react'
import { useEffect, useId, useRef } from 'react'
import type { ReactNode } from 'react'
import { Boton } from './ui'

interface Props {
  titulo: string
  onCerrar: () => void
  ocupado?: boolean
  children: ReactNode
}

export function Modal({ titulo, onCerrar, ocupado, children }: Props) {
  const ref = useRef<HTMLDialogElement>(null)
  const id = useId()

  useEffect(() => {
    const dialog = ref.current
    const origen = document.activeElement
    dialog?.showModal()
    return () => {
      dialog?.close()
      if (origen instanceof HTMLElement && origen.isConnected) origen.focus()
    }
  }, [])

  return (
    <dialog
      ref={ref}
      aria-labelledby={id}
      onCancel={(event) => {
        event.preventDefault()
        if (!ocupado) onCerrar()
      }}
      className="superficie fixed inset-0 m-auto max-h-[calc(100dvh-2rem)] w-[calc(100%-2rem)] max-w-md overflow-auto p-5 text-slate-900 backdrop:bg-slate-950/65 dark:text-slate-100"
    >
      <div className="mb-3 flex items-center justify-between gap-2">
        <h2 id={id} className="text-xl font-semibold">
          {titulo}
        </h2>
        <Boton
          className="px-3"
          variante="fantasma"
          aria-label="Cerrar diálogo"
          disabled={ocupado}
          onClick={onCerrar}
        >
          <X aria-hidden="true" className="size-5" />
        </Boton>
      </div>
      {children}
    </dialog>
  )
}
