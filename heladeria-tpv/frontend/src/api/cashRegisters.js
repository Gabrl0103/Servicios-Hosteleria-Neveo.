import client from './client'

export async function getCurrentCashRegister() {
  try {
    const { data } = await client.get('/cash-registers/current')
    return data
  } catch {
    // No hay turno abierto, esto es un estado normal, no un error real.
    return null
  }
}

export async function getAllCashRegisters() {
  const { data } = await client.get('/cash-registers')
  return data
}

export async function openCashRegister(userId, cashierName, openingAmount) {
  const params = { userId, cashierName }
  if (openingAmount != null) params.openingAmount = openingAmount
  const { data } = await client.post('/cash-registers/open', null, { params })
  return data
}

export async function closeCashRegister(id) {
  const { data } = await client.post(`/cash-registers/${id}/close`)
  return data
}
