import client from './client'

export async function getCashiers() {
  const { data } = await client.get('/cashiers')
  return data
}

export async function createCashier(name) {
  const { data } = await client.post('/cashiers', { name })
  return data
}

export async function deleteCashier(id) {
  await client.delete(`/cashiers/${id}`)
}
