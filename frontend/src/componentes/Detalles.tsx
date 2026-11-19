import type { ReactNode } from 'react'

export function Detalles({ filas }: { filas: { etiqueta: string; valor: ReactNode }[] }) {
  return (
    <dl className="divide-y divide-slate-200 dark:divide-slate-700">
      {filas.map(({ etiqueta, valor }) => (
        <div key={etiqueta} className="flex flex-wrap justify-between gap-2 py-3 text-sm">
          <dt className="secundario">{etiqueta}</dt>
          <dd className="min-w-0 font-medium break-words">{valor}</dd>
        </div>
      ))}
    </dl>
  )
}
