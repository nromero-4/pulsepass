import clsx from 'clsx'
import {
  CalendarDays,
  CirclePlay,
  GraduationCap,
  Landmark,
  MapPin,
  Monitor,
  Music2,
  Trophy,
} from 'lucide-react'
import type { LucideIcon } from 'lucide-react'
import type { ReactNode } from 'react'
import { Link } from 'react-router'
import type { Artista, Categoria, Evento, Sede } from '../../lib/contratos'
import { categorias } from '../../lib/formatos'
import { RUTAS } from '../../lib/rutas'
import { Badge, Fecha } from '../../componentes/ui'

const diseno: Record<Categoria, { colores: string; icono: LucideIcon }> = {
  MUSIC: {
    colores: 'bg-indigo-100 text-indigo-800 dark:bg-indigo-950 dark:text-indigo-300',
    icono: Music2,
  },
  TECHNOLOGY: { colores: 'bg-sky-100 text-sky-900 dark:bg-sky-950 dark:text-sky-300', icono: Monitor },
  SPORTS: {
    colores: 'bg-emerald-100 text-emerald-900 dark:bg-emerald-950 dark:text-emerald-300',
    icono: Trophy,
  },
  CULTURE: { colores: 'bg-rose-100 text-rose-900 dark:bg-rose-950 dark:text-rose-300', icono: Landmark },
  EDUCATION: {
    colores: 'bg-amber-100 text-amber-900 dark:bg-amber-950 dark:text-amber-300',
    icono: GraduationCap,
  },
  ENTERTAINMENT: {
    colores: 'bg-violet-100 text-violet-900 dark:bg-violet-950 dark:text-violet-300',
    icono: CirclePlay,
  },
}

export function PanelCategoria({ categoria, className }: { categoria: Categoria; className?: string }) {
  const { colores, icono: Icono } = diseno[categoria]
  return (
    <div aria-hidden="true" className={clsx('grid place-items-center', colores, className)}>
      <Icono className="size-11" strokeWidth={1.6} />
    </div>
  )
}

export function DatoIcono({ icono: Icono, children }: { icono: LucideIcon; children: ReactNode }) {
  return (
    <span className="secundario flex items-start gap-2 text-sm">
      <Icono aria-hidden="true" className="mt-0.5 size-4 shrink-0" />{' '}
      <span className="min-w-0 break-words">{children}</span>
    </span>
  )
}

export function EtiquetaCategoria({ categoria }: { categoria: Categoria }) {
  return (
    <span className="text-xs font-bold tracking-wide text-marca-700 uppercase dark:text-indigo-300">
      {categorias[categoria]}
    </span>
  )
}

export function TarjetaEvento({ evento, nivel }: { evento: Evento; nivel: 2 | 3 }) {
  const Titulo = nivel === 2 ? 'h2' : 'h3'
  return (
    <article className="superficie overflow-hidden transition-shadow hover:shadow-md">
      <Link to={RUTAS.evento(evento.eventCode)} className="block h-full focus-visible:-outline-offset-4">
        <PanelCategoria categoria={evento.category} className="aspect-[2.45/1]" />
        <div className="space-y-2 p-4">
          <EtiquetaCategoria categoria={evento.category} />
          <Titulo className="text-lg leading-snug font-semibold break-words">{evento.name}</Titulo>
          <DatoIcono icono={CalendarDays}>
            <Fecha valor={evento.eventDate} />
          </DatoIcono>
          <DatoIcono icono={MapPin}>{evento.venueName}</DatoIcono>
          <Badge estado={evento.status} />
        </div>
      </Link>
    </article>
  )
}

export function RejillaEventos({ eventos, nivel = 2 }: { eventos: Evento[]; nivel?: 2 | 3 }) {
  return (
    <div className="grid gap-4 sm:grid-cols-2 md:grid-cols-3">
      {eventos.map((evento) => (
        <TarjetaEvento key={evento.id} evento={evento} nivel={nivel} />
      ))}
    </div>
  )
}

const avatares = [
  'bg-indigo-100 text-indigo-800 dark:bg-indigo-950 dark:text-indigo-200',
  'bg-emerald-100 text-emerald-900 dark:bg-emerald-950 dark:text-emerald-200',
  'bg-sky-100 text-sky-900 dark:bg-sky-950 dark:text-sky-200',
]
export function Avatar({
  nombre,
  orden = 0,
  grande = false,
}: {
  nombre: string
  orden?: number
  grande?: boolean
}) {
  const iniciales = nombre
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part[0])
    .join('')
    .toUpperCase()
  return (
    <span
      aria-hidden="true"
      className={clsx(
        'grid shrink-0 place-items-center rounded-full font-titulo font-semibold',
        grande ? 'size-16 text-xl' : 'size-10 text-sm',
        avatares[orden % avatares.length],
      )}
    >
      {iniciales}
    </span>
  )
}

export function ArtistaTarjeta({ artista, orden }: { artista: Artista; orden: number }) {
  return (
    <article className="superficie">
      <Link
        to={RUTAS.artista(artista.id)}
        className="flex h-full min-w-0 flex-col items-center gap-2 rounded-2xl p-5 text-center hover:bg-slate-50 dark:hover:bg-slate-800"
      >
        <Avatar nombre={artista.stageName} grande orden={orden} />
        <h2 className="mt-1 font-semibold break-words">{artista.stageName}</h2>
        <p className="secundario text-sm">{artista.genre}</p>
      </Link>
    </article>
  )
}

export function Activo({ valor }: { valor: boolean }) {
  return (
    <span
      className={clsx(
        'w-fit rounded-full px-2.5 py-1 text-xs font-semibold',
        valor ? 'bg-emerald-100 text-emerald-900' : 'bg-slate-200 text-slate-800',
      )}
    >
      {valor ? 'Activa' : 'Inactiva'}
    </span>
  )
}

export function SedeTarjeta({ sede }: { sede: Sede }) {
  return (
    <article className="superficie">
      <Link
        to={RUTAS.sede(sede.code)}
        className="block h-full space-y-2 rounded-2xl p-5 hover:bg-slate-50 dark:hover:bg-slate-800"
      >
        <div className="flex flex-wrap items-start justify-between gap-2">
          <h2 className="text-lg font-semibold">{sede.name}</h2>
          <Activo valor={sede.active} />
        </div>
        <DatoIcono icono={MapPin}>
          {sede.city} · Capacidad {sede.capacity}
        </DatoIcono>
      </Link>
    </article>
  )
}
