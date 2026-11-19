import { z } from 'zod'

const requerido = (label: string) => z.string().trim().min(1, label)

export const login = z.object({
  identifier: requerido('Ingresa tu usuario o correo'),
  password: z
    .string()
    .min(1, 'Ingresa tu contraseña')
    .refine((value) => value.trim() !== '', 'Ingresa tu contraseña'),
})

export const registro = z.object({
  username: requerido('Ingresa un nombre de usuario'),
  email: z.email('Ingresa un correo válido').trim(),
  firstName: requerido('Ingresa tu nombre'),
  lastName: requerido('Ingresa tu apellido'),
  phone: z.string().trim(),
  city: z.string().trim(),
  birthDate: z.iso.date('Ingresa tu fecha de nacimiento').refine((date) => {
    const hoy = new Intl.DateTimeFormat('sv-SE', { timeZone: 'America/Bogota' }).format(new Date())
    return date <= hoy
  }, 'La fecha no puede estar en el futuro'),
  password: z
    .string()
    .min(12, 'Usa al menos 12 caracteres')
    .refine((value) => value.trim() !== '', 'Ingresa una contraseña')
    .refine(
      (value) => new TextEncoder().encode(value).length <= 72,
      'La contraseña supera los 72 bytes',
    ),
})

export type Login = z.infer<typeof login>
export type Registro = z.infer<typeof registro>
