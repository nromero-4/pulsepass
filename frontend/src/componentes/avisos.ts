import { createContext, useContext } from 'react'

interface Avisos {
  avisar: (mensaje: string, tipo?: 'exito' | 'error') => void
}

export const AvisosContext = createContext<Avisos | null>(null)

export function useAvisos() {
  const context = useContext(AvisosContext)
  if (!context) throw new Error('Los avisos requieren AvisosProvider')
  return context
}
