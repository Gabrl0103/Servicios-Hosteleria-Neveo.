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

export async function getOrders(from, to, page = 0, size = 20) {
  const { data } = await client.get('/orders', { params: { from, to, page, size } })
  return data
}

export async function anularOrder(id, motivo) {
  const { data } = await client.post(`/orders/${id}/anular`, null, { params: { motivo } })
  return data
}
