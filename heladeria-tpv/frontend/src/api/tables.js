import client from './client'

export async function getTables() {
  const { data } = await client.get('/tables')
  return data
}

export async function getTable(id) {
  const { data } = await client.get(`/tables/${id}`)
  return data
}

export async function createTable(name) {
  const { data } = await client.post('/tables', { name })
  return data
}

export async function renameTable(id, name) {
  const { data } = await client.put(`/tables/${id}`, { name })
  return data
}

export async function deleteTable(id) {
  await client.delete(`/tables/${id}`)
}

export async function updateTablePosition(id, positionX, positionY) {
  const { data } = await client.patch(`/tables/${id}/position`, { positionX, positionY })
  return data
}

export async function getTablePendingItems(tableId) {
  const { data } = await client.get(`/tables/${tableId}/items`)
  return data
}

export async function addProductToTable(tableId, productId, quantity) {
  const { data } = await client.post(`/tables/${tableId}/items`, { productId, quantity })
  return data
}

export async function removeProductFromTable(tableId, productId, quantity) {
  const { data } = await client.post(`/tables/${tableId}/items/remove`, { productId, quantity })
  return data
}
