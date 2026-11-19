import { z } from 'zod'
import { categoria, codigo } from '../../lib/contratos'

export const crearEvento = z.object({
  eventCode: codigo,
  name: z.string().trim().min(1, 'Ingresa el nombre'),
  description: z
    .string()
    .trim()
    .min(1, 'Ingresa una descripción')
    .max(1000, 'Usa máximo 1000 caracteres'),
  category: categoria,
  eventDate: z.iso.datetime({ local: true, message: 'Selecciona una fecha y hora válidas' }),
  minimumAge: z
    .number({ error: 'Ingresa una edad mínima' })
    .int('Ingresa una edad entera')
    .min(0, 'La edad mínima es 0'),
  venueCode: codigo,
})

export const buscarCodigo = z.object({ code: codigo })
export const asociarArtista = z.object({
  id: z.number({ error: 'Selecciona un artista' }).int().positive('Selecciona un artista'),
})
export type CrearEvento = z.infer<typeof crearEvento>
