import { Eye, EyeOff, Ticket } from 'lucide-react'
import { useState } from 'react'
import type { ComponentProps, ReactNode } from 'react'
import { Boton, Campo } from '../../componentes/ui'

export function FormularioCuenta({ titulo, children }: { titulo: string; children: ReactNode }) {
  return (
    <section className="superficie mx-auto max-w-md p-5 sm:p-8">
      <header className="mb-6 flex flex-col items-center gap-3 text-center">
        <span className="grid size-12 place-items-center rounded-xl bg-marca-600 text-white">
          <Ticket aria-hidden="true" className="size-6" />
        </span>
        <h1 className="text-2xl font-bold">{titulo}</h1>
      </header>
      {children}
    </section>
  )
}

export function Contrasena({
  children,
  ...props
}: ComponentProps<typeof Campo> & { children?: ReactNode }) {
  const [visible, setVisible] = useState(false)
  const Icono = visible ? EyeOff : Eye
  return (
    <div>
      <div className="relative">
        <Campo {...props} type={visible ? 'text' : 'password'} className="pr-12" />
        <Boton
          variante="fantasma"
          className="absolute top-[1.65rem] right-0 px-3"
          aria-label={visible ? 'Ocultar contraseña' : 'Mostrar contraseña'}
          onClick={() => setVisible(!visible)}
        >
          <Icono className="size-4" aria-hidden="true" />
        </Boton>
      </div>
      {children}
    </div>
  )
}
