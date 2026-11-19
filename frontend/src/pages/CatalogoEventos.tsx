import { useQuery } from '@tanstack/react-query'
import { useSearchParams } from 'react-router'
import { z } from 'zod'
import { Consulta, Vacio } from '../componentes/estados'
import { Boton, Cabecera, Campo, Selector } from '../componentes/ui'
import { artistasQ, eventosQ } from '../features/eventos/consultas'
import { RejillaEventos } from '../features/eventos/presentacion'
import { categoria } from '../lib/contratos'
import { categorias, textoComparable } from '../lib/formatos'

const filtros = z.object({
  q: z.string().max(200).catch(''),
  categoria: categoria.optional().catch(undefined),
  artista: z.string().trim().max(150).catch(''),
})

export function Component() {
  const [params, setParams] = useSearchParams()
  const { q, categoria: categoriaActiva, artista } = filtros.parse(Object.fromEntries(params))
  const artistas = useQuery(artistasQ.activos())
  const perfil = useQuery(artistasQ.porNombre(artista))
  const seleccion = useQuery(artista ? eventosQ.porArtista(artista) : eventosQ.publicados())

  function cambiar(name: keyof z.infer<typeof filtros>, value: string, replace = false) {
    const next = new URLSearchParams(params)
    if (value) next.set(name, value)
    else next.delete(name)
    setParams(next, { replace })
  }

  return (
    <>
      <Cabecera titulo="Cartelera" />
      <div className="mb-5 grid items-end gap-3 sm:grid-cols-2 lg:grid-cols-[2fr_1fr_1fr_auto]">
        <Campo
          etiqueta="Buscar por nombre"
          type="search"
          placeholder="Buscar por nombre…"
          maxLength={200}
          value={q}
          onChange={(event) => cambiar('q', event.target.value, true)}
        />
        <Selector
          etiqueta="Categoría"
          value={categoriaActiva ?? ''}
          onChange={(event) => cambiar('categoria', event.target.value)}
        >
          <option value="">Todas</option>
          {categoria.options.map((value) => (
            <option key={value} value={value}>
              {categorias[value]}
            </option>
          ))}
        </Selector>
        <Selector
          etiqueta="Artista"
          value={artista}
          onChange={(event) => cambiar('artista', event.target.value)}
        >
          <option value="">Todos</option>
          {artista && !artistas.data?.some(({ stageName }) => stageName === artista) && (
            <option value={artista}>{perfil.data?.stageName ?? artista}</option>
          )}
          {artistas.data?.map(({ id, stageName }) => (
            <option key={id} value={stageName}>
              {stageName}
            </option>
          ))}
        </Selector>
        <Boton variante="secundario" onClick={() => setParams({})}>
          Limpiar
        </Boton>
      </div>
      {artistas.isError && (
        <p className="secundario mb-4 text-sm" role="status">
          El filtro por artista no está disponible en este momento.
        </p>
      )}
      <Consulta consulta={seleccion}>
        {(data) => {
          const texto = textoComparable(q.trim())
          const visibles = data.filter(
            (evento) =>
              (!categoriaActiva || evento.category === categoriaActiva) &&
              textoComparable(evento.name).includes(texto),
          )
          return (
            <>
              <p className="secundario mb-4 text-sm" aria-live="polite">
                {visibles.length} {visibles.length === 1 ? 'evento' : 'eventos'}
              </p>
              {visibles.length ? (
                <RejillaEventos eventos={visibles} />
              ) : (
                <Vacio
                  titulo="Sin resultados para tu búsqueda"
                  descripcion="Prueba con otros filtros."
                />
              )}
            </>
          )
        }}
      </Consulta>
    </>
  )
}
