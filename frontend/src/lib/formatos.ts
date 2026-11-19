import type { Categoria, EstadoTicket, Evento, Ticket } from './contratos'

const moneda = new Intl.NumberFormat('es-CO', {
  style: 'currency',
  currency: 'COP',
  maximumFractionDigits: 2,
})
const formatoFecha = new Intl.DateTimeFormat('es-CO', { dateStyle: 'long', timeZone: 'UTC' })
const formatoHora = new Intl.DateTimeFormat('es-CO', {
  dateStyle: 'long',
  timeStyle: 'short',
  timeZone: 'UTC',
})

export const precioCOP = (valor: number) => moneda.format(valor)
export function fechaLocal(valor: string) {
  // Spring entrega una fecha local sin zona; UTC conserva la fecha y hora civil en pantalla.
  const tieneHora = valor.includes('T')
  const instante = new Date(
    tieneHora ? (valor.endsWith('Z') ? valor : `${valor}Z`) : `${valor}T00:00:00Z`,
  )
  return (tieneHora ? formatoHora : formatoFecha).format(instante)
}

export const categorias: Record<Categoria, string> = {
  MUSIC: 'Música',
  SPORTS: 'Deportes',
  TECHNOLOGY: 'Tecnología',
  EDUCATION: 'Educación',
  CULTURE: 'Cultura',
  ENTERTAINMENT: 'Entretenimiento',
}

export const tiposTicket: Record<Ticket['type'], string> = {
  GENERAL: 'General',
  VIP: 'VIP',
  BACKSTAGE: 'Backstage',
  STUDENT: 'Estudiante',
}

export const estados: Record<Evento['status'] | EstadoTicket, string> = {
  DRAFT: 'Borrador',
  PUBLISHED: 'Publicado',
  SOLD_OUT: 'Agotado',
  CANCELLED: 'Cancelado',
  FINISHED: 'Finalizado',
  RESERVED: 'Reservado',
  PAID: 'Pagado',
  USED: 'Usado',
}

export function textoComparable(value: string) {
  return value
    .normalize('NFD')
    .replace(/\p{Diacritic}/gu, '')
    .toLocaleLowerCase('es-CO')
}
