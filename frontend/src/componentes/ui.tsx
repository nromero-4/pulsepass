import clsx from 'clsx'
import { LoaderCircle } from 'lucide-react'
import { useId } from 'react'
import type { ComponentProps, ReactNode } from 'react'
import { Link } from 'react-router'
import type { Ticket, Evento } from '../lib/contratos'
import { estados, fechaLocal } from '../lib/formatos'

type Variante = 'primario' | 'secundario' | 'peligro' | 'fantasma' | 'blanco' | 'transparente'
const estilos: Record<Variante, string> = {
  primario: 'bg-marca-600 text-white hover:bg-marca-700 border-transparent',
  secundario:
    'border-slate-300 bg-white text-slate-900 hover:bg-slate-100 dark:border-slate-600 dark:bg-slate-900 dark:text-white dark:hover:bg-slate-800',
  peligro: 'border-transparent bg-red-700 text-white hover:bg-red-800',
  fantasma:
    'border-transparent text-slate-700 hover:bg-slate-100 dark:text-slate-200 dark:hover:bg-slate-800',
  blanco: 'border-white bg-white text-marca-700 hover:bg-indigo-50 focus-visible:outline-white',
  transparente:
    'border-white/70 bg-transparent text-white hover:bg-white/10 focus-visible:outline-white',
}
function clasesBoton(variante: Variante, className?: string) {
  return clsx(
    'inline-flex min-h-11 items-center justify-center gap-2 rounded-lg border px-4 py-2.5 text-sm font-semibold transition-colors disabled:cursor-not-allowed disabled:opacity-60',
    estilos[variante],
    className,
  )
}

interface BotonProps extends ComponentProps<'button'> {
  variante?: Variante
  cargando?: boolean
}
export function Boton({
  variante = 'primario',
  cargando,
  className,
  type = 'button',
  disabled,
  children,
  ...props
}: BotonProps) {
  return (
    <button
      {...props}
      type={type}
      disabled={disabled || cargando}
      aria-busy={cargando}
      className={clasesBoton(variante, className)}
    >
      {cargando && <LoaderCircle className="size-4 motion-safe:animate-spin" aria-hidden="true" />}
      {cargando ? 'Cargando…' : children}
    </button>
  )
}

interface EnlaceProps extends ComponentProps<typeof Link> {
  variante?: Variante
}
export function EnlaceBoton({ variante = 'primario', className, ...props }: EnlaceProps) {
  return <Link {...props} className={clasesBoton(variante, className)} />
}

const estilosBadge: Record<Evento['status'] | Ticket['status'], string> = {
  DRAFT: 'bg-slate-200 text-slate-800',
  PUBLISHED: 'bg-emerald-100 text-emerald-900',
  SOLD_OUT: 'bg-amber-100 text-amber-900',
  CANCELLED: 'bg-red-100 text-red-900',
  FINISHED: 'bg-slate-200 text-slate-800',
  RESERVED: 'bg-sky-100 text-sky-900',
  PAID: 'bg-emerald-100 text-emerald-900',
  USED: 'bg-slate-300 text-slate-900',
}
export function Badge({ estado }: { estado: Evento['status'] | Ticket['status'] }) {
  return (
    <span
      className={clsx(
        'inline-flex w-fit rounded-full px-2.5 py-1 text-xs font-semibold',
        estilosBadge[estado],
      )}
    >
      {estados[estado]}
    </span>
  )
}

export function Fecha({ valor }: { valor: string }) {
  return <time dateTime={valor}>{fechaLocal(valor)}</time>
}

export function Codigo({ children }: { children: string }) {
  return (
    <code className="rounded-md bg-slate-100 px-2 py-1 font-mono text-sm font-medium break-all dark:bg-slate-800">
      {children}
    </code>
  )
}

export function Cabecera({
  titulo,
  descripcion,
  accion,
}: {
  titulo: string
  descripcion?: string
  accion?: ReactNode
}) {
  return (
    <header className="mb-7 flex flex-wrap items-end justify-between gap-4">
      <div className="min-w-0">
        <h1 className="text-3xl font-bold tracking-tight sm:text-4xl">{titulo}</h1>
        {descripcion && <p className="secundario mt-2">{descripcion}</p>}
      </div>
      {accion}
    </header>
  )
}

const campo =
  'min-h-11 w-full min-w-0 rounded-lg border border-slate-400 bg-white px-3 py-2 text-slate-900 placeholder:text-slate-500 aria-invalid:border-red-600 dark:border-slate-600 dark:bg-slate-950 dark:text-white dark:placeholder:text-slate-400'
interface BaseCampo {
  etiqueta: string
  error?: string
}
interface MarcoProps extends BaseCampo {
  id: string
  children: ReactNode
}
function MarcoCampo({ etiqueta, error, id, children }: MarcoProps) {
  return (
    <div className="min-w-0 space-y-1.5">
      <label htmlFor={id} className="block text-sm font-semibold">
        {etiqueta}
      </label>
      {children}
      {error && (
        <p id={id + '-error'} role="alert" className="text-sm text-red-800 dark:text-red-300">
          {error}
        </p>
      )}
    </div>
  )
}

export function Campo({ etiqueta, error, className, ...props }: BaseCampo & ComponentProps<'input'>) {
  const id = useId()
  return (
    <MarcoCampo {...{ etiqueta, error, id }}>
      <input
        {...props}
        id={id}
        className={clsx(campo, className)}
        aria-invalid={!!error}
        aria-describedby={error ? id + '-error' : props['aria-describedby']}
      />
    </MarcoCampo>
  )
}

export function Selector({
  etiqueta,
  error,
  className,
  ...props
}: BaseCampo & ComponentProps<'select'>) {
  const id = useId()
  return (
    <MarcoCampo {...{ etiqueta, error, id }}>
      <select
        {...props}
        id={id}
        className={clsx(campo, className)}
        aria-invalid={!!error}
        aria-describedby={error ? id + '-error' : props['aria-describedby']}
      />
    </MarcoCampo>
  )
}

export function Area({ etiqueta, error, className, ...props }: BaseCampo & ComponentProps<'textarea'>) {
  const id = useId()
  return (
    <MarcoCampo {...{ etiqueta, error, id }}>
      <textarea
        {...props}
        id={id}
        className={clsx(campo, className)}
        aria-invalid={!!error}
        aria-describedby={error ? id + '-error' : props['aria-describedby']}
      />
    </MarcoCampo>
  )
}
