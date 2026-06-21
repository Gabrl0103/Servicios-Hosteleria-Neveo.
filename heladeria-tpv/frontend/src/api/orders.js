import client from './client'

export async function createOrder(order) {
  const { data } = await client.post('/orders', order)
  return data
}

export async function getOrder(id) {
  const { data } = await client.get(`/orders/${id}`)
  return data
}

export async function voidOrder(id, voidedByUserId) {
  const { data } = await client.post(`/orders/${id}/void`, null, {
    params: { voidedByUserId },
  })
  return data
}
