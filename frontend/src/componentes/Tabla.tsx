import type { Key, ReactNode } from 'react'

interface Columna<T> {
  encabezado: string
  celda: (fila: T) => ReactNode
}
interface Props<T> {
  titulo: string
  columnas: Columna<T>[]
  filas: T[]
  clave: (fila: T) => Key
}

export function Tabla<T>({ titulo, columnas, filas, clave }: Props<T>) {
  return (
    <div className="superficie overflow-x-auto">
      <table className="w-full border-collapse text-left text-sm">
        <caption className="sr-only">{titulo}</caption>
        <thead className="bg-slate-100 text-slate-700 dark:bg-slate-800 dark:text-slate-200">
          <tr>
            {columnas.map(({ encabezado }) => (
              <th key={encabezado} scope="col" className="px-3 py-3 font-semibold">
                {encabezado}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-200 dark:divide-slate-800">
          {filas.map((fila) => (
            <tr key={clave(fila)}>
              {columnas.map(({ encabezado, celda }) => (
                <td key={encabezado} className="px-3 py-2 break-words">
                  {celda(fila)}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
