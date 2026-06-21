import { createContext, useContext, useState, useEffect, useCallback } from 'react'
import { getCurrentCashRegister } from '../api/cashRegisters'

const SessionContext = createContext(null)

// La app no tiene login. Se usa siempre el mismo usuario de sistema
// (id 1, creado automaticamente por el backend) para registrar
// ventas y turnos.
const SYSTEM_USER = { id: 1, name: 'Heladeria' }

export function SessionProvider({ children }) {
  const [cashRegister, setCashRegister] = useState(null)
  const [loadingRegister, setLoadingRegister] = useState(true)

  const refreshCashRegister = useCallback(async () => {
    setLoadingRegister(true)
    try {
      const register = await getCurrentCashRegister()
      setCashRegister(register)
    } finally {
      setLoadingRegister(false)
    }
  }, [])

  useEffect(() => {
    refreshCashRegister()
  }, [refreshCashRegister])

  const value = {
    user: SYSTEM_USER,
    cashRegister,
    loadingRegister,
    refreshCashRegister,
  }

  return <SessionContext.Provider value={value}>{children}</SessionContext.Provider>
}

export function useSession() {
  const context = useContext(SessionContext)
  if (!context) {
    throw new Error('useSession debe usarse dentro de SessionProvider')
  }
  return context
}
