import { z } from 'zod'
import { estadoEvento } from '../../lib/contratos'
import type { Evento } from '../../lib/contratos'

const lista = z.array(z.object({ eventCode: z.string(), name: z.string(), status: estadoEvento }))
type EventoReciente = z.infer<typeof lista>[number]
const clave = 'pulsepass.eventos-recientes'
const almacen = z.object({ sesion: z.number(), eventos: lista })

export function leerRecientes(sesion: number): EventoReciente[] {
  try {
    const valor = sessionStorage.getItem(clave)
    if (!valor) return []
    const parsed = almacen.safeParse(JSON.parse(valor))
    return parsed.success && parsed.data.sesion === sesion ? parsed.data.eventos : []
  } catch {
    return []
  }
}

export function recordarEvento(sesion: number, evento: Evento, nuevo = false) {
  const eventos = leerRecientes(sesion)
  if (!nuevo && !eventos.some(({ eventCode }) => eventCode === evento.eventCode)) return
  const { eventCode, name, status } = evento
  try {
    sessionStorage.setItem(
      clave,
      JSON.stringify({
        sesion,
        eventos: [
          { eventCode, name, status },
          ...eventos.filter((item) => item.eventCode !== eventCode),
        ],
      }),
    )
  } catch {
    // El acceso por código sigue funcionando sin almacenamiento de sesión.
  }
}

export function limpiarRecientes() {
  try {
    sessionStorage.removeItem(clave)
  } catch {
    /* almacenamiento no disponible */
  }
}
